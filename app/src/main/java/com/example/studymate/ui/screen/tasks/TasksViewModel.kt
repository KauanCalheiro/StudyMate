package com.example.studymate.ui.screen.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studymate.data.dao.SubjectDao
import com.example.studymate.data.dao.TaskDao
import com.example.studymate.data.model.Task
import com.example.studymate.util.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val subjectDao: SubjectDao,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _filterSubjectId = MutableStateFlow<Long?>(null)
    private val _filterDateRange = MutableStateFlow<Pair<LocalDateTime, LocalDateTime>?>(null)
    private val _showCompleted = MutableStateFlow(false)
    val showCompleted = _showCompleted.asStateFlow()

    val subjects = subjectDao.getAllSubjects().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tasks = combine(
        _filterSubjectId,
        _filterDateRange,
        _showCompleted,
        taskDao.getAllTasks()
    ) { subjectId, dateRange, showCompleted, allTasks ->
        var filteredTasks = if (showCompleted) {
            allTasks
        } else {
            allTasks.filter { !it.isCompleted }
        }

        if (subjectId != null) {
            filteredTasks = filteredTasks.filter { it.subjectId == subjectId }
        }

        if (dateRange != null) {
            filteredTasks = filteredTasks.filter {
                it.dueDate.isAfter(dateRange.first) && it.dueDate.isBefore(dateRange.second)
            }
        }

        filteredTasks
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addTask(task: Task) {
        viewModelScope.launch {
            val id = taskDao.insertTask(task)
            notificationScheduler.scheduleTaskNotification(task.copy(id = id))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task)
            if (!task.isCompleted) {
                notificationScheduler.scheduleTaskNotification(task)
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            taskDao.updateTask(updatedTask)
            if (!task.isCompleted) {
                notificationScheduler.cancelNotification(task.id.toInt())
            }
        }
    }

    fun applyFilter(subjectId: Long?, dateRange: Pair<LocalDateTime, LocalDateTime>?) {
        _filterSubjectId.value = subjectId
        _filterDateRange.value = dateRange
    }

    fun clearFilters() {
        _filterSubjectId.value = null
        _filterDateRange.value = null
    }

    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
    }

    fun getShowCompleted(): Boolean = _showCompleted.value

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
            notificationScheduler.cancelNotification(task.id.toInt())
        }
    }
} 