package com.example.studymate.ui.widget

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.studymate.data.repository.PomodoroRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PomodoroActionService : Service() {
    @Inject
    lateinit var pomodoroRepository: PomodoroRepository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PomodoroWidgetProvider.ACTION_START -> pomodoroRepository.startTimer()
            PomodoroWidgetProvider.ACTION_PAUSE -> pomodoroRepository.pauseTimer()
            PomodoroWidgetProvider.ACTION_RESET -> pomodoroRepository.resetTimer()
        }
        stopSelf()
        return START_NOT_STICKY
    }
} 