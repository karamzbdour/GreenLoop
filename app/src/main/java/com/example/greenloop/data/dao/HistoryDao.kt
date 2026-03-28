package com.example.greenloop.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.greenloop.data.model.UpcycleHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM upcycle_history ORDER BY completedDate DESC")
    fun getAllHistory(): Flow<List<UpcycleHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: UpcycleHistory)

    @Query("SELECT SUM(co2Saved) FROM upcycle_history")
    fun getTotalCo2Saved(): Flow<Double?>
}
