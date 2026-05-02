package com.kernelpanic.vertblock.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WatchSessionDao {
    @Insert
    suspend fun insertSession(session: WatchSessionEntity): Long

    @Update
    suspend fun updateSession(session: WatchSessionEntity)

    @Query("SELECT * FROM watch_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): WatchSessionEntity?

    @Query("SELECT SUM(durationSeconds) FROM watch_sessions WHERE appName = :appName AND endTime IS NOT NULL")
    suspend fun getTotalWatchTime(appName: String): Int
}