package com.kernelpanic.vertblock.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kernelpanic.vertblock.database.WatchSessionEntity
import com.kernelpanic.vertblock.database.QuizResultEntity

@Database(
    entities = [WatchSessionEntity::class, QuizResultEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VertBlockDatabase : RoomDatabase() {

    abstract fun watchSessionDao(): WatchSessionDao
    abstract fun quizResultDao(): QuizResultDao

    companion object {
        @Volatile
        private var INSTANCE: VertBlockDatabase? = null

        fun getDatabase(context: Context): VertBlockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VertBlockDatabase::class.java,
                    "vertblock.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}