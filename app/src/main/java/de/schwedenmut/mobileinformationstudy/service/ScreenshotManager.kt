package de.schwedenmut.mobileinformationstudy.service

import android.content.Context
import de.schwedenmut.mobileinformationstudy.misc.MediaProjectionHelper
import de.schwedenmut.mobileinformationstudy.persistence.ScreenCapture
import de.schwedenmut.mobileinformationstudy.persistence.ScreenCaptureDao
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


object ScreenshotManager {

    val sInstance = this
    // Teilweise werden während der Studie Hintergrundaufgaben deutlich laggy ausgeführt. Z.B. die Vervollständigung der
    // Tastatur. Daher der Versuch immer einen freien Thread zu behalten, auch wenn dadurch ggf. weniger Performance für
    // die Erstellung und Auswertung der Bilder bereitgestellt wird.
    private val NUMBERS_OF_CORES = if (Runtime.getRuntime().availableProcessors() > 2) Runtime.getRuntime().availableProcessors() - 1 else 1
    private val KEEP_ALIVE_TIME: Long = 1
    private val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS

    //Queue of Runnables for Taking and Decoding the Screenshots
    private val mTaskQueue = LinkedBlockingQueue<Runnable>()

    //Managed Pool of Decoder Threads
    private val mExecutorService = ThreadPoolExecutor(NUMBERS_OF_CORES, NUMBERS_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mTaskQueue, BackgroundThreadFactory())

    fun getInstance(): ScreenshotManager{
        return sInstance
    }

    fun startScreenshot(context: Context, mMediaProjectionHelper: MediaProjectionHelper, screenCaptureDao: ScreenCaptureDao){
        val screenshotRunnable = this.mTaskQueue.poll() ?: ScreenshotRunnable(context, mMediaProjectionHelper, screenCaptureDao)
        this.mExecutorService.execute(screenshotRunnable)
    }

    fun endSession(context: Context, mMediaProjectionHelper: MediaProjectionHelper, screenCaptureDao: ScreenCaptureDao) {
        val screenshotRunnable = Runnable { screenCaptureDao.insertScreenCapture(
                ScreenCapture(null, System.currentTimeMillis(), 0, 0, "SESSION_END", 0, 0, 0, 0, 0)
        ) }
        this.mExecutorService.execute(screenshotRunnable)
    }


}

class BackgroundThreadFactory: ThreadFactory {
    override fun newThread(r: Runnable?): Thread {
        val thread = Thread(r)
        thread.name = "ScreenThread"
        thread.priority = android.os.Process.THREAD_PRIORITY_BACKGROUND
        return thread
    }
}
