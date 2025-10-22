package com.example.habitrpg.data.local

import androidx.room.*
import com.example.habitrpg.data.model.Quest
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quest ORDER BY createdAt DESC")
    fun getAllQuests(): Flow<List<Quest>>

    @Query("SELECT * FROM quest WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveQuests(): Flow<List<Quest>>

    @Insert
    suspend fun insertQuest(quest: Quest): Long

    @Update
    suspend fun updateQuest(quest: Quest)

    @Delete
    suspend fun deleteQuest(quest: Quest)

    @Query("DELETE FROM quest")
    suspend fun deleteAllQuests()
}