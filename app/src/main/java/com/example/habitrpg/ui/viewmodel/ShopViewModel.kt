package com.example.habitrpg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitrpg.data.model.InventoryItem
import com.example.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopViewModel(private val repository: GameRepository) : ViewModel() {

    private val _shopItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val shopItems: StateFlow<List<InventoryItem>> = _shopItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _purchaseResult = MutableStateFlow<PurchaseResult?>(null)
    val purchaseResult: StateFlow<PurchaseResult?> = _purchaseResult.asStateFlow()

    init {
        loadShopItems()
    }

    fun loadShopItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _shopItems.value = repository.getShopItems()
            _isLoading.value = false
        }
    }

    fun buyItem(item: InventoryItem, character: com.example.habitrpg.data.model.Character) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.buyItem(item, character)
            _purchaseResult.value = if (success) {
                PurchaseResult.Success("Предмет \"${item.name}\" куплен!")
            } else {
                PurchaseResult.Error("Недостаточно золота!")
            }
            _isLoading.value = false
        }
    }

    fun clearPurchaseResult() {
        _purchaseResult.value = null
    }
}

sealed class PurchaseResult {
    data class Success(val message: String) : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}