package com.example.studymate.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val professor: String?,
    val location: String,
    val color: Int,
    val notificationMinutesBefore: Int = 15,
    val position: Int = 0 // For custom ordering
) 