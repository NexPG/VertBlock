package com.kernelpanic.vertblock.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WatchSessionEntity::class], version = 1, exportSchema = false)
abstract class VertBlockDatabase : RoomDatabase() {
    abstract fun watchSessionDao(): WatchSessionDao
}