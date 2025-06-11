package com.example.studymate.ui.screen.pomodoro

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studymate.ui.widget.PomodoroWidgetProvider
import com.example.studymate.ui.widget.dataStore
import kotlinx.coroutines.flow.map
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val timerState by remember {
        context.dataStore.data.map { preferences ->
            preferences[PomodoroWidgetProvider.TIMER_STATE] ?: TimerState.Stopped.name
        }
    }.collectAsState(initial = TimerState.Stopped.name)

    val remainingTime by remember {
        context.dataStore.data.map { preferences ->
            preferences[PomodoroWidgetProvider.REMAINING_TIME] ?: PomodoroWidgetProvider.DEFAULT_POMODORO_TIME
        }
    }.collectAsState(initial = PomodoroWidgetProvider.DEFAULT_POMODORO_TIME)

    val timerMode by viewModel.timerMode.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val pomodoroDuration by viewModel.pomodoroDuration.collectAsState()
    val shortBreakDuration by viewModel.shortBreakDuration.collectAsState()
    val longBreakDuration by viewModel.longBreakDuration.collectAsState()

    val progress = when (timerMode) {
        TimerMode.Pomodoro -> remainingTime.toFloat() / (pomodoroDuration * 60)
        TimerMode.ShortBreak -> remainingTime.toFloat() / (shortBreakDuration * 60)
        TimerMode.LongBreak -> remainingTime.toFloat() / (longBreakDuration * 60)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "Timer Progress"
    )

    if (showEditDialog) {
        EditTimerDialog(
            onDismiss = { viewModel.hideEditDialog() },
            onConfirm = { pomodoro, shortBreak, longBreak ->
                viewModel.updateDurations(pomodoro, shortBreak, longBreak)
                viewModel.hideEditDialog()
            },
            currentPomodoro = pomodoroDuration,
            currentShortBreak = shortBreakDuration,
            currentLongBreak = longBreakDuration
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pomodoro Timer") },
                actions = {
                    IconButton(onClick = { viewModel.showEditDialog() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar tempos")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mode Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimerModeButton(
                    text = "Pomodoro",
                    selected = timerMode == TimerMode.Pomodoro,
                    onClick = { viewModel.setTimerMode(TimerMode.Pomodoro) }
                )
                TimerModeButton(
                    text = "Pausa Curta",
                    selected = timerMode == TimerMode.ShortBreak,
                    onClick = { viewModel.setTimerMode(TimerMode.ShortBreak) }
                )
                TimerModeButton(
                    text = "Pausa Longa",
                    selected = timerMode == TimerMode.LongBreak,
                    onClick = { viewModel.setTimerMode(TimerMode.LongBreak) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timer Circle
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularTimer(
                    progress = animatedProgress,
                    remainingTime = remainingTime
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Control Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { viewModel.resetTimer() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reiniciar Timer")
                }

                FloatingActionButton(
                    onClick = {
                        when (timerState) {
                            TimerState.Running.name -> viewModel.pauseTimer()
                            else -> viewModel.startTimer()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        if (timerState == TimerState.Running.name) Icons.Default.Pause
                        else Icons.Default.PlayArrow,
                        contentDescription = if (timerState == TimerState.Running.name) "Pausar" else "Iniciar"
                    )
                }
            }
        }
    }
}

@Composable
fun TimerModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(text)
    }
}

@Composable
fun CircularTimer(
    progress: Float,
    remainingTime: Long
) {
    val minutes = remainingTime / 60
    val seconds = remainingTime % 60

    // Extract colors outside of Canvas
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background circle
            drawArc(
                color = surfaceVariantColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )

            // Progress arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )

            // Draw tick marks
            for (i in 0 until 60) {
                val angle = (i * 360f / 60f) * (PI / 180f)
                val radius = size.width / 2
                val tickLength = if (i % 5 == 0) 15.dp.toPx() else 8.dp.toPx()
                val startRadius = radius - tickLength
                val endRadius = radius

                val startX = (center.x + cos(angle) * startRadius).toFloat()
                val startY = (center.y + sin(angle) * startRadius).toFloat()
                val endX = (center.x + cos(angle) * endRadius).toFloat()
                val endY = (center.y + sin(angle) * endRadius).toFloat()

                drawLine(
                    color = surfaceVariantColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (i % 5 == 0) 2.dp.toPx() else 1.dp.toPx()
                )
            }
        }

        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
} 