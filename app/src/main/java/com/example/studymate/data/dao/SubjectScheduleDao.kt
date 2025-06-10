package com.example.studymate.data.dao

import androidx.room.*
import com.example.studymate.data.model.SubjectSchedule
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface SubjectScheduleDao {
    @Query("SELECT * FROM subject_schedules WHERE subjectId = :subjectId")
    fun getSchedulesForSubject(subjectId: Long): Flow<List<SubjectSchedule>>

    @Query("SELECT * FROM subject_schedules WHERE dayOfWeek = :dayOfWeek ORDER BY startTime")
    fun getSchedulesForDay(dayOfWeek: DayOfWeek): Flow<List<SubjectSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: SubjectSchedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<SubjectSchedule>)

    @Update
    suspend fun updateSchedule(schedule: SubjectSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: SubjectSchedule)

    @Query("DELETE FROM subject_schedules WHERE subjectId = :subjectId")
    suspend fun deleteSchedulesForSubject(subjectId: Long)
} 