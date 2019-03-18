package de.schwedenmut.mobileinformationstudy.misc

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v4.content.FileProvider
import de.schwedenmut.mobileinformationstudy.BuildConfig
import de.schwedenmut.mobileinformationstudy.persistence.DeviceInformation
import de.schwedenmut.mobileinformationstudy.persistence.ScreenCaptureRoomDatabase
import org.hcilab.projects.nlog.library.misc.NotificationRoomDatabase
import org.hcilab.projects.nlog.library.misc.Util
import java.io.File
import java.io.FileWriter
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ExportTask(context: Context) : AsyncTask<Void, Int, Void>(){

    companion object {
        var exporting = false
    }

    private val contextReference: WeakReference<Context> = WeakReference(context)
    private val POSTED_NOTIFICATION_EXPORT_FILE_NAME = "export_postednotification_%s.csv"
    private val REMOVED_NOTIFICATION_EXPORT_FILE_NAME = "export_removednotification_%s.csv"
    private val SCREENCAPTURE_EXPORT_FILE_NAME = "export_screencapture_%s.csv"
    private val DEVICEINFORMATION_EXPORT_FILE_NAME = "export_deviceinformation_%s.csv"
    private val progressDialog = ProgressDialog(context)
    //generate connection to the room database
    val screenCaptureRoomDatabase = ScreenCaptureRoomDatabase.getDatabase(context!!)
    val screenCaptureDao = screenCaptureRoomDatabase?.screenCaptureDao()
    val deviceInformationDao = screenCaptureRoomDatabase?.deviceInformationDao()
    val notificationRoomDatabase = NotificationRoomDatabase.getDatabase(context!!)
    val postedNotificationDao = notificationRoomDatabase.postedEntryDao()
    val removedNotificationDao = notificationRoomDatabase.removedEntryDao()


    override fun onPreExecute() {
        super.onPreExecute()
        exporting = true

        progressDialog.setTitle("Database Export")
        progressDialog.setMessage("Initializing (1/3)\nExporting notifications (2/3)\nExporting screencaptures (3/3)")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        MyAsyncTask({ postedNotificationDao.rowCount + removedNotificationDao.rowCount + screenCaptureDao?.getRowCount()!! }, { progressDialog.max = it }).execute()
        progressDialog.show()
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        progressDialog.progress = values[0]?: 0 //TODO an reale Werte anpassen
    }

    override fun doInBackground(vararg params: Void): Void? {
        val context = contextReference.get()
        val list = ArrayList<File>()


        val currentTime = System.currentTimeMillis()
        //persist device information into the database
        val deviceInformation = DeviceInformation(null, BuildConfig.VERSION_CODE, Util.getLocale(context),
                Build.MODEL, Build.DEVICE, Build.PRODUCT, Build.MANUFACTURER,
                Build.VERSION.SDK_INT, TimeZone.getDefault().id, TimeZone.getDefault().getOffset(currentTime),
                currentTime)
        deviceInformationDao?.insertDeviceInformation(deviceInformation)

        val exportPath = File(context!!.cacheDir, "share")
        if (!exportPath.exists()) exportPath.mkdirs()
        //remove old files
        (exportPath.listFiles())?.forEach { if (it.name.startsWith("export")) it.delete() }

        //filenames
        val exportDate = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date(currentTime))
        val postedNotificationExportFileName = String.format(POSTED_NOTIFICATION_EXPORT_FILE_NAME, exportDate)
        val removedNotificationExportFileName = String.format(REMOVED_NOTIFICATION_EXPORT_FILE_NAME, exportDate)
        val screencaptureExportFileName = String.format(SCREENCAPTURE_EXPORT_FILE_NAME, exportDate)
        val deviceInformationExportFileName = String.format(DEVICEINFORMATION_EXPORT_FILE_NAME, exportDate)
        var exportFile = File(exportPath, postedNotificationExportFileName).also { list.add(it) }

        //export
        var count = 0
        var fileWriter = FileWriter(exportFile)
        fileWriter.append("id,content\n")
        postedNotificationDao.allPostedEntryData.forEach {
            publishProgress(it._ID)
            count++
            fileWriter.append(it.toString()+"\n")
        }
        fileWriter.flush()
        exportFile = File(exportPath, removedNotificationExportFileName).also { list.add(it) }
        fileWriter = FileWriter(exportFile)
        fileWriter.append("id,content\n")
        removedNotificationDao.allRemovedEntryData.forEach {
            publishProgress(count++)
            fileWriter.append(it.toString()+"\n")
        }
        fileWriter.flush()


        exportFile = File(exportPath, screencaptureExportFileName).also { list.add(it) }
        fileWriter = FileWriter(exportFile)
        fileWriter.append("id,timestamp,filesizePNG,filesizeJPEG,recentApplication,width,height,density,elapsedPNG,elapsedJPEG\n")
        screenCaptureDao?.getAllScreenCaptureData()?.forEach {
            publishProgress(count++)
            it.toString()
            fileWriter.append(it.toString()+"\n")
        }
        fileWriter.flush()
        fileWriter = FileWriter(File(exportPath, deviceInformationExportFileName).also { list.add(it) })
        fileWriter.append("id,version,locale,model,device,product,manufacturer,sdk,timezone,offset,exportTime\n")
        deviceInformationDao?.getDeviceInformation()?.forEach {
            fileWriter.append(it.toString()+"\n")
        }
        fileWriter.flush()
        fileWriter.close()

        //Contentprovider
        val uriList: ArrayList<Uri> = ArrayList()
        list.forEach {
            uriList.add(FileProvider.getUriForFile(context!!,"de.schwedenmut.mobileinformationstudy.fileprovider", it))
        }
        //Sharedialog
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).setType("text/csv").putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
        context!!.startActivity(Intent.createChooser(shareIntent, "Export Data"))
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        if (progressDialog.isShowing) progressDialog.dismiss()
        exporting = false
    }
}
