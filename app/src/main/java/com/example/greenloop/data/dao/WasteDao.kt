package com.example.greenloop.data.dao

import androidx.room.*
import com.example.greenloop.data.model.WasteItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteDao {
    @Query("SELECT * FROM waste_items ORDER BY expiryDate ASC")
    fun getAllItems(): Flow<List<WasteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WasteItem)

    @Delete
    suspend fun deleteItem(item: WasteItem)

    @Query("SELECT * FROM waste_items WHERE expiryDate <= :threshold")
    fun getExpiringSoon(threshold: Long): Flow<List<WasteItem>>
}
