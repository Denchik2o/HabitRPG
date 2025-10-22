package com.example.habitrpg.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.habitrpg.data.local.converters.ListConverter
import com.example.habitrpg.data.model.Character
import com.example.habitrpg.data.model.InventoryItem
import com.example.habitrpg.data.model.Quest

@Database(
    entities = [Character::class, Quest::class, InventoryItem::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(ListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun questDao(): QuestDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "game_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}