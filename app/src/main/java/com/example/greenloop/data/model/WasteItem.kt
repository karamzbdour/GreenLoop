package com.example.greenloop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waste_items")
data class WasteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val expiryDate: Long, // Timestamp
    val addedDate: Long = System.currentTimeMillis()
)
