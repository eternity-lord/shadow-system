package com.shadowsystem.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/** Generated brown-noise loop, played quietly during a focus session. Cancelling the coroutine stops it. */
object AmbientSound {
    private const val RATE = 22_050

    suspend fun play() = withContext(Dispatchers.IO) {
        val minBuf = AudioTrack.getMinBufferSize(RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBuf <= 0) return@withContext
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        try {
            track.setVolume(0.12f)
            track.play()
            val buf = ShortArray(RATE / 10)
            val random = java.util.Random()
            var level = 0.0
            while (isActive) {
                for (i in buf.indices) {
                    level = level * 0.995 + (random.nextDouble() * 2 - 1) * 0.05
                    buf[i] = (level.coerceIn(-1.0, 1.0) * 12_000).toInt().toShort()
                }
                track.write(buf, 0, buf.size)
            }
        } finally {
            track.stop()
            track.release()
        }
    }
}
