package de.schwedenmut.mobileinformationstudy.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.v4.content.LocalBroadcastManager
import de.schwedenmut.mobileinformationstudy.misc.Const
import de.schwedenmut.mobileinformationstudy.misc.MediaProjectionHelper
import de.schwedenmut.mobileinformationstudy.misc.Utilities
import de.schwedenmut.mobileinformationstudy.misc.Utilities.saveBitmap
import de.schwedenmut.mobileinformationstudy.persistence.ScreenCapture
import de.schwedenmut.mobileinformationstudy.persistence.ScreenCaptureDao
import java.io.File
import kotlin.system.measureTimeMillis

class ScreenshotRunnable(val mContext: Context, val mMediaProjectionHelper: MediaProjectionHelper, val screenCaptureDao: ScreenCaptureDao): Runnable {
    override fun run() {
        var timestamp = System.currentTimeMillis()
        var fileName =  "$timestamp"
        var filePNG = File(mContext.filesDir, fileName + ".png")
        var fileJPEG = File(mContext.filesDir, fileName + ".jpeg")
        lateinit var bitmap: Bitmap
        var elapsedGetBitmap = measureTimeMillis {
            bitmap = mMediaProjectionHelper.getCurrentBitmap()
        }

        val elapsedPNG = measureTimeMillis {
            saveBitmap(bitmap, filePNG, Bitmap.CompressFormat.PNG)
        }
        val elapsedJPEG = measureTimeMillis {
            saveBitmap(bitmap, fileJPEG, Bitmap.CompressFormat.JPEG)
        }
        //Log.d("After", "${elapsed} - ${file.length()}")
        AsyncTask.execute {
            screenCaptureDao.insertScreenCapture(ScreenCapture(null, timestamp, filePNG.length(), fileJPEG.length(), Utilities.getRecentPackage(mContext), bitmap.width, bitmap.height, bitmap.density, elapsedPNG+elapsedGetBitmap, elapsedJPEG+elapsedGetBitmap))
            filePNG.delete()
            fileJPEG.delete()
            Intent().also { intent ->
                intent.action = Const.BROADCAST
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
            }
        }
    }
}
