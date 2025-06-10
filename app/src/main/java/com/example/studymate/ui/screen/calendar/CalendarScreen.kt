package com.example.studymate.ui.screen.calendar

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.SubjectSchedule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val subjects by viewModel.subjectsForSelectedDay.collectAsState()
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<SubjectWithSchedules?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Horário Semanal") },
                actions = {
                    IconButton(onClick = { showAddSubjectDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Disciplina")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            WeekDaySelector(
                selectedDay = selectedDay,
                onDaySelected = viewModel::selectDay
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subjects) { subjectWithSchedules ->
                    SubjectCard(
                        subjectWithSchedules = subjectWithSchedules,
                        onSubjectClick = { selectedSubject = it }
                    )
                }

                if (subjects.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma aula agendada para ${selectedDay.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))}",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (showAddSubjectDialog) {
            AddSubjectDialog(
                onDismiss = { showAddSubjectDialog = false },
                onSubjectAdded = { subject, schedules ->
                    viewModel.addSubject(subject, schedules)
                    showAddSubjectDialog = false
                }
            )
        }

        selectedSubject?.let { subject ->
            EditSubjectDialog(
                subjectWithSchedules = subject,
                onDismiss = { selectedSubject = null },
                onSubjectUpdated = { updatedSubject, updatedSchedules ->
                    viewModel.updateSubject(updatedSubject, updatedSchedules)
                    selectedSubject = null
                },
                onSubjectDeleted = {
                    viewModel.deleteSubject(subject.subject)
                    selectedSubject = null
                }
            )
        }
    }
}

@Composable
fun WeekDaySelector(
    selectedDay: DayOfWeek,
    onDaySelected: (DayOfWeek) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DayOfWeek.values().forEach { day ->
            FilterChip(
                selected = selectedDay == day,
                onClick = { onDaySelected(day) },
                label = {
                    Text(day.getDisplayName(TextStyle.NARROW, Locale("pt", "BR")))
                }
            )
        }
    }
}

@Composable
fun SubjectCard(
    subjectWithSchedules: SubjectWithSchedules,
    onSubjectClick: (SubjectWithSchedules) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { onSubjectClick(subjectWithSchedules) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .background(Color(subjectWithSchedules.subject.color))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subjectWithSchedules.subject.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (subjectWithSchedules.subject.professor != null) {
                    Text(
                        text = subjectWithSchedules.subject.professor,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                subjectWithSchedules.schedules.forEach { schedule ->
                    Text(
                        text = "${schedule.startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))} - ${schedule.endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = subjectWithSchedules.subject.location,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubjectDialog(
    subjectWithSchedules: SubjectWithSchedules,
    onDismiss: () -> Unit,
    onSubjectUpdated: (Subject, List<SubjectSchedule>) -> Unit,
    onSubjectDeleted: () -> Unit
) {
    var name by remember { mutableStateOf(subjectWithSchedules.subject.name) }
    var professor by remember { mutableStateOf(subjectWithSchedules.subject.professor ?: "") }
    var location by remember { mutableStateOf(subjectWithSchedules.subject.location) }
    var color by remember { mutableStateOf(subjectWithSchedules.subject.color) }
    var schedules by remember { mutableStateOf(
        subjectWithSchedules.schedules.map { schedule ->
            ScheduleEntry(
                dayOfWeek = schedule.dayOfWeek,
                startTime = schedule.startTime.toString(),
                endTime = schedule.endTime.toString()
            )
        }
    )}
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirmar exclusão") },
            text = { Text("Deseja realmente excluir esta disciplina?") },
            confirmButton = {
                Button(
                    onClick = onSubjectDeleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Disciplina") },
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val updatedSubject = subjectWithSchedules.subject.copy(
                            name = name,
                            professor = professor.takeIf { it.isNotBlank() },
                            location = location,
                            color = color
                        )
                        val updatedSchedules = schedules.map { schedule ->
                            SubjectSchedule(
                                subjectId = subjectWithSchedules.subject.id,
                                dayOfWeek = schedule.dayOfWeek,
                                startTime = LocalTime.parse(schedule.startTime),
                                endTime = LocalTime.parse(schedule.endTime)
                            )
                        }
                        onSubjectUpdated(updatedSubject, updatedSchedules)
                    },
                    enabled = name.isNotBlank() && location.isNotBlank() && schedules.isNotEmpty()
                ) {
                    Text("Salvar")
                }
                
                Button(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Excluir")
                }
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