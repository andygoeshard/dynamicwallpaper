package com.andyl.iris.domain.helper

import android.content.Context
import java.io.File

/**
 * Single source of truth for what the live wallpaper should display.
 *
 * The live wallpaper service runs in a separate process (":wallpaper") while
 * the worker writes from the app process. SharedPreferences is cached per
 * process, so a plain file on disk is the simplest reliable hand-off.
 */
data class LiveTarget(
    val isVideo: Boolean,
    val path: String
)

object LiveTargetStore {

    private const val FILE_NAME = "live_target.txt"

    fun write(context: Context, target: LiveTarget?) {
        val file = File(context.filesDir, FILE_NAME)
        if (target == null) {
            file.delete()
            return
        }
        try {
            file.writeText("${if (target.isVideo) "video" else "image"}\n${target.path}")
        } catch (_: Exception) {
        }
    }

    fun read(context: Context): LiveTarget? {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return null
            val lines = file.readLines()
            if (lines.size < 2) return null
            val isVideo = lines[0] == "video"
            LiveTarget(isVideo = isVideo, path = lines[1])
        } catch (_: Exception) {
            null
        }
    }
}