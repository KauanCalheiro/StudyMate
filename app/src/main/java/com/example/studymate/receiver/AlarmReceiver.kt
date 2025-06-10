package com.example.studymate.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.studymate.R
import com.example.studymate.StudyMateApplication.Companion.CHANNEL_POMODORO
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: return
        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        val id = intent.getIntExtra(EXTRA_ID, NOTIFICATION_ID)

        showNotification(context, id, title, message, type)
    }

    private fun showNotification(context: Context, id: Int, title: String, message: String, type: String) {
        val channelId = when (type) {
            TYPE_CLASS -> com.example.studymate.StudyMateApplication.CHANNEL_CLASSES
            TYPE_TASK -> com.example.studymate.StudyMateApplication.CHANNEL_TASKS
            TYPE_POMODORO -> CHANNEL_POMODORO
            else -> CHANNEL_POMODORO
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_ID = "extra_id"
        const val TYPE_CLASS = "type_class"
        const val TYPE_TASK = "type_task"
        const val TYPE_POMODORO = "type_pomodoro"
    }
} 