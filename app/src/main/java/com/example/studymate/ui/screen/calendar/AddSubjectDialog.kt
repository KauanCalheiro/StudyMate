package com.example.studymate.ui.screen.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.SubjectSchedule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.*

data class ScheduleEntry(
    val dayOfWeek: DayOfWeek,
    val startTime: String,
    val endTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onSubjectAdded: (Subject, List<SubjectSchedule>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var professor by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(Color.Blue.hashCode()) }
    var schedules by remember { mutableStateOf(listOf<ScheduleEntry>()) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Disciplina") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome da Disciplina") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = professor,
                        onValueChange = { professor = it },
                        label = { Text("Professor (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Local") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Horários", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(schedules) { schedule ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = schedule.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR")),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${schedule.startTime} - ${schedule.endTime}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(
                                onClick = {
                                    schedules = schedules.filter { it != schedule }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover horário")
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { showAddScheduleDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adicionar Horário")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val subject = Subject(
                        name = name,
                        professor = professor.takeIf { it.isNotBlank() },
                        location = location,
                        color = color
                    )
                    val subjectSchedules = schedules.map { schedule ->
                        SubjectSchedule(
                            subjectId = 0, // Will be updated after subject is inserted
                            dayOfWeek = schedule.dayOfWeek,
                            startTime = LocalTime.parse(schedule.startTime),
                            endTime = LocalTime.parse(schedule.endTime)
                        )
                    }
                    onSubjectAdded(subject, subjectSchedules)
                },
                enabled = name.isNotBlank() && location.isNotBlank() && schedules.isNotEmpty()
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showAddScheduleDialog) {
        AddScheduleDialog(
            onDismiss = { showAddScheduleDialog = false },
            onScheduleAdded = { dayOfWeek, startTime, endTime ->
                schedules = schedules + ScheduleEntry(dayOfWeek, startTime, endTime)
                showAddScheduleDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onScheduleAdded: (DayOfWeek, String, String) -> Unit
) {
    var selectedDay by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("09:30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Horário") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Dia da Semana", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DayOfWeek.values().forEach { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            label = { 
                                Text(day.getDisplayName(TextStyle.NARROW, Locale("pt", "BR")))
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Início") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Fim") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onScheduleAdded(selectedDay, startTime, endTime) },
                enabled = startTime.isNotBlank() && endTime.isNotBlank()
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
} 