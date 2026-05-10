package com.kernelpanic.vertblock.repository

import com.kernelpanic.vertblock.database.WatchSessionDao

class StatsRepository(private val watchSessionDao: WatchSessionDao) {
    suspend fun getTotalWatchTimeSeconds(): Int {
        return watchSessionDao.getTotalWatchTime("youtube_shorts")
    }

    // Заглушки для вопросов (пока нет таблицы QuizResult)
    suspend fun getQuestionsAnsweredToday(): Int = 15
    suspend fun getFirstTryCorrectToday(): Int = 11
    suspend fun getTopicsTouchedToday(): Set<String> = emptySet()
    suspend fun getStreakDays(): Int = 5
}