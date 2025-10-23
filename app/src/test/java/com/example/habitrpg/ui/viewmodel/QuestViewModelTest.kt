package com.example.habitrpg.ui.viewmodel

import com.example.habitrpg.data.model.*
import com.example.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class QuestViewModelTest {

    private val mockRepository: GameRepository = mock()
    private val viewModel = QuestViewModel(mockRepository)

    @Test
    fun `add habit should call repository`() = runTest {
        val title = "Test Habit"
        val description = "Test Description"
        val difficulty = QuestDifficulty.MEDIUM

        viewModel.addHabit(title, description, difficulty)

        verify(mockRepository).addHabit(title, description, difficulty)
    }

    @Test
    fun `add daily should call repository`() = runTest {
        val title = "Test Daily"
        val description = "Test Description"
        val difficulty = QuestDifficulty.EASY
        val weekDays = listOf(WeekDay.MONDAY, WeekDay.WEDNESDAY, WeekDay.FRIDAY)

        viewModel.addDaily(title, description, difficulty, weekDays)

        verify(mockRepository).addDaily(title, description, difficulty, weekDays)
    }

    @Test
    fun `complete quest should call repository and check death`() = runTest {
        val character = Character(nickname = "Test")
        val quest = Quest(title = "Test Quest", questType = "TASK")
        whenever(mockRepository.completeQuest(quest, character)).thenReturn(character)

        viewModel.completeQuest(quest, character)

        verify(mockRepository).completeQuest(quest, character)
        verify(mockRepository).checkCharacterDeath(character)
    }

    @Test
    fun `delete quest should call repository`() = runTest {
        val quest = Quest(title = "Test Quest", questType = "TASK")

        viewModel.deleteQuest(quest)

        verify(mockRepository).deleteQuest(quest)
    }

    @Test
    fun `character death reset should call repository`() = runTest {
        val characterClass = CharacterClass.WARRIOR
        val resetCharacter = Character(nickname = "Reborn", characterClass = "WARRIOR")
        whenever(mockRepository.resetCharacterOnDeathWithNewClass(characterClass)).thenReturn(resetCharacter)

        viewModel.resetCharacterOnDeathWithNewClass(characterClass)

        verify(mockRepository).resetCharacterOnDeathWithNewClass(characterClass)
    }
}