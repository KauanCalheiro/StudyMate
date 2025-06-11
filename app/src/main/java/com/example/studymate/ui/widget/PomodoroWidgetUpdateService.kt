package com.example.studymate.ui.widget

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.studymate.ui.screen.pomodoro.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PomodoroWidgetUpdateService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun startUpdates() {
        scope.launch {
            while (true) {
                val preferences = dataStore.data.first()
                val timerState = preferences[PomodoroWidgetProvider.TIMER_STATE] ?: TimerState.Stopped.name

                if (timerState == TimerState.Running.name) {
                    PomodoroWidgetProvider.updateWidgets(this@PomodoroWidgetUpdateService)
                } else {
                    stopSelf()
                    break
                }

                delay(1000) // Atualiza o widget a cada segundo
            }
        }
    }
} 