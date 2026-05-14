package com.kernelpanic.vertblock.repository

import com.kernelpanic.vertblock.StatItem
import com.kernelpanic.vertblock.database.DayOfWeekActivity
import com.kernelpanic.vertblock.database.QuizResultDao
import com.kernelpanic.vertblock.database.WatchSessionDao
import java.util.*

class StatsRepository(
    private val watchSessionDao: WatchSessionDao,
    private val quizResultDao: QuizResultDao
) {
    private val appName = "youtube_shorts"

    // -- Общее время (секунды) --
    suspend fun getTotalSeconds(): Int {
        return watchSessionDao.getTotalWatchTime(appName)
    }

    // -- Периоды в секундах --
    suspend fun getDailySeconds(): Int {
        val (start, end) = getDayRange()
        return watchSessionDao.getTotalWatchTimeForPeriod(appName, start, end) ?: 0
    }

    suspend fun getWeeklySeconds(): Int {
        val (start, end) = getWeekRange()
        return watchSessionDao.getTotalWatchTimeForPeriod(appName, start, end) ?: 0
    }

    suspend fun getMonthlySeconds(): Int {
        val (start, end) = getMonthRange()
        return watchSessionDao.getTotalWatchTimeForPeriod(appName, start, end) ?: 0
    }

    suspend fun getYearlySeconds(): Int {
        val (start, end) = getYearRange()
        return watchSessionDao.getTotalWatchTimeForPeriod(appName, start, end) ?: 0
    }

    // -- Распределение по дням недели (проценты) --
    suspend fun getWeeklyPercentages(): List<Float> {
        val dayStats = watchSessionDao.getWatchTimeByDayOfWeek(appName)
        if (dayStats.isEmpty()) return listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)

        val totalSeconds = dayStats.sumOf { it.totalSeconds }
        if (totalSeconds == 0) return listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)

        val sorted = dayStats.sortedBy { it.dayOfWeek }
        return sorted.map { (it.totalSeconds.toFloat() / totalSeconds) * 100f }
    }

    // -- Самый активный день --
    suspend fun getMostActiveDay(): String {
        val dayStats = watchSessionDao.getWatchTimeByDayOfWeek(appName)
        val best = dayStats.maxByOrNull { it.totalSeconds } ?: return "None"
        return dayOfWeekName(best.dayOfWeek)
    }

    suspend fun getMostActiveHours(): Float {
        val dayStats = watchSessionDao.getWatchTimeByDayOfWeek(appName)
        val best = dayStats.maxByOrNull { it.totalSeconds } ?: return 0f
        return best.totalSeconds / 3600f
    }

    // -- Вопросы --
    suspend fun getTotalAnswers(): Int = quizResultDao.getAllResults().size

    suspend fun getFirstTryCount(): Int = quizResultDao.getFirstTryCount()

    suspend fun getAttemptsBreakdown(): List<StatItem> {
        val all = quizResultDao.getAllResults()
        val counts = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0)
        all.forEach { result ->
            val attempt = result.attempts.coerceIn(1, 4)
            counts[attempt] = counts.getOrDefault(attempt, 0) + 1
        }
        return listOf(
            StatItem("1st try", counts[1] ?: 0),
            StatItem("2nd try", counts[2] ?: 0),
            StatItem("3rd try", counts[3] ?: 0),
            StatItem("4th try", counts[4] ?: 0)
        )
    }

    suspend fun getTopCategories(): List<StatItem> {
        val all = quizResultDao.getAllResults()
        return all.groupBy { it.category }
            .map { (category, results) -> StatItem(category, results.size) }
            .sortedByDescending { it.value }
            .take(8)
    }

    suspend fun getTodayTopics(): Set<String> {
        return quizResultDao.getAllResults().map { it.category }.toSet()
    }

    suspend fun getStreakDays(): Int {
        val results = quizResultDao.getAllResults()
        if (results.isEmpty()) return 0
        val sortedDays = results.map { it.answeredAt / (1000 * 60 * 60 * 24) }
            .distinct()
            .sortedDescending()
        var streak = 1
        for (i in 0 until sortedDays.size - 1) {
            if (sortedDays[i] - sortedDays[i + 1] == 1L) streak++
            else break
        }
        return streak
    }

    // --- Вспомогательные функции для периодов ---
    private fun getDayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val start = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val end = cal.timeInMillis
        return start to end
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }

    private fun getYearRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_YEAR, 1)
        val start = cal.timeInMillis
        cal.add(Calendar.YEAR, 1)
        val end = cal.timeInMillis
        return start to end
    }

    private fun dayOfWeekName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            0 -> "Sunday"
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            else -> "Unknown"
        }
    }
}