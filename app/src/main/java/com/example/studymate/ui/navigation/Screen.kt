package com.example.studymate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Calendar : Screen("calendar", Icons.Default.CalendarMonth, "Calendário")
    object Tasks : Screen("tasks", Icons.Default.CheckCircle, "Tarefas")
    object Pomodoro : Screen("pomodoro", Icons.Default.Timer, "Pomodoro")

    companion object {
        val bottomNavItems = listOf(Calendar, Tasks, Pomodoro)
    }
} 