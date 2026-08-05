package com.andyl.iris.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.andyl.iris.R

class SoundManager(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val clickId: Int = soundPool.load(context, R.raw.sfx_click, 1)
    private val confirmId: Int = soundPool.load(context, R.raw.sfx_confirm, 1)

    fun playClick() {
        soundPool.play(clickId, 1f, 1f, 1, 0, 1f)
    }

    fun playConfirm() {
        soundPool.play(confirmId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
