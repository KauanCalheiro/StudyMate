package com.example.studymate.ui.screen.pomodoro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (pomodoro: Int, shortBreak: Int, longBreak: Int) -> Unit,
    currentPomodoro: Int,
    currentShortBreak: Int,
    currentLongBreak: Int
) {
    var pomodoroMinutes by remember { mutableStateOf(currentPomodoro.toString()) }
    var shortBreakMinutes by remember { mutableStateOf(currentShortBreak.toString()) }
    var longBreakMinutes by remember { mutableStateOf(currentLongBreak.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Tempos") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = pomodoroMinutes,
                    onValueChange = { pomodoroMinutes = it.filter { char -> char.isDigit() } },
                    label = { Text("Pomodoro (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = shortBreakMinutes,
                    onValueChange = { shortBreakMinutes = it.filter { char -> char.isDigit() } },
                    label = { Text("Pausa Curta (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = longBreakMinutes,
                    onValueChange = { longBreakMinutes = it.filter { char -> char.isDigit() } },
                    label = { Text("Pausa Longa (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val pomodoro = pomodoroMinutes.toIntOrNull() ?: currentPomodoro
                    val shortBreak = shortBreakMinutes.toIntOrNull() ?: currentShortBreak
                    val longBreak = longBreakMinutes.toIntOrNull() ?: currentLongBreak
                    onConfirm(pomodoro, shortBreak, longBreak)
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
} 