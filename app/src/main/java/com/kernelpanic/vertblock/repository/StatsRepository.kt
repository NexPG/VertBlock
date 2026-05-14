package com.kernelpanic.vertblock.repository

import com.kernelpanic.vertblock.database.QuizResultDao
import com.kernelpanic.vertblock.database.WatchSessionDao
import com.kernelpanic.vertblock.StatItem  // импорт из QuestionStatsScreen

class StatsRepository(
    private val watchSessionDao: WatchSessionDao,
    private val quizResultDao: QuizResultDao
) {
    // -- Время просмотра --
    suspend fun getTotalWatchTimeSeconds(): Int {
        return watchSessionDao.getTotalWatchTime("youtube_shorts")
    }

    // -- Вопросы --
    suspend fun getTotalAnswers(): Int {
        return quizResultDao.getAllResults().size
    }

    suspend fun getFirstTryCount(): Int {
        return quizResultDao.getFirstTryCount()
    }

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

    suspend fun getTodaysTopics(): Set<String> {
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

    // --- Статистика времени (заглушки) ---
    suspend fun getDailyHours(): Float {
        val totalSeconds = getTotalWatchTimeSeconds()
        return totalSeconds / 3600f
    }

    suspend fun getWeeklyHours(): Float = getDailyHours() * 7f
    suspend fun getMonthlyHours(): Float = getDailyHours() * 30f
    suspend fun getYearlyHours(): Float = getDailyHours() * 365f

    suspend fun getWeeklyPercentages(): List<Float> {
        // Заглушка: равномерное распределение по дням недели
        return listOf(15f, 20f, 18f, 25f, 30f, 10f, 5f)
    }

    suspend fun getMostActiveDay(): String = "Friday"
    suspend fun getMostActiveHours(): Float = 5.4f
}