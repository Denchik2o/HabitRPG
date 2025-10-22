package com.example.habitrpg.data.local

import androidx.room.*
import com.example.habitrpg.data.model.InventoryItem
import com.example.habitrpg.data.model.ItemType
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventoryitem")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventoryitem WHERE isEquipped = 1")
    fun getEquippedItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventoryitem WHERE itemType = :itemType AND isEquipped = 1")
    fun getEquippedItemByType(itemType: ItemType): Flow<InventoryItem?>

    @Insert
    suspend fun insertItem(item: InventoryItem): Long

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("DELETE FROM inventoryitem WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Long)
}