package com.kernelpanic.vertblock.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WatchSessionEntity::class], version = 1, exportSchema = false)
abstract class VertBlockDatabase : RoomDatabase() {
    abstract fun watchSessionDao(): WatchSessionDao

    companion object {
        @Volatile
        private var INSTANCE: VertBlockDatabase? = null

        fun getDatabase(context: Context): VertBlockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VertBlockDatabase::class.java,
                    "vertblock_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}