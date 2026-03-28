package com.example.greenloop.data.repository

import com.example.greenloop.data.dao.WasteDao
import com.example.greenloop.data.model.WasteItem
import kotlinx.coroutines.flow.Flow

class WasteRepository(private val wasteDao: WasteDao) {
    val allItems: Flow<List<WasteItem>> = wasteDao.getAllItems()

    fun getExpiringSoon(threshold: Long): Flow<List<WasteItem>> {
        return wasteDao.getExpiringSoon(threshold)
    }

    suspend fun insertItem(item: WasteItem) {
        wasteDao.insertItem(item)
    }

    suspend fun deleteItem(item: WasteItem) {
        wasteDao.deleteItem(item)
    }
}
