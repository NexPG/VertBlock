package com.kernelpanic.vertblock.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
// Убедись, что эти сущности импортированы правильно из твоего пакета
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
            // Если INSTANCE не null, возвращаем его.
            // Если null, создаем базу в синхронизированном блоке
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VertBlockDatabase::class.java,
                    "vertblock.db"
                )
                    // Добавим это, чтобы не было крашей при обновлении версии БД
                    .fallbackToDestructiveMigration()
                    .build()
                    "vertblock_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}