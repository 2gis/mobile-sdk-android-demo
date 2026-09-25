package ru.dgis.sdk.demo.compose.examples.snapshot

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import java.nio.ByteBuffer

@Composable
fun RGBAImage(byteArray: ByteArray, width: Int, height: Int) {
    val bitmap = createBitmap(width, height)
    val buffer = ByteBuffer.wrap(byteArray)
    bitmap.copyPixelsFromBuffer(buffer)

    val imageBitmap = bitmap.asImageBitmap()

    Image(
        bitmap = imageBitmap,
        contentDescription = "RGBA_8888 Image"
    )
}
