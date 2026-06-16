package com.andyl.iris.domain.model

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Downloading(val progress: Float, val current: Int, val total: Int) : DownloadStatus()
    object Success : DownloadStatus()
    data class Error(val message: String, val failedUrls: List<String>) : DownloadStatus()
}

data class DownloadTask(
    val id: String,
    val packName: String,
    val status: DownloadStatus,
    val timestamp: Long = System.currentTimeMillis()
)
