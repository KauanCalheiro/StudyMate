package com.example.studymate.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.studymate.R
import com.example.studymate.ui.screen.pomodoro.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pomodoro_settings")

@AndroidEntryPoint
class PomodoroWidgetProvider : AppWidgetProvider() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val ACTION_START = "com.example.studymate.ACTION_START_POMODORO"
        const val ACTION_PAUSE = "com.example.studymate.ACTION_PAUSE_POMODORO"
        const val ACTION_RESET = "com.example.studymate.ACTION_RESET_POMODORO"
        const val DEFAULT_POMODORO_TIME = 25L * 60L // 25 minutes

        val TIMER_STATE = stringPreferencesKey("timer_state")
        val REMAINING_TIME = longPreferencesKey("remaining_time")

        fun updateWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, PomodoroWidgetProvider::class.java)
            )
            val intent = Intent(context, PomodoroWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_START -> handleStartAction(context)
            ACTION_PAUSE -> handlePauseAction(context)
            ACTION_RESET -> handleResetAction(context)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        scope.launch {
            val views = RemoteViews(context.packageName, R.layout.pomodoro_widget)

            // Get current timer state and remaining time
            val preferences = context.dataStore.data.first()
            val timerState = preferences[TIMER_STATE] ?: TimerState.Stopped.name
            val remainingTime = preferences[REMAINING_TIME] ?: 0L

            // Update timer text
            val minutes = remainingTime / 60
            val seconds = remainingTime % 60
            val timeText = String.format("%02d:%02d", minutes, seconds)
            views.setTextViewText(R.id.timer_text, timeText)

            // Set button click intents
            views.setOnClickPendingIntent(
                R.id.btn_start,
                getPendingIntent(context, ACTION_START)
            )
            views.setOnClickPendingIntent(
                R.id.btn_pause,
                getPendingIntent(context, ACTION_PAUSE)
            )
            views.setOnClickPendingIntent(
                R.id.btn_reset,
                getPendingIntent(context, ACTION_RESET)
            )

            // Update button visibility based on timer state
            when (timerState) {
                TimerState.Running.name -> {
                    views.setViewVisibility(R.id.btn_start, View.GONE)
                    views.setViewVisibility(R.id.btn_pause, View.VISIBLE)
                }
                else -> {
                    views.setViewVisibility(R.id.btn_start, View.VISIBLE)
                    views.setViewVisibility(R.id.btn_pause, View.GONE)
                }
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    private fun getPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, PomodoroWidgetProvider::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(), // Use action-specific request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun handleStartAction(context: Context) {
        val intent = Intent(context, PomodoroActionService::class.java).apply {
            action = ACTION_START
        }
        context.startService(intent)
    }

    private fun handlePauseAction(context: Context) {
        val intent = Intent(context, PomodoroActionService::class.java).apply {
            action = ACTION_PAUSE
        }
        context.startService(intent)
    }

    private fun handleResetAction(context: Context) {
        val intent = Intent(context, PomodoroActionService::class.java).apply {
            action = ACTION_RESET
        }
        context.startService(intent)
    }
} 