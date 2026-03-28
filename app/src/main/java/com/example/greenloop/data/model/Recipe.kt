package com.example.greenloop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val biowasteType: String,
    val steps: String, // Stored as a joined string or JSON
    val difficulty: String, // Easy, Medium, Hard
    val co2Saved: Double, // in kg
    val isFavorite: Boolean = false
)
