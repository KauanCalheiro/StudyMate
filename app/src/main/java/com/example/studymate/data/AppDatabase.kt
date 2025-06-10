package com.example.studymate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.studymate.data.dao.PomodoroSessionDao
import com.example.studymate.data.dao.SubjectDao
import com.example.studymate.data.dao.SubjectScheduleDao
import com.example.studymate.data.dao.TaskDao
import com.example.studymate.data.model.PomodoroSession
import com.example.studymate.data.model.Subject
import com.example.studymate.data.model.SubjectSchedule
import com.example.studymate.data.model.Task
import com.example.studymate.data.util.Converters

@Database(
    entities = [
        Subject::class,
        SubjectSchedule::class,
        Task::class,
        PomodoroSession::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun subjectScheduleDao(): SubjectScheduleDao
    abstract fun taskDao(): TaskDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studymate_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 