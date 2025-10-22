package com.example.habitrpg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitrpg.data.model.Quest
import com.example.habitrpg.data.model.QuestDifficulty
import com.example.habitrpg.data.model.WeekDay
import com.example.habitrpg.data.repository.GameRepository
import com.example.habitrpg.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class QuestViewModel(private val repository: GameRepository) : ViewModel() {

    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests: StateFlow<List<Quest>> = _quests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _characterDeathEvent = MutableStateFlow(false)
    val characterDeathEvent: StateFlow<Boolean> = _characterDeathEvent.asStateFlow()

    private val _lastMaintenanceDate = MutableStateFlow<Long?>(null)
    val lastMaintenanceDate: StateFlow<Long?> = _lastMaintenanceDate.asStateFlow()

    init {
        loadQuests()
        checkInitialCharacterState()
    }

    // Проверяем состояние персонажа при запуске приложения
    private fun checkInitialCharacterState() {
        viewModelScope.launch {
            val currentCharacter = repository.getCharacter().first()
            if (currentCharacter != null && repository.checkCharacterDeath(currentCharacter)) {
                _characterDeathEvent.value = true
            }
        }
    }

    private fun loadQuests() {
        viewModelScope.launch {
            repository.getAllQuests().collect { quests ->
                _quests.value = quests
            }
        }
    }

    // Методы для добавления разных типов задач
    fun addHabit(
        title: String,
        description: String,
        difficulty: QuestDifficulty
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addHabit(title, description, difficulty)
            _isLoading.value = false
        }
    }

    fun addDaily(
        title: String,
        description: String,
        difficulty: QuestDifficulty,
        weekDays: List<WeekDay>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addDaily(title, description, difficulty, weekDays)
            _isLoading.value = false
        }
    }

    fun addTask(
        title: String,
        description: String,
        difficulty: QuestDifficulty,
        deadline: Long?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addTask(title, description, difficulty, deadline)
            _isLoading.value = false
        }
    }

    // Методы для выполнения/провала задач
    fun completeQuest(quest: Quest, character: com.example.habitrpg.data.model.Character) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.completeQuest(quest, character)
            checkCharacterDeath()
            _isLoading.value = false
        }
    }

    fun failQuest(quest: Quest, character: com.example.habitrpg.data.model.Character) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.failQuest(quest, character)
            checkCharacterDeath()
            _isLoading.value = false
        }
    }

    // Методы для привычек
    fun incrementHabit(quest: Quest, character: com.example.habitrpg.data.model.Character) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.incrementHabit(quest, character)
            checkCharacterDeath()
            _isLoading.value = false
        }
    }

    fun decrementHabit(quest: Quest, character: com.example.habitrpg.data.model.Character) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.decrementHabit(quest, character)
            checkCharacterDeath()
            _isLoading.value = false
        }
    }

    fun deleteQuest(quest: Quest) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteQuest(quest)
            _isLoading.value = false
        }
    }

    // Метод для проверки смерти (используется после действий)
    fun checkCharacterDeath() {
        viewModelScope.launch {
            val currentCharacter = repository.getCharacter().first()
            if (currentCharacter != null && repository.checkCharacterDeath(currentCharacter)) {
                _characterDeathEvent.value = true
            }
        }
    }

    // Сброс персонажа при смерти с выбором нового класса
    fun resetCharacterOnDeathWithNewClass(characterClass: com.example.habitrpg.data.model.CharacterClass) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.resetCharacterOnDeathWithNewClass(characterClass)
            _characterDeathEvent.value = false
            _isLoading.value = false
        }
    }

    // Метод для проверки и обслуживания задач при запуске приложения
    fun performMaintenanceIfNeeded(character: com.example.habitrpg.data.model.Character) {
        viewModelScope.launch {
            _isLoading.value = true

            val currentDay = DateUtils.getStartOfDay()
            val lastMaintenance = _lastMaintenanceDate.value

            // Выполняем обслуживание если еще не выполняли сегодня
            if (lastMaintenance == null || lastMaintenance < currentDay) {
                repository.performDailyMaintenance(character)
                _lastMaintenanceDate.value = currentDay
            }

            _isLoading.value = false
        }
    }
}