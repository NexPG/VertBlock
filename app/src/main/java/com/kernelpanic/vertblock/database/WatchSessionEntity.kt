package com.kernelpanic.vertblock.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_sessions")
data class WatchSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val startTime: Long,      // время начала сессии (System.currentTimeMillis())
    val endTime: Long? = null, // время окончания (если ещё не закончена – null)
    val durationSeconds: Int = 0, // итоговая продолжительность в секундах
    val appName: String = "youtube_shorts"
)