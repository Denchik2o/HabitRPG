package com.example.habitrpg.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habitrpg.data.local.AppDatabase
import com.example.habitrpg.data.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameRepositoryAndroidTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: GameRepository

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
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun create_and_retrieve_character_integration() = runBlocking {
        val nickname = "AndroidTestHero"
        val characterClass = CharacterClass.MAGE

        repository.createCharacter(nickname, characterClass)
        val character = repository.getCharacter().first()

        assertNotNull(character)
        assertEquals(nickname, character?.nickname)
        assertEquals(characterClass.name, character?.characterClass)
        assertEquals(characterClass.baseHp, character?.maxHp)
        assertEquals(characterClass.baseMp, character?.maxMp)
        assertEquals(characterClass.baseAttack, character?.attack)
        assertEquals(characterClass.baseDefense, character?.defense)
    }

    @Test
    fun complete_quest_should_reward_character_and_level_up() = runBlocking {
        repository.createCharacter("TestWarrior", CharacterClass.WARRIOR)
        val character = repository.getCharacter().first()!!

        val questId = repository.addTask(
            "Test Integration Quest",
            "Complete this integration test quest",
            QuestDifficulty.MEDIUM,
            null
        )
        val quest = repository.getAllQuests().first().find { it.id == questId }!!

        val updatedCharacter = repository.completeQuest(quest, character)

        assertEquals(character.exp + quest.expReward, updatedCharacter.exp)
        assertEquals(character.gold + quest.goldReward, updatedCharacter.gold)
        assertTrue(updatedCharacter.exp >= character.exp)

        val completedQuest = repository.getAllQuests().first().find { it.id == questId }!!
        assertTrue(completedQuest.isCompleted)
        assertFalse(completedQuest.isFailed)
    }

    @Test
    fun fail_quest_should_damage_character_and_mark_quest_failed() = runBlocking {
        repository.createCharacter("TestArcher", CharacterClass.ARCHER)
        val character = repository.getCharacter().first()!!

        val questId = repository.addTask(
            "Failed Integration Quest",
            "Fail this integration test quest",
            QuestDifficulty.HARD,
            null
        )
        val quest = repository.getAllQuests().first().find { it.id == questId }!!

        val updatedCharacter = repository.failQuest(quest, character)

        assertEquals(character.currentHp - quest.penaltyDamage, updatedCharacter.currentHp)

        val failedQuest = repository.getAllQuests().first().find { it.id == questId }!!
        assertTrue(failedQuest.isCompleted)
        assertTrue(failedQuest.isFailed)
    }

    @Test
    fun character_level_up_system_integration() = runBlocking {
        repository.createCharacter("LevelUpTest", CharacterClass.WARRIOR)
        var character = repository.getCharacter().first()!!
        val initialLevel = character.level

        val highExpQuest = Quest(
            title = "High EXP Quest",
            description = "Gives lots of EXP",
            questType = "TASK",
            difficulty = "EPIC",
            expReward = 500,
            goldReward = 100,
            penaltyDamage = 50
        )
        val questId = database.questDao().insertQuest(highExpQuest)
        val quest = database.questDao().getAllQuests().first().find { it.id == questId }!!

        character = repository.completeQuest(quest, character)

        assertTrue(character.level > initialLevel)
        assertTrue(character.maxHp > 150)
        assertTrue(character.maxMp > 30)
        assertTrue(character.attack > 15)
        assertTrue(character.defense > 15)
    }

    @Test
    fun shop_and_inventory_integration() = runBlocking {
        repository.createCharacter("ShopTest", CharacterClass.MAGE)
        var character = repository.getCharacter().first()!!
        val initialGold = character.gold

        val shopItems = repository.getShopItems()
        val affordableItem = shopItems.first { it.goldValue <= initialGold }

        val success = repository.buyItem(affordableItem, character)

        assertTrue(success)

        val updatedCharacter = repository.getCharacter().first()!!
        assertEquals(initialGold - affordableItem.goldValue, updatedCharacter.gold)

        val inventory = repository.getAllInventoryItems().first()
        assertTrue(inventory.any { it.name == affordableItem.name })
    }

    @Test
    fun daily_quest_system_integration() = runBlocking {
        repository.createCharacter("DailyTest", CharacterClass.WARRIOR)
        val character = repository.getCharacter().first()!!

        val dailyId = repository.addDaily(
            "Morning Routine",
            "Complete morning routine",
            QuestDifficulty.MEDIUM,
            listOf(WeekDay.MONDAY, WeekDay.TUESDAY, WeekDay.WEDNESDAY, WeekDay.THURSDAY, WeekDay.FRIDAY)
        )
        val daily = repository.getAllQuests().first().find { it.id == dailyId }!!

        val updatedCharacter = repository.completeQuest(daily, character)

        val completedDaily = repository.getAllQuests().first().find { it.id == dailyId }!!
        assertTrue(completedDaily.isCompleted)
        assertFalse(completedDaily.isFailed)
        assertEquals(character.exp + daily.expReward, updatedCharacter.exp)
    }

    @Test
    fun character_death_and_resurrection_system() = runBlocking {
        repository.createCharacter("DeathTest", CharacterClass.MAGE)
        var character = repository.getCharacter().first()!!

        val item1 = InventoryItem(
            name = "Magic Wand",
            description = "A magical wand",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.UNCOMMON,
            attackBonus = 5,
            mpBonus = 10,
            goldValue = 100
        )
        val item2 = InventoryItem(
            name = "Health Potion",
            description = "Restores health",
            itemType = ItemType.CONSUMABLE,
            rarity = ItemRarity.COMMON,
            hpBonus = 30,
            goldValue = 25,
            isConsumable = true,
            stackSize = 3
        )
        database.inventoryDao().insertItem(item1)
        database.inventoryDao().insertItem(item2)

        val initialInventory = repository.getAllInventoryItems().first()
        assertEquals(2, initialInventory.size)

        val lethalQuest = Quest(
            title = "Lethal Quest",
            description = "This will kill the character",
            questType = "TASK",
            difficulty = "EPIC",
            expReward = 100,
            goldReward = 50,
            penaltyDamage = 1000
        )
        val questId = database.questDao().insertQuest(lethalQuest)
        val quest = database.questDao().getAllQuests().first().find { it.id == questId }!!

        val deadCharacter = repository.failQuest(quest, character)
        assertTrue(repository.checkCharacterDeath(deadCharacter))

        val newClass = CharacterClass.ARCHER
        val resurrectedCharacter = repository.resetCharacterOnDeathWithNewClass(newClass)

        assertEquals(character.nickname, resurrectedCharacter.nickname)
        assertEquals(newClass.name, resurrectedCharacter.characterClass)
        assertEquals(1, resurrectedCharacter.level)
        assertEquals(0, resurrectedCharacter.exp)
        assertEquals(100, resurrectedCharacter.gold)

        val clearedInventory = repository.getAllInventoryItems().first()
        assertTrue(clearedInventory.isEmpty())

        assertEquals(resurrectedCharacter.maxHp, resurrectedCharacter.currentHp)
        assertFalse(repository.checkCharacterDeath(resurrectedCharacter))
    }

    @Test
    fun consumable_items_integration() = runBlocking {
        repository.createCharacter("PotionTest", CharacterClass.WARRIOR)
        var character = repository.getCharacter().first()!!
        val damagedCharacter = character.copy(currentHp = 30, currentMp = 10)
        repository.updateCharacter(damagedCharacter)

        val potion = InventoryItem(
            name = "Super Potion",
            description = "Restores HP and MP",
            itemType = ItemType.CONSUMABLE,
            rarity = ItemRarity.UNCOMMON,
            hpBonus = 50,
            mpBonus = 30,
            goldValue = 40,
            isConsumable = true,
            stackSize = 2
        )
        val potionId = database.inventoryDao().insertItem(potion)
        val savedPotion = potion.copy(id = potionId)

        val healedCharacter = repository.useConsumable(savedPotion, damagedCharacter)

        assertTrue(healedCharacter.currentHp > damagedCharacter.currentHp)
        assertTrue(healedCharacter.currentMp > damagedCharacter.currentMp)

        val updatedPotion = repository.getAllInventoryItems().first().find { it.id == potionId }
        assertEquals(1, updatedPotion?.stackSize)
    }

    @Test
    fun multiple_quest_types_integration() = runBlocking {
        repository.createCharacter("QuestTypeTest", CharacterClass.MAGE)

        val habitId = repository.addHabit("Read Books", "Read for 30 minutes", QuestDifficulty.EASY)
        val dailyId = repository.addDaily(
            "Meditate",
            "Meditate for 10 minutes",
            QuestDifficulty.MEDIUM,
            listOf(WeekDay.MONDAY, WeekDay.WEDNESDAY, WeekDay.FRIDAY)
        )
        val taskId = repository.addTask(
            "Learn Android",
            "Complete Android course",
            QuestDifficulty.HARD,
            System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
        )

        val allQuests = repository.getAllQuests().first()

        assertEquals(3, allQuests.size)

        val habit = allQuests.find { it.id == habitId }
        val daily = allQuests.find { it.id == dailyId }
        val task = allQuests.find { it.id == taskId }

        assertNotNull(habit)
        assertEquals("HABIT", habit?.questType)
        assertEquals(0, habit?.habitCounter)

        assertNotNull(daily)
        assertEquals("DAILY", daily?.questType)
        assertEquals(3, daily?.weekDays?.size)

        assertNotNull(task)
        assertEquals("TASK", task?.questType)
        assertNotNull(task?.deadline)
    }

    @Test
    fun stat_recalculation_after_equipment_changes() = runBlocking {
        repository.createCharacter("StatTest", CharacterClass.WARRIOR)
        val character = repository.getCharacter().first()!!
        val baseAttack = character.attack
        val baseDefense = character.defense

        val strongWeapon = InventoryItem(
            name = "Dragon Slayer",
            description = "Extremely powerful weapon",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.EPIC,
            requiredLevel = 1,
            attackBonus = 20,
            defenseBonus = 5,
            goldValue = 300
        )
        val strongArmor = InventoryItem(
            name = "Dragon Scale Mail",
            description = "Extremely powerful armor",
            itemType = ItemType.BREASTPLATE,
            rarity = ItemRarity.EPIC,
            requiredLevel = 1,
            attackBonus = 5,
            defenseBonus = 15,
            hpBonus = 50,
            goldValue = 400
        )

        val weaponId = database.inventoryDao().insertItem(strongWeapon)
        val armorId = database.inventoryDao().insertItem(strongArmor)

        var updatedCharacter = repository.equipItem(strongWeapon.copy(id = weaponId), character)
        updatedCharacter = repository.equipItem(strongArmor.copy(id = armorId), updatedCharacter)

        assertEquals(baseAttack + 25, updatedCharacter.attack)
        assertEquals(baseDefense + 20, updatedCharacter.defense)
        assertEquals(character.maxHp + 50, updatedCharacter.maxHp)

        val unequippedWeapon = strongWeapon.copy(id = weaponId, isEquipped = true)
        updatedCharacter = repository.unequipItem(unequippedWeapon, updatedCharacter)

        assertEquals(baseAttack + 5, updatedCharacter.attack)
        assertEquals(baseDefense + 15, updatedCharacter.defense)
    }
}