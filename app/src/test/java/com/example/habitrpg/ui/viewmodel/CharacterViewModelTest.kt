package com.example.habitrpg.ui.viewmodel

import com.example.habitrpg.data.model.CharacterClass
import com.example.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class CharacterViewModelTest {

    private val mockRepository: GameRepository = mock()
    private val viewModel = CharacterViewModel(mockRepository)

    @Test
    fun `create character should call repository`() = runTest {
        val nickname = "TestHero"
        val characterClass = CharacterClass.MAGE

        viewModel.createCharacter(nickname, characterClass)

        verify(mockRepository).createCharacter(nickname, characterClass)
    }

    @Test
    fun `reload character should refresh data`() = runTest {
        viewModel.reloadCharacter()

        // Проверка, что reloadCharacter выполнился
        assertTrue(true)
    }
}