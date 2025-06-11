package com.example.studymate.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.example.studymate.data.AppDatabase
import com.example.studymate.data.repository.TaskRepository

class TaskWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val database = AppDatabase.getDatabase(applicationContext)
        val taskRepository = TaskRepository(
            taskDao = database.taskDao(),
            subjectDao = database.subjectDao()
        )
        return TaskRemoteViewsFactory(applicationContext, taskRepository)
    }
} 