package com.example.studymate.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.studymate.data.dao.SubjectDao
import com.example.studymate.data.dao.SubjectScheduleDao
import com.example.studymate.data.dao.TaskDao
import com.example.studymate.util.NotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var subjectDao: SubjectDao

    @Inject
    lateinit var subjectScheduleDao: SubjectScheduleDao

    @Inject
    lateinit var taskDao: TaskDao

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                // Reschedule class notifications
                val subjects = subjectDao.getAllSubjects().first()
                subjects.forEach { subject ->
                    val schedules = subjectScheduleDao.getSchedulesForSubject(subject.id).first()
                    schedules.forEach { schedule ->
                        notificationScheduler.scheduleClassNotification(subject, schedule)
                    }
                }

                // Reschedule task notifications
                taskDao.getPendingTasks().first().forEach { task ->
                    notificationScheduler.scheduleTaskNotification(task)
                }
            }
        }
    }
} 