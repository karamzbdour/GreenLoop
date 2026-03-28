package com.example.greenloop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.greenloop.data.dao.HistoryDao
import com.example.greenloop.data.dao.RecipeDao
import com.example.greenloop.data.dao.WasteDao
import com.example.greenloop.data.model.Recipe
import com.example.greenloop.data.model.UpcycleHistory
import com.example.greenloop.data.model.WasteItem

@Database(
    entities = [WasteItem::class, Recipe::class, UpcycleHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wasteDao(): WasteDao
    abstract fun recipeDao(): RecipeDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "greenloop_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // This will run on a background thread by Room
                        INSTANCE?.let { database ->
                            DatabaseInitializer.populateDatabase(database)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
