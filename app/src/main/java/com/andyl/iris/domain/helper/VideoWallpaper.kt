package com.andyl.iris.domain.helper

// Every container/extension users realistically throw at us. Playback still
// depends on device codecs, but recognizing them routes them through the
// live-video pipeline instead of being treated as photos.
private val VIDEO_EXTENSIONS = listOf(
    "mp4", "m4v", "mov", "mkv", "webm", "3gp", "3g2", "avi",
    "wmv", "flv", "f4v", "mpg", "mpeg", "mpe", "m2v", "ts", "m2ts", "mts",
    "ogv", "asf", "divx", "vob", "rm", "rmvb"
)

fun isVideoUri(uri: String?): Boolean {
    if (uri.isNullOrBlank()) return false
    val lower = uri.lowercase()
    if (VIDEO_EXTENSIONS.any { lower.endsWith(".$it") }) return true
    if (lower.startsWith("content://") && lower.contains("/video/")) return true
    return false
}
