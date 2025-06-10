package com.example.studymate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class StudyMateApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_CLASSES,
                    "Classes",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for upcoming classes"
                },
                NotificationChannel(
                    CHANNEL_TASKS,
                    "Tasks",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for task deadlines"
                },
                NotificationChannel(
                    CHANNEL_POMODORO,
                    "Pomodoro",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for Pomodoro timer"
                }
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        const val CHANNEL_CLASSES = "channel_classes"
        const val CHANNEL_TASKS = "channel_tasks"
        const val CHANNEL_POMODORO = "channel_pomodoro"
    }
} 