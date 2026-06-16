package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.DownloadTask
import kotlinx.coroutines.flow.StateFlow

interface DownloadRepository {
    val activeTasks: StateFlow<List<DownloadTask>>
    fun updateTask(task: DownloadTask)
    fun clearCompleted()
    fun removeTask(taskId: String)
}
