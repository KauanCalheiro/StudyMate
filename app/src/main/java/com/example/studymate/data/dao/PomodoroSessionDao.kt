package com.example.studymate.data.dao

import androidx.room.*
import com.example.studymate.data.model.PomodoroSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface PomodoroSessionDao {
    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE startTime >= :startDate AND endTime <= :endDate
        ORDER BY startTime DESC
    """)
    fun getSessionsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<PomodoroSession>>

    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE taskId = :taskId
        ORDER BY startTime DESC
    """)
    fun getSessionsForTask(taskId: Long): Flow<List<PomodoroSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSession): Long

    @Update
    suspend fun updateSession(session: PomodoroSession)

    @Delete
    suspend fun deleteSession(session: PomodoroSession)

    @Query("""
        SELECT SUM(totalFocusTimeMinutes) 
        FROM pomodoro_sessions 
        WHERE startTime >= :startDate AND endTime <= :endDate
    """)
    suspend fun getTotalFocusTimeInRange(startDate: LocalDateTime, endDate: LocalDateTime): Int?

    @Query("""
        SELECT COUNT(*) 
        FROM pomodoro_sessions 
        WHERE startTime >= :startDate AND endTime <= :endDate
    """)
    suspend fun getCompletedSessionsCountInRange(startDate: LocalDateTime, endDate: LocalDateTime): Int
} 