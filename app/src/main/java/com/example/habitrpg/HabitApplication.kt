package com.example.habitrpg

import android.app.Application
import com.example.habitrpg.data.local.AppDatabase
import com.example.habitrpg.data.repository.GameRepository

class HabitApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        GameRepository(
            database.characterDao(),
            database.questDao(),
            database.inventoryDao(),
        )
    }
}