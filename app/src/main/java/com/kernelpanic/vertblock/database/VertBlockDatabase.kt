package com.kernelpanic.vertblock.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WatchSessionEntity::class, QuizResultEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VertBlockDatabase : RoomDatabase() {
    abstract fun watchSessionDao(): WatchSessionDao
    abstract fun quizResultDao(): QuizResultDao
}