package com.example.studymate.data.repository

import com.example.studymate.data.dao.SubjectDao
import com.example.studymate.data.dao.TaskDao
import com.example.studymate.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class TaskRepository(
    private val taskDao: TaskDao,
    private val subjectDao: SubjectDao
) {
    suspend fun getUncompletedTasks(): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getUncompletedTasksByDueDate()
    }

    suspend fun getSubjectName(subjectId: Long): String? = withContext(Dispatchers.IO) {
        subjectDao.getSubjectById(subjectId)?.name
    }
} 