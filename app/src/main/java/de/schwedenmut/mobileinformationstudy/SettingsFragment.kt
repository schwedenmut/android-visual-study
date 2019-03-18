package de.schwedenmut.mobileinformationstudy


import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import de.schwedenmut.mobileinformationstudy.misc.Const
import de.schwedenmut.mobileinformationstudy.misc.MyAsyncTask
import de.schwedenmut.mobileinformationstudy.misc.Utilities
import de.schwedenmut.mobileinformationstudy.persistence.DeviceInformationDao
import de.schwedenmut.mobileinformationstudy.persistence.ScreenCaptureDao
import de.schwedenmut.mobileinformationstudy.persistence.ScreenCaptureRoomDatabase
import de.schwedenmut.mobileinformationstudy.service.ScreenCaptureListener
import org.hcilab.projects.nlog.library.misc.NotificationRoomDatabase
import org.hcilab.projects.nlog.library.misc.PostedEntryDao
import org.hcilab.projects.nlog.library.misc.Util
import org.hcilab.projects.nlog.library.service.NotificationHandler


class SettingsFragment : PreferenceFragmentCompat() {

    private val TAG :String = SettingsFragment::class.java.name
    private lateinit var notificationStatus : Preference
    private lateinit var screencaptureStatus : Preference
    private lateinit var notificationEntries : Preference
    private lateinit var screencaptureEntries : Preference
    private lateinit var notificationUpdateReceiver: BroadcastReceiver
    private lateinit var notificationRoomDatabase: NotificationRoomDatabase
    private lateinit var captureRoomScreenCaptureRoomDatabase: ScreenCaptureRoomDatabase
    private lateinit var postedEntryDao: PostedEntryDao
    private lateinit var screenCaptureDao: ScreenCaptureDao
    private lateinit var deviceInformationDao: DeviceInformationDao
    private lateinit var mMediaProjectionManager : MediaProjectionManager

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(R.xml.preferences)

        notificationStatus = findPreference(Const.NOTIFICATION_STATUS)
        notificationStatus.setOnPreferenceClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            true
        }
        screencaptureStatus = findPreference(Const.SCREENCAPTURE_STATUS)
        screencaptureStatus.setOnPreferenceClickListener {
            when (Utilities.isServiceRunning(ScreenCaptureListener::class.java.name, context!!)) {
                true -> {
                    activity!!.stopService(Intent(context, ScreenCaptureListener::class.java))
                    screencaptureStatus.summary = "Disabled"
                    false
                }
                else -> {
                    mMediaProjectionManager = activity!!.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), Const.REQUEST_MEDIA_PROJECTION)
                    true
                }
            }

        }
        notificationEntries = findPreference(Const.NOTIFICATION_ENTRIES)
        screencaptureEntries = findPreference(Const.SCREENCAPTURE_ENTRIES)

        findPreference(Const.PREF_VERSION).summary = BuildConfig.VERSION_NAME + (when (Const.DEBUG) {
            true -> " dev"
            else -> ""
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Const.REQUEST_MEDIA_PROJECTION -> {
                    screencaptureStatus.summary = "Enabled"
                    activity!!.startService(Intent(context!!, ScreenCaptureListener::class.java).putExtra(Const.EXTRA, data))
                }
            }
        } else activity!!.stopService(Intent(context!!, ScreenCaptureListener::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationRoomDatabase = NotificationRoomDatabase.getDatabase(activity)
        captureRoomScreenCaptureRoomDatabase = ScreenCaptureRoomDatabase.getDatabase(activity!!)!!
        postedEntryDao = notificationRoomDatabase.postedEntryDao()
        screenCaptureDao = captureRoomScreenCaptureRoomDatabase.screenCaptureDao()
        deviceInformationDao = captureRoomScreenCaptureRoomDatabase.deviceInformationDao()

        notificationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                update()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        when(Util.isNotificationAccessEnabled(activity)) {
            true -> notificationStatus.summary = "Enabled"
            else -> notificationStatus.summary = "Disabled"
        }
        when(Utilities.isServiceRunning(ScreenCaptureListener::class.java.name, context!!)) {
            true -> screencaptureStatus.summary = "Enabled"
            else -> screencaptureStatus.summary = "Disabled"
        }
        val filter = IntentFilter()
        filter.addAction(NotificationHandler.BROADCAST)
        filter.addAction(Const.BROADCAST)
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(notificationUpdateReceiver, filter)

        update()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(notificationUpdateReceiver)
        super.onPause()
    }

    private fun update() {
        MyAsyncTask({
            val r1 = postedEntryDao.getRowCount()
            val r2 = screenCaptureDao.getRowCount()
            (r1 to r2)
        }, {
            notificationEntries.summary = it.first.toString()
            screencaptureEntries.summary = it.second.toString()
        }).execute()
    }
}
