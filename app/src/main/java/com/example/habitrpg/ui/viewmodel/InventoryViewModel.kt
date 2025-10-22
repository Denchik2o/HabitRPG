package com.example.habitrpg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitrpg.data.model.InventoryItem
import com.example.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryViewModel(private val repository: GameRepository) : ViewModel() {

    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()

    private val _equippedItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val equippedItems: StateFlow<List<InventoryItem>> = _equippedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInventory()
        loadEquippedItems()
    }

    private fun loadInventory() {
        viewModelScope.launch {
            repository.getAllInventoryItems().collect { items ->
                _inventoryItems.value = items
            }
        }
    }

    private fun loadEquippedItems() {
        viewModelScope.launch {
            repository.getEquippedItems().collect { items ->
                _equippedItems.value = items
            }
        }
    }

    fun equipItem(item: InventoryItem, character: com.example.habitrpg.data.model.Character) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.equipItem(item, character)
            _isLoading.value = false
        }
    }

    fun unequipItem(item: InventoryItem, character: com.example.habitrpg.data.model.Character) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.unequipItem(item, character)
            _isLoading.value = false
        }
    }

    fun useConsumable(item: InventoryItem, character: com.example.habitrpg.data.model.Character) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.useConsumable(item, character)
            _isLoading.value = false
        }
    }
}