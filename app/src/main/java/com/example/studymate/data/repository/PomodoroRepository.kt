package com.example.studymate.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.studymate.ui.screen.pomodoro.TimerState
import com.example.studymate.ui.widget.PomodoroWidgetProvider
import com.example.studymate.ui.widget.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            // Check if already running
            val preferences = context.dataStore.data.first()
            if (preferences[PomodoroWidgetProvider.TIMER_STATE] == TimerState.Running.name) {
                return@launch
            }

            context.dataStore.edit { prefs ->
                prefs[PomodoroWidgetProvider.TIMER_STATE] = TimerState.Running.name
            }
            PomodoroWidgetProvider.updateWidgets(context)

            var lastUpdateTime = System.currentTimeMillis()
            
            while (true) {
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - lastUpdateTime
                
                if (elapsedTime >= 1000) {
                    val currentPrefs = context.dataStore.data.first()
                    val currentState = currentPrefs[PomodoroWidgetProvider.TIMER_STATE]
                    if (currentState != TimerState.Running.name) {
                        break
                    }

                    val remainingTime = currentPrefs[PomodoroWidgetProvider.REMAINING_TIME] ?: 0L
                    
                    if (remainingTime <= 0) {
                        context.dataStore.edit { prefs ->
                            prefs[PomodoroWidgetProvider.TIMER_STATE] = TimerState.Finished.name
                        }
                        PomodoroWidgetProvider.updateWidgets(context)
                        break
                    }

                    context.dataStore.edit { prefs ->
                        prefs[PomodoroWidgetProvider.REMAINING_TIME] = remainingTime - 1
                    }
                    PomodoroWidgetProvider.updateWidgets(context)
                    lastUpdateTime = currentTime
                }
                
                delay(100)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[PomodoroWidgetProvider.TIMER_STATE] = TimerState.Paused.name
            }
            PomodoroWidgetProvider.updateWidgets(context)
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[PomodoroWidgetProvider.TIMER_STATE] = TimerState.Stopped.name
                preferences[PomodoroWidgetProvider.REMAINING_TIME] = PomodoroWidgetProvider.DEFAULT_POMODORO_TIME
            }
            PomodoroWidgetProvider.updateWidgets(context)
        }
    }
} 