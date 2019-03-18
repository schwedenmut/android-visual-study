package de.schwedenmut.mobileinformationstudy.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.PRIORITY_MIN
import android.view.WindowManager
import de.schwedenmut.mobileinformationstudy.MainActivity
import de.schwedenmut.mobileinformationstudy.R
import de.schwedenmut.mobileinformationstudy.misc.Const
import de.schwedenmut.mobileinformationstudy.misc.MediaProjectionHelper
import de.schwedenmut.mobileinformationstudy.persistence.ScreenCaptureDao
import de.schwedenmut.mobileinformationstudy.persistence.ScreenCaptureRoomDatabase
import org.hcilab.projects.nlog.library.misc.Util


class ScreenCaptureListener : Service() {

    private var mIntent: Intent = Intent()
    private lateinit var mContext: Context
    private lateinit var database: ScreenCaptureRoomDatabase
    private lateinit var screenCaptureDao: ScreenCaptureDao

    private lateinit var mMediaProjectionHelper: MediaProjectionHelper
    private var mHandler = Handler()
    private val mHandlerTask: Runnable = object : Runnable {
        var screenOn = false
        override fun run() {
            if (Util.isScreenOn(application)) {
                screenOn = true
                ScreenshotManager.startScreenshot(mContext, mMediaProjectionHelper, screenCaptureDao!!)
            } else if (screenOn) {
                screenOn = false
                ScreenshotManager.endSession(mContext, mMediaProjectionHelper, screenCaptureDao!!)
            }
            mHandler.postDelayed(this, 100)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        mContext = this
        database = ScreenCaptureRoomDatabase.getDatabase(mContext)!!
        screenCaptureDao = database.screenCaptureDao()

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("screencapture_service", "Screencapture Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_imageservice_notification)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentTitle("Background Evaluation")
                .setContentText("Tap to start MobileInformationGain Study")
                .setContentIntent(PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), Intent(applicationContext, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                .build()

        startForeground(42, notification)
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mIntent = intent!!.getParcelableExtra(Const.EXTRA)
        val mediaProjectionManager = getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mIntent)
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay

        mMediaProjectionHelper = MediaProjectionHelper(mediaProjection, display, resources.displayMetrics.densityDpi,mContext)

        mMediaProjectionHelper.startProjection()
        mHandlerTask.run()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
        mMediaProjectionHelper.stopProjection()
        stopSelf()
        return super.onDestroy()
    }
}
