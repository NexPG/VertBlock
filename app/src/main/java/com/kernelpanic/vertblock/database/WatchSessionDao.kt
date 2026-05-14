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

    // НОВЫЙ МЕТОД: сумма секунд за указанный период [startMillis, endMillis)
    @Query("""
        SELECT SUM(durationSeconds) 
        FROM watch_sessions 
        WHERE appName = :appName 
          AND endTime IS NOT NULL 
          AND startTime >= :startMillis 
          AND startTime < :endMillis
    """)
    suspend fun getTotalWatchTimeForPeriod(
        appName: String,
        startMillis: Long,
        endMillis: Long
    ): Int?

    // НОВЫЙ МЕТОД: сумма секунд по дням недели (0=Вс, 1=Пн, ..., 6=Сб)
    @Query("""
        SELECT CAST(strftime('%w', startTime / 1000, 'unixepoch') AS INTEGER) as dayOfWeek,
               SUM(durationSeconds) as totalSeconds
        FROM watch_sessions 
        WHERE appName = :appName AND endTime IS NOT NULL 
        GROUP BY dayOfWeek 
        ORDER BY dayOfWeek
    """)
    suspend fun getWatchTimeByDayOfWeek(appName: String): List<DayOfWeekActivity>
}

// Вспомогательный класс для результата запроса по дням недели
data class DayOfWeekActivity(
    val dayOfWeek: Int,
    val totalSeconds: Int
)