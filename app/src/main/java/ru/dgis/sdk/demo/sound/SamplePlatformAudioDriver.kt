package ru.dgis.sdk.demo.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import ru.dgis.sdk.platform.AudioDriver
import ru.dgis.sdk.platform.AudioStreamReader
import ru.dgis.sdk.platform.AvailableCallback

class SamplePlatformAudioDriver : AudioDriver {
    private companion object {
        const val TAG = "PlatformAudioDriver"
        const val SAMPLE_RATE = 22050
    }

    private var reader: AudioStreamReader? = null
    private var callback: AvailableCallback? = null
    private val player = createPlayer()
    override fun setReader(reader: AudioStreamReader) {
        Log.i(TAG, "setReader")
        this.reader = reader
    }

    override fun setAvailableCallback(callback: AvailableCallback) {
        Log.i(TAG, "setAvailableCallback")
        this.callback = callback
    }

    override fun available(): Boolean {
        Log.i(TAG, "available")
        return true
    }

    override fun play() {
        Log.i(TAG, "play")
        val reader = this.reader ?: return
        player.play()
        do {
            val buffer = reader.read().toShortArray()
            player.write(buffer, 0, buffer.size)
        } while (buffer.isNotEmpty())
        player.stop()
    }

    private fun createPlayer(): AudioTrack {
        val systemBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        return AudioTrack(
            AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build(),
            AudioFormat.Builder()
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .build(),
            systemBufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }
}
