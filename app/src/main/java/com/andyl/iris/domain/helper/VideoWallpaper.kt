package com.andyl.iris.domain.helper

private val VIDEO_EXTENSIONS = listOf("mp4", "webm", "mkv", "mov", "3gp", "m4v", "avi")

fun isVideoUri(uri: String?): Boolean {
    if (uri.isNullOrBlank()) return false
    val lower = uri.lowercase()
    if (VIDEO_EXTENSIONS.any { lower.endsWith(".$it") }) return true
    if (lower.startsWith("content://") && lower.contains("/video/")) return true
    return false
}
