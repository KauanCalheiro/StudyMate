package com.example.studymate.ui.screen.pomodoro

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studymate.data.repository.PomodoroRepository
import com.example.studymate.ui.widget.PomodoroWidgetProvider
import com.example.studymate.ui.widget.dataStore
import com.example.studymate.util.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val notificationScheduler: NotificationScheduler,
    private val pomodoroRepository: PomodoroRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _timerMode = MutableStateFlow<TimerMode>(TimerMode.Pomodoro)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    // Custom durations for each mode (in minutes)
    private val _pomodoroDuration = MutableStateFlow(25)
    val pomodoroDuration: StateFlow<Int> = _pomodoroDuration.asStateFlow()

    private val _shortBreakDuration = MutableStateFlow(5)
    val shortBreakDuration: StateFlow<Int> = _shortBreakDuration.asStateFlow()

    private val _longBreakDuration = MutableStateFlow(15)
    val longBreakDuration: StateFlow<Int> = _longBreakDuration.asStateFlow()

    init {
        viewModelScope.launch {
            val preferences = context.dataStore.data.first()
            val timerState = preferences[PomodoroWidgetProvider.TIMER_STATE] ?: TimerState.Stopped.name

            if (timerState == TimerState.Running.name) {
                startTimer()
            }
        }
    }

    fun startTimer() {
        pomodoroRepository.startTimer()
        viewModelScope.launch {
            val preferences = context.dataStore.data.first()
            if (preferences[PomodoroWidgetProvider.TIMER_STATE] == TimerState.Finished.name) {
                notifyTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        pomodoroRepository.pauseTimer()
    }

    fun resetTimer() {
        pomodoroRepository.resetTimer()
    }

    fun setTimerMode(mode: TimerMode) {
        viewModelScope.launch {
            if (getTimerState() == TimerState.Running) {
                pomodoroRepository.pauseTimer()
            }
            _timerMode.value = mode
            resetTimer()
        }
    }

    fun showEditDialog() {
        _showEditDialog.value = true
    }

    fun hideEditDialog() {
        _showEditDialog.value = false
    }

    fun updateDurations(pomodoro: Int, shortBreak: Int, longBreak: Int) {
        _pomodoroDuration.value = pomodoro.coerceIn(1, 60)
        _shortBreakDuration.value = shortBreak.coerceIn(1, 30)
        _longBreakDuration.value = longBreak.coerceIn(1, 45)
        
        viewModelScope.launch {
            if (getTimerState() != TimerState.Running) {
                resetTimer()
            }
        }
    }

    private fun getCurrentModeDuration(): Long {
        return when (_timerMode.value) {
            TimerMode.Pomodoro -> _pomodoroDuration.value * 60L
            TimerMode.ShortBreak -> _shortBreakDuration.value * 60L
            TimerMode.LongBreak -> _longBreakDuration.value * 60L
        }
    }

    private suspend fun getTimerState(): TimerState {
        val preferences = context.dataStore.data.first()
        return TimerState.valueOf(preferences[PomodoroWidgetProvider.TIMER_STATE] ?: TimerState.Stopped.name)
    }

    private fun notifyTimerFinished() {
        val isBreak = when (_timerMode.value) {
            TimerMode.ShortBreak, TimerMode.LongBreak -> true
            TimerMode.Pomodoro -> false
        }

        notificationScheduler.schedulePomodoroNotification(
            id = _timerMode.value.hashCode(),
            dateTime = LocalDateTime.now(),
            isBreak = isBreak
        )
    }

    companion object {
        private const val DEFAULT_POMODORO_TIME = 25L * 60L // 25 minutes
    }
}

enum class TimerState {
    Running,
    Paused,
    Stopped,
    Finished
}

enum class TimerMode {
    Pomodoro,
    ShortBreak,
    LongBreak
} 