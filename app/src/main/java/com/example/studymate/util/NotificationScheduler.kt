package com.example.studymate.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.SubjectSchedule
import com.example.studymate.data.model.Task
import com.example.studymate.receiver.AlarmReceiver
import com.example.studymate.receiver.AlarmReceiver.Companion.EXTRA_ID
import com.example.studymate.receiver.AlarmReceiver.Companion.EXTRA_MESSAGE
import com.example.studymate.receiver.AlarmReceiver.Companion.EXTRA_TITLE
import com.example.studymate.receiver.AlarmReceiver.Companion.EXTRA_TYPE
import com.example.studymate.receiver.AlarmReceiver.Companion.TYPE_CLASS
import com.example.studymate.receiver.AlarmReceiver.Companion.TYPE_POMODORO
import com.example.studymate.receiver.AlarmReceiver.Companion.TYPE_TASK
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleClassNotification(subject: Subject, schedule: SubjectSchedule) {
        val nextClassTime = getNextClassTime(schedule)
        val notificationTime = nextClassTime.minusMinutes(subject.notificationMinutesBefore.toLong())

        scheduleNotification(
            title = "Aula de ${subject.name}",
            message = "Sua aula de ${subject.name} começa em ${subject.notificationMinutesBefore} minutos na sala ${subject.location}",
            dateTime = notificationTime,
            id = schedule.id.toInt(),
            type = TYPE_CLASS
        )
    }

    fun scheduleTaskNotification(task: Task) {
        val notificationTime = task.dueDate.minusHours(1)
        if (notificationTime.isAfter(LocalDateTime.now())) {
            scheduleNotification(
                title = "Tarefa: ${task.name}",
                message = "Sua tarefa ${task.name} vence em 1 hora",
                dateTime = notificationTime,
                id = task.id.toInt(),
                type = TYPE_TASK
            )
        }
    }

    fun schedulePomodoroNotification(id: Int, dateTime: LocalDateTime, isBreak: Boolean) {
        scheduleNotification(
            title = if (isBreak) "Pausa Finalizada" else "Tempo Finalizado",
            message = if (isBreak) "Hora de voltar aos estudos!" else "Hora de fazer uma pausa!",
            dateTime = dateTime,
            id = id,
            type = TYPE_POMODORO
        )
    }

    fun cancelNotification(id: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleNotification(
        title: String,
        message: String,
        dateTime: LocalDateTime,
        id: Int,
        type: String
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_TYPE, type)
            putExtra(EXTRA_ID, id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = dateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
            pendingIntent
        )
    }

    private fun getNextClassTime(schedule: SubjectSchedule): LocalDateTime {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val classTime = schedule.startTime

        val nextClassDate = when {
            // If today is the class day and class hasn't started yet
            today.dayOfWeek == schedule.dayOfWeek && now.toLocalTime().isBefore(classTime) -> today
            // If today is before the class day this week
            today.dayOfWeek.value < schedule.dayOfWeek.value -> today.plusDays(
                (schedule.dayOfWeek.value - today.dayOfWeek.value).toLong()
            )
            // Otherwise, get next week's class
            else -> today.plusDays(
                (7 - today.dayOfWeek.value + schedule.dayOfWeek.value).toLong()
            )
        }

        return LocalDateTime.of(nextClassDate, classTime)
    }
} 