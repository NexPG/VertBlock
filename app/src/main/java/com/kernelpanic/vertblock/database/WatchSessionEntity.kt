package com.kernelpanic.vertblock.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_sessions")
data class WatchSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val startTime: Long,
    val endTime: Long? = null,
    val durationSeconds: Int = 0,   // теперь прошедшее время (elapsed)
    val remainingSeconds: Int = 0,  // для восстановления таймера
    val appName: String = "youtube_shorts"
)