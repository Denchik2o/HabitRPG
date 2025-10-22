package com.example.habitrpg.data.local

import androidx.room.*
import com.example.habitrpg.data.model.Character
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM character WHERE id = 1")
    fun getCharacter(): Flow<Character?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCharacter(character: Character)

    @Update
    suspend fun updateCharacter(character: Character)
}