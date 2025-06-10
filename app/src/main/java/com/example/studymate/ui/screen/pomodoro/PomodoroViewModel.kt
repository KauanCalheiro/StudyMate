package com.example.studymate.ui.screen.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studymate.util.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Stopped)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _remainingTime = MutableStateFlow(DEFAULT_POMODORO_TIME)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

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

    private var timerJob: Job? = null

    fun startTimer() {
        if (_timerState.value == TimerState.Running) return

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _timerState.value = TimerState.Running
            while (_remainingTime.value > 0) {
                delay(1000)
                _remainingTime.value -= 1
            }
            if (_remainingTime.value <= 0) {
                _timerState.value = TimerState.Finished
                notifyTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.Paused
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.Stopped
        _remainingTime.value = getCurrentModeDuration()
    }

    fun setTimerMode(mode: TimerMode) {
        if (_timerState.value == TimerState.Running) {
            timerJob?.cancel()
        }
        _timerMode.value = mode
        resetTimer()
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
        
        // Reset timer with new duration if it's not running
        if (_timerState.value != TimerState.Running) {
            resetTimer()
        }
    }

    private fun getCurrentModeDuration(): Long {
        return when (_timerMode.value) {
            TimerMode.Pomodoro -> _pomodoroDuration.value * 60L
            TimerMode.ShortBreak -> _shortBreakDuration.value * 60L
            TimerMode.LongBreak -> _longBreakDuration.value * 60L
        }
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