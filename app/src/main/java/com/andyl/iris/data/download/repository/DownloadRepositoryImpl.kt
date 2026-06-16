package com.andyl.iris.data.download.repository

import com.andyl.iris.domain.model.DownloadStatus
import com.andyl.iris.domain.model.DownloadTask
import com.andyl.iris.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DownloadRepositoryImpl : DownloadRepository {
    private val _activeTasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    override val activeTasks: StateFlow<List<DownloadTask>> = _activeTasks.asStateFlow()

    override fun updateTask(task: DownloadTask) {
        _activeTasks.update { tasks ->
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                tasks.toMutableList().apply { set(index, task) }
            } else {
                tasks + task
            }
        }
    }

    override fun clearCompleted() {
        _activeTasks.update { tasks ->
            tasks.filter { it.status is DownloadStatus.Downloading }
        }
    }

    override fun removeTask(taskId: String) {
        _activeTasks.update { tasks ->
            tasks.filter { it.id != taskId }
        }
    }
}
