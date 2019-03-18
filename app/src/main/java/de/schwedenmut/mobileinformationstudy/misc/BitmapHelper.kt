package de.schwedenmut.mobileinformationstudy.misc

import android.graphics.Bitmap
import android.media.Image

class BitmapHelper {

    fun convertImageToBitmap(image: Image): Bitmap {
        val planes : Image.Plane = image.planes[0]
        val rowStride = planes.rowStride
        val pixelStride = planes.pixelStride
        val rowPadding = rowStride - pixelStride * image.width
        val buffer = planes.buffer
        val bitmap = Bitmap.createBitmap(image.width + rowPadding / pixelStride,
                image.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        return bitmap
    }
}
