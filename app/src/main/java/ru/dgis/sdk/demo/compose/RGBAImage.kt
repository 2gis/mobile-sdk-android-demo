package ru.dgis.sdk.demo.compose

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import java.nio.ByteBuffer

@Composable
fun RGBAImage(byteArray: ByteArray, width: Int, height: Int) {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val buffer = ByteBuffer.wrap(byteArray)
    bitmap.copyPixelsFromBuffer(buffer)

    val imageBitmap = bitmap.asImageBitmap()

    Image(
        bitmap = imageBitmap,
        contentDescription = "RGBA_8888 Image"
    )
}
