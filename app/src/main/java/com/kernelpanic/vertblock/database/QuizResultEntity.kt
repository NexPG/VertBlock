package com.kernelpanic.vertblock.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val question: String,
    val correctAnswer: String,
    val userAnswer: String,
    val attempts: Int, // с какой попытки ответил (1, 2, 3, 4)
    val category: String, // тема (music, science, ...)
    val answeredAt: Long = System.currentTimeMillis()
)