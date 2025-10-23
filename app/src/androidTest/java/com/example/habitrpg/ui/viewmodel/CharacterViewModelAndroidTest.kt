package com.example.habitrpg.ui.viewmodel

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habitrpg.data.local.AppDatabase
import com.example.habitrpg.data.model.CharacterClass
import com.example.habitrpg.data.model.QuestDifficulty
import com.example.habitrpg.data.model.WeekDay
import com.example.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuestViewModelAndroidTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: GameRepository
    private lateinit var viewModel: QuestViewModel

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = GameRepository(
            database.characterDao(),
            database.questDao(),
            database.inventoryDao()
        )
        viewModel = QuestViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addHabit_should_add_habit_to_quests_flow() = runBlocking {
        repository.createCharacter("QuestTest", CharacterClass.WARRIOR)
        val title = "Test Habit"
        val description = "Test Habit Description"
        val difficulty = QuestDifficulty.MEDIUM

        viewModel.addHabit(title, description, difficulty)

        val quests = viewModel.quests.first { it.any { quest -> quest.title == title } }
        val addedHabit = quests.find { it.title == title }

        assertNotNull(addedHabit)
        assertEquals("HABIT", addedHabit?.questType)
        assertEquals(difficulty.name, addedHabit?.difficulty)
    }

    @Test
    fun completeQuest_should_update_character_and_quest() = runBlocking {
        repository.createCharacter("CompleteTest", CharacterClass.ARCHER)
        val character = repository.getCharacter().first()!!

        val questId = repository.addTask("Test Completion", "Test quest completion", QuestDifficulty.EASY, null)
        val quest = repository.getAllQuests().first().find { it.id == questId }!!

        viewModel.completeQuest(quest, character)

        val updatedQuests = viewModel.quests.first { it.any { q -> q.id == questId && q.isCompleted } }
        val completedQuest = updatedQuests.find { it.id == questId }

        assertTrue(completedQuest?.isCompleted == true)
        assertFalse(completedQuest?.isFailed == true)

        val updatedCharacter = repository.getCharacter().first()!!
        assertTrue(updatedCharacter.exp > character.exp)
    }

    @Test
    fun character_death_detection_should_work() = runBlocking {
        repository.createCharacter("DeathDetectionTest", CharacterClass.MAGE)
        val character = repository.getCharacter().first()!!

        val lethalQuestId = repository.addTask("Lethal Quest", "This will kill you", QuestDifficulty.EPIC, null)
        val lethalQuest = repository.getAllQuests().first().find { it.id == lethalQuestId }!!

        repository.failQuest(lethalQuest, character.copy(currentHp = 5))

        viewModel.checkCharacterDeath()

        val deathDetected = viewModel.characterDeathEvent.first { it }

        assertTrue(deathDetected)
    }

    @Test
    fun resetCharacterOnDeath_should_create_new_character() = runBlocking {
        repository.createCharacter("ResurrectionTest", CharacterClass.WARRIOR)
        var character = repository.getCharacter().first()!!

        character = character.copy(currentHp = 0)
        repository.updateCharacter(character)

        viewModel.checkCharacterDeath()

        val initialDeathState = viewModel.characterDeathEvent.first { it }
        assertTrue(initialDeathState)

        viewModel.resetCharacterOnDeathWithNewClass(CharacterClass.ARCHER)

        val deathEventReset = viewModel.characterDeathEvent.first { !it }

        assertFalse(deathEventReset)

        val resurrectedCharacter = repository.getCharacter().first()!!
        assertEquals("ResurrectionTest", resurrectedCharacter.nickname)
        assertEquals("ARCHER", resurrectedCharacter.characterClass)
        assertEquals(1, resurrectedCharacter.level)
        assertEquals(resurrectedCharacter.maxHp, resurrectedCharacter.currentHp)
    }

    @Test
    fun quests_flow_should_update_when_new_quests_are_added() = runBlocking {
        val initialQuests = viewModel.quests.first()

        viewModel.addHabit("New Habit", "A new habit", QuestDifficulty.EASY)
        viewModel.addDaily("New Daily", "A new daily", QuestDifficulty.MEDIUM, listOf(WeekDay.MONDAY))
        viewModel.addTask("New Task", "A new task", QuestDifficulty.HARD, null)

        val updatedQuests = viewModel.quests.first {
            it.size >= initialQuests.size + 3 &&
                    it.any { quest -> quest.questType == "HABIT" } &&
                    it.any { quest -> quest.questType == "DAILY" } &&
                    it.any { quest -> quest.questType == "TASK" }
        }

        assertTrue(updatedQuests.size > initialQuests.size)
        assertTrue(updatedQuests.any { it.questType == "HABIT" })
        assertTrue(updatedQuests.any { it.questType == "DAILY" })
        assertTrue(updatedQuests.any { it.questType == "TASK" })
    }

    @Test
    fun deleteQuest_should_remove_quest_from_flow() = runBlocking {
        val questId = repository.addTask("To Delete", "This will be deleted", QuestDifficulty.EASY, null)

        var quests = viewModel.quests.first { it.any { quest -> quest.id == questId } }
        val questToDelete = quests.find { it.id == questId }
        assertNotNull("Quest should exist before deletion", questToDelete)

        viewModel.deleteQuest(questToDelete!!)

        quests = viewModel.quests.first { it.none { quest -> quest.id == questId } }

        assertNull(quests.find { it.id == questId })
    }

    @Test
    fun failQuest_should_damage_character_and_mark_quest_failed() = runBlocking {
        repository.createCharacter("FailTest", CharacterClass.WARRIOR)
        val character = repository.getCharacter().first()!!
        val initialHp = character.currentHp

        val questId = repository.addTask("Fail Quest", "This quest will be failed", QuestDifficulty.MEDIUM, null)
        val quest = repository.getAllQuests().first().find { it.id == questId }!!

        viewModel.failQuest(quest, character)

        val updatedQuests = viewModel.quests.first { it.any { q -> q.id == questId && q.isFailed } }
        val failedQuest = updatedQuests.find { it.id == questId }

        assertTrue(failedQuest?.isFailed == true)
        assertTrue(failedQuest?.isCompleted == true)

        val updatedCharacter = repository.getCharacter().first()!!
        assertTrue(updatedCharacter.currentHp < initialHp)
    }

    @Test
    fun habit_increment_should_increase_counter_and_give_rewards() = runBlocking {
        repository.createCharacter("HabitTest", CharacterClass.ARCHER)
        val character = repository.getCharacter().first()!!
        val initialExp = character.exp
        val initialGold = character.gold

        val habitId = repository.addHabit("Test Habit Counter", "Test habit counter", QuestDifficulty.EASY)
        val habit = repository.getAllQuests().first().find { it.id == habitId }!!
        val initialCounter = habit.habitCounter

        viewModel.incrementHabit(habit, character)

        val updatedQuests = viewModel.quests.first { it.any { q -> q.id == habitId && q.habitCounter > initialCounter } }
        val updatedHabit = updatedQuests.find { it.id == habitId }

        assertEquals(initialCounter + 1, updatedHabit?.habitCounter)

        val updatedCharacter = repository.getCharacter().first()!!
        assertTrue(updatedCharacter.exp > initialExp)
        assertTrue(updatedCharacter.gold > initialGold)
    }

    @Test
    fun habit_decrement_should_decrease_counter_and_apply_damage() = runBlocking {
        repository.createCharacter("HabitDecrementTest", CharacterClass.MAGE)
        val character = repository.getCharacter().first()!!
        val initialHp = character.currentHp

        val habitId = repository.addHabit("Test Habit Decrement", "Test habit decrement", QuestDifficulty.MEDIUM)
        val habit = repository.getAllQuests().first().find { it.id == habitId }!!

        repository.incrementHabit(habit, character)
        val habitAfterIncrement = repository.getAllQuests().first().find { it.id == habitId }!!

        viewModel.decrementHabit(habitAfterIncrement, character)

        val updatedQuests = viewModel.quests.first { it.any { q -> q.id == habitId && q.habitCounter < habitAfterIncrement.habitCounter } }
        val updatedHabit = updatedQuests.find { it.id == habitId }

        assertEquals(habitAfterIncrement.habitCounter - 1, updatedHabit?.habitCounter)

        val updatedCharacter = repository.getCharacter().first()!!
        assertTrue(updatedCharacter.currentHp < initialHp)
    }
}