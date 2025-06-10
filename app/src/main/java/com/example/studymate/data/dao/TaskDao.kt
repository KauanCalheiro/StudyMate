package com.example.studymate.data.dao

import androidx.room.*
import com.example.studymate.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate")
    fun getPendingTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId AND isCompleted = 0 ORDER BY dueDate")
    fun getPendingTasksForSubject(subjectId: Long): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE isCompleted = 0 
        AND dueDate BETWEEN :startDate AND :endDate 
        ORDER BY dueDate
    """)
    fun getTasksInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Long, completed: Boolean)

    @Query("""
        SELECT * FROM tasks 
        WHERE isCompleted = 0 
        AND dueDate <= :date 
        ORDER BY dueDate LIMIT 5
    """)
    fun getUpcomingTasks(date: LocalDateTime = LocalDateTime.now()): Flow<List<Task>>
} 