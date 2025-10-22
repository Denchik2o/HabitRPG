package com.example.habitrpg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitrpg.data.model.Character
import com.example.habitrpg.data.model.CharacterClass
import com.example.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterViewModel(private val repository: GameRepository) : ViewModel() {

    private val _character = MutableStateFlow<Character?>(null)
    val character: StateFlow<Character?> = _character.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCharacter()
    }

    private fun loadCharacter() {
        viewModelScope.launch {
            repository.getCharacter().collect { character ->
                _character.value = character
            }
        }
    }

    fun createCharacter(nickname: String, characterClass: CharacterClass) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createCharacter(nickname, characterClass)
            _isLoading.value = false
        }
    }

    fun reloadCharacter() {
        viewModelScope.launch {
            loadCharacter()
        }
    }
}