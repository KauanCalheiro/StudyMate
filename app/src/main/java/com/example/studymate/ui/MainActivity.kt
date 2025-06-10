package com.example.studymate.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studymate.ui.navigation.Screen
import com.example.studymate.ui.navigation.StudyMateBottomNavigation
import com.example.studymate.ui.screen.calendar.CalendarScreen
import com.example.studymate.ui.screen.pomodoro.PomodoroScreen
import com.example.studymate.ui.screen.tasks.TasksScreen
import com.example.studymate.ui.theme.StudyMateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyMateTheme {
                StudyMateApp()
            }
        }
    }
}

@Composable
fun StudyMateApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            StudyMateBottomNavigation(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Calendar.route) {
                CalendarScreen()
            }
            composable(Screen.Tasks.route) {
                TasksScreen()
            }
            composable(Screen.Pomodoro.route) {
                PomodoroScreen()
            }
        }
    }
} 