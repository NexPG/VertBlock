package com.kernelpanic.vertblock.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface QuizResultDao {
    @Insert
    suspend fun insertResult(result: QuizResultEntity)

    @Query("SELECT * FROM quiz_results")
    suspend fun getAllResults(): List<QuizResultEntity>

    @Query("SELECT COUNT(*) FROM quiz_results WHERE attempts = 1")
    suspend fun getFirstTryCount(): Int
}