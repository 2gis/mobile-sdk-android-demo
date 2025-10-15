package ru.dgis.sdk.demo.car

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.Surface

/*
 A sample renderer demonstrating drawing capabilities on a surface provided by AndroidAutoMapSession.
 This renderer draws a circle with a black background and a number inside, which increments every second.
 Refer to MapSession from this demo for details on how to use this renderer.
 */
class CustomRenderer {
    private var renderThread: RenderThread? = null

    fun start(surface: Surface) {
        renderThread = RenderThread(surface)
        renderThread?.start()
    }

    fun stop() {
        renderThread?.interrupt()
        renderThread?.join()
        renderThread = null
    }

    private class RenderThread(private val surface: Surface) : Thread() {
        private val renderIntervalMs = 1000L

        override fun run() {
            var value = 0

            while (!isInterrupted) {
                var canvas: Canvas? = null
                try {
                    canvas =
                        surface.lockHardwareCanvas()
                    draw(value++, canvas)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (canvas != null) {
                        surface.unlockCanvasAndPost(canvas)
                    }
                }

                try {
                    sleep(renderIntervalMs)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }

        private fun draw(value: Int, canvas: Canvas) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }

            // Draw a black circle
            val centerX = 100f
            val centerY = 100f
            val radius = 50f
            canvas.drawCircle(centerX, centerY, radius, paint)

            // Draw the white text inside the circle
            paint.apply {
                color = Color.WHITE
                textSize = 30f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("$value", centerX, centerY + paint.textSize / 3, paint)
        }
    }
}
