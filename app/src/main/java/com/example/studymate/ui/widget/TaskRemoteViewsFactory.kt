package com.example.studymate.ui.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.studymate.R
import com.example.studymate.data.model.Priority
import com.example.studymate.data.model.Task
import com.example.studymate.data.repository.TaskRepository
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class TaskRemoteViewsFactory(
    private val context: Context,
    private val taskRepository: TaskRepository
) : RemoteViewsService.RemoteViewsFactory {

    private var tasks: List<Task> = emptyList()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM - HH:mm", Locale("pt", "BR"))

    override fun onCreate() {
        // Initialization is done in onDataSetChanged()
    }

    override fun onDataSetChanged() {
        runBlocking {
            tasks = taskRepository.getUncompletedTasks()
        }
    }

    override fun onDestroy() {
        tasks = emptyList()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position >= tasks.size) return null

        val task = tasks[position]
        val now = LocalDateTime.now()
        val daysUntilDue = ChronoUnit.DAYS.between(now, task.dueDate)
        
        return RemoteViews(context.packageName, R.layout.widget_task_item).apply {
            setTextViewText(R.id.task_name, task.name)
            
            // Formatar o texto de detalhes com a data e a disciplina
            val subject = runBlocking { taskRepository.getSubjectName(task.subjectId) }
            val details = buildString {
                append(task.dueDate.format(dateFormatter))
                if (subject != null) {
                    append(" • ")
                    append(subject)
                }
            }
            setTextViewText(R.id.task_details, details)
            
            // Definir o ícone baseado no prazo
            val iconResId = when {
                now.isAfter(task.dueDate) -> R.drawable.ic_task_late
                daysUntilDue <= 7 -> R.drawable.ic_task_warning
                else -> R.drawable.ic_task_ok
            }
            setImageViewResource(R.id.task_status_icon, iconResId)

            // Configurar o intent para clique
            val fillInIntent = Intent().apply {
                putExtra("task_id", task.id)
            }
            setOnClickFillInIntent(R.id.root_layout, fillInIntent)
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = tasks.getOrNull(position)?.id?.toLong() ?: position.toLong()

    override fun hasStableIds(): Boolean = true
} 