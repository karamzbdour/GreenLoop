package com.example.greenloop.data.repository

import com.example.greenloop.data.dao.HistoryDao
import com.example.greenloop.data.model.UpcycleHistory
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<UpcycleHistory>> = historyDao.getAllHistory()
    val totalCo2Saved: Flow<Double?> = historyDao.getTotalCo2Saved()

    suspend fun insertHistory(history: UpcycleHistory) {
        historyDao.insertHistory(history)
    }
}
