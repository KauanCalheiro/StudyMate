package com.example.studymate.ui.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studymate.data.dao.SubjectDao
import com.example.studymate.data.dao.SubjectScheduleDao
import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.SubjectSchedule
import com.example.studymate.util.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

data class SubjectWithSchedules(
    val subject: Subject,
    val schedules: List<SubjectSchedule>
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val subjectDao: SubjectDao,
    private val subjectScheduleDao: SubjectScheduleDao,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(DayOfWeek.MONDAY)
    val selectedDay: StateFlow<DayOfWeek> = _selectedDay

    val subjectsForSelectedDay = _selectedDay
        .flatMapLatest { day ->
            subjectScheduleDao.getSchedulesForDay(day)
                .combine(subjectDao.getAllSubjects()) { schedules, subjects ->
                    val subjectMap = subjects.associateBy { it.id }
                    schedules.mapNotNull { schedule ->
                        subjectMap[schedule.subjectId]?.let { subject ->
                            SubjectWithSchedules(subject, listOf(schedule))
                        }
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDay(day: DayOfWeek) {
        _selectedDay.value = day
    }

    fun addSubject(subject: Subject, schedules: List<SubjectSchedule>) {
        viewModelScope.launch {
            val subjectId = subjectDao.insertSubject(subject)
            val schedulesWithSubjectId = schedules.map { it.copy(subjectId = subjectId) }
            subjectScheduleDao.insertSchedules(schedulesWithSubjectId)
            
            // Schedule notifications for each time slot
            schedulesWithSubjectId.forEach { schedule ->
                notificationScheduler.scheduleClassNotification(subject.copy(id = subjectId), schedule)
            }
        }
    }

    fun updateSubject(subject: Subject, schedules: List<SubjectSchedule>) {
        viewModelScope.launch {
            subjectDao.updateSubject(subject)
            subjectScheduleDao.deleteSchedulesForSubject(subject.id)
            val schedulesWithSubjectId = schedules.map { it.copy(subjectId = subject.id) }
            subjectScheduleDao.insertSchedules(schedulesWithSubjectId)
            
            // Reschedule notifications
            schedulesWithSubjectId.forEach { schedule ->
                notificationScheduler.scheduleClassNotification(subject, schedule)
            }
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            subjectDao.deleteSubject(subject)
            // Schedules will be deleted automatically due to CASCADE
        }
    }
} 