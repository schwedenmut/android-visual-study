package de.schwedenmut.mobileinformationstudy.misc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.OrientationEventListener

class MediaProjectionHelper constructor(val mediaProjection: MediaProjection,
                                        val display: Display, val displayDensity: Int,
                                        val context: Context) {
    private lateinit var mImageReader: ImageReader
    private lateinit var mVirtualDisplay: VirtualDisplay
    private lateinit var projectionHandler: Handler

    private var displayHeight: Int = 1
    private var displayWidth: Int = 1
    private var displayRotation: Int = display.rotation

    private var precedingBitmap: Bitmap = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_8888)


    private val mOrientationEventListener: OrientationEventListener = object: OrientationEventListener(context){
        override fun onOrientationChanged(orientation: Int) {
            var rotation = display.rotation
            if (rotation != displayRotation) {
                displayRotation = rotation
                mVirtualDisplay.release()
                setupVirtualDisplay()
            }
        }
    }
    private val mMediaProjectionStopListener: MediaProjection.Callback = object: MediaProjection.Callback(){
        override fun onStop() {
            projectionHandler.post {
                mVirtualDisplay.release()
                mOrientationEventListener.disable()
                mediaProjection.unregisterCallback(this)
            }
        }
    }

    companion object {
        @JvmField val maxImages: Int = 15
        @JvmField val flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
    }

    fun startProjection() {
        Thread{
            Looper.prepare()
            projectionHandler = Handler()
            Looper.loop()
        }.start()
        if (mOrientationEventListener.canDetectOrientation()) mOrientationEventListener.enable()
        mediaProjection.registerCallback(mMediaProjectionStopListener, projectionHandler)
        setupVirtualDisplay()
    }

    fun stopProjection() {
        projectionHandler.post { mediaProjection.stop() }
    }

    fun getCurrentBitmap(): Bitmap {
        val image = mImageReader.acquireLatestImage() ?: return precedingBitmap
        precedingBitmap = BitmapHelper().convertImageToBitmap(image)
        return precedingBitmap
    }

    private fun setupVirtualDisplay() {
        var size = Point()
        display.getRealSize(size)
        displayHeight = size.y
        displayWidth = size.x
        //Log.d("setupVirtualDisplay","Image width ${displayWidth} - height ${displayHeight}")
        mImageReader = ImageReader.newInstance(displayWidth, displayHeight, PixelFormat.RGBA_8888, maxImages)
        mVirtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture", displayWidth,
                displayHeight, displayDensity, flags, mImageReader.surface, null, projectionHandler)
    }


}
