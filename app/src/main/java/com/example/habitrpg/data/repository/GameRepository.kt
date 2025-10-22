package com.example.habitrpg.data.repository

import com.example.habitrpg.data.local.CharacterDao
import com.example.habitrpg.data.local.InventoryDao
import com.example.habitrpg.data.local.QuestDao
import com.example.habitrpg.data.model.Character
import com.example.habitrpg.data.model.CharacterClass
import com.example.habitrpg.data.model.InventoryItem
import com.example.habitrpg.data.model.ItemType
import com.example.habitrpg.data.model.Quest
import com.example.habitrpg.data.model.QuestDifficulty
import com.example.habitrpg.data.model.WeekDay
import com.example.habitrpg.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class GameRepository(
    private val characterDao: CharacterDao,
    private val questDao: QuestDao,
    private val inventoryDao: InventoryDao
) {
    // методы Character
    fun getCharacter(): Flow<Character?> = characterDao.getCharacter()

    suspend fun createCharacter(nickname: String, characterClass: CharacterClass) {
        val character = Character(
            nickname = nickname,
            characterClass = characterClass.name,
            maxHp = characterClass.baseHp,
            currentHp = characterClass.baseHp,
            maxMp = characterClass.baseMp,
            currentMp = characterClass.baseMp,
            attack = characterClass.baseAttack,
            defense = characterClass.baseDefense
        )
        characterDao.saveCharacter(character)
    }

    suspend fun updateCharacter(character: Character) {
        characterDao.updateCharacter(character)
    }

    // Quest methods
    suspend fun addHabit(
        title: String,
        description: String,
        difficulty: QuestDifficulty
    ): Long {
        val quest = Quest(
            title = title,
            description = description,
            questType = "HABIT",
            difficulty = difficulty.name,
            expReward = difficulty.expReward,
            goldReward = difficulty.goldReward,
            penaltyDamage = difficulty.penaltyDamage,
            habitCounter = 0
        )
        return questDao.insertQuest(quest)
    }

    suspend fun addDaily(
        title: String,
        description: String,
        difficulty: QuestDifficulty,
        weekDays: List<WeekDay>
    ): Long {
        val quest = Quest(
            title = title,
            description = description,
            questType = "DAILY",
            difficulty = difficulty.name,
            expReward = difficulty.expReward,
            goldReward = difficulty.goldReward,
            penaltyDamage = difficulty.penaltyDamage,
            weekDays = weekDays.map { it.displayName }
        )
        return questDao.insertQuest(quest)
    }

    suspend fun addTask(
        title: String,
        description: String,
        difficulty: QuestDifficulty,
        deadline: Long?
    ): Long {
        val quest = Quest(
            title = title,
            description = description,
            questType = "TASK",
            difficulty = difficulty.name,
            expReward = difficulty.expReward,
            goldReward = difficulty.goldReward,
            penaltyDamage = difficulty.penaltyDamage,
            deadline = deadline
        )
        return questDao.insertQuest(quest)
    }

    fun getAllQuests(): Flow<List<Quest>> = questDao.getAllQuests()

    suspend fun deleteQuest(quest: Quest) = questDao.deleteQuest(quest)

    suspend fun completeQuest(quest: Quest, character: Character): Character {
        val updatedCharacter = character.copy(
            exp = character.exp + quest.expReward,
            gold = character.gold + quest.goldReward
        ).checkLevelUp()

        characterDao.updateCharacter(updatedCharacter)
        questDao.updateQuest(quest.copy(isCompleted = true, isFailed = false))

        return updatedCharacter
    }

    suspend fun failQuest(quest: Quest, character: Character): Character {
        val damage = quest.penaltyDamage
        val updatedCharacter = character.copy(
            currentHp = maxOf(0, character.currentHp - damage)
        )

        characterDao.updateCharacter(updatedCharacter)
        questDao.updateQuest(quest.copy(isCompleted = true, isFailed = true))

        return updatedCharacter
    }

    // Методы для привычек
    suspend fun incrementHabit(quest: Quest, character: Character): Character {
        val updatedQuest = quest.copy(habitCounter = quest.habitCounter + 1)
        questDao.updateQuest(updatedQuest)

        val reducedExpReward = quest.expReward / 4
        val reducedGoldReward = quest.goldReward / 4

        val updatedCharacter = character.copy(
            exp = character.exp + reducedExpReward,
            gold = character.gold + reducedGoldReward
        ).checkLevelUp()

        characterDao.updateCharacter(updatedCharacter)
        return updatedCharacter
    }

    suspend fun decrementHabit(quest: Quest, character: Character): Character {
        val updatedQuest = quest.copy(habitCounter = quest.habitCounter - 1)
        questDao.updateQuest(updatedQuest)

        val reducedDamage = maxOf(1, quest.penaltyDamage / 4)

        val updatedCharacter = character.copy(
            currentHp = maxOf(0, character.currentHp - reducedDamage)
        )

        characterDao.updateCharacter(updatedCharacter)
        return updatedCharacter
    }

    suspend fun checkCharacterDeath(character: Character): Boolean {
        return character.currentHp <= 0
    }

    // Сброс персонажа при смерти с возможностью выбора класса
    suspend fun resetCharacterOnDeathWithNewClass(characterClass: CharacterClass): Character {
        clearAllInventory()

        val currentCharacter = getCharacter().first()

        // Создаем нового персонажа с выбранным классом
        val resetCharacter = Character(
            nickname = currentCharacter?.nickname ?: "Герой",
            characterClass = characterClass.name,
            level = 1,
            exp = 0,
            maxHp = characterClass.baseHp,
            currentHp = characterClass.baseHp,
            maxMp = characterClass.baseMp,
            currentMp = characterClass.baseMp,
            attack = characterClass.baseAttack,
            defense = characterClass.baseDefense,
            gold = 100
        )

        characterDao.updateCharacter(resetCharacter)
        return resetCharacter
    }

    // Метод для очистки всего инвентаря
    suspend fun clearAllInventory() {
        val allItems = inventoryDao.getAllItems().first()
        allItems.forEach { item ->
            inventoryDao.deleteItem(item)
        }
    }


    // Inventory methods
    fun getAllInventoryItems(): Flow<List<InventoryItem>> = inventoryDao.getAllItems()
    fun getEquippedItems(): Flow<List<InventoryItem>> = inventoryDao.getEquippedItems()

    suspend fun equipItem(item: InventoryItem, character: Character): Character {
        if (item.itemType == ItemType.CONSUMABLE) {
            return character
        }

        if (!item.canUse(character)) {
            return character
        }

        val currentEquipped = inventoryDao.getEquippedItemByType(item.itemType).first()
        currentEquipped?.let {
            inventoryDao.updateItem(it.copy(isEquipped = false))
        }

        inventoryDao.updateItem(item.copy(isEquipped = true))

        return recalculateCharacterStats(character)
    }

    suspend fun unequipItem(item: InventoryItem, character: Character): Character {
        inventoryDao.updateItem(item.copy(isEquipped = false))

        return recalculateCharacterStats(character)
    }

    suspend fun useConsumable(item: InventoryItem, character: Character): Character {
        if (!item.isConsumable) return character

        var updatedCharacter = character

        // Применяем эффекты зелья
        if (item.hpBonus > 0) {
            updatedCharacter = updatedCharacter.copy(
                currentHp = minOf(updatedCharacter.maxHp, updatedCharacter.currentHp + item.hpBonus)
            )
        }
        if (item.mpBonus > 0) {
            updatedCharacter = updatedCharacter.copy(
                currentMp = minOf(updatedCharacter.maxMp, updatedCharacter.currentMp + item.mpBonus)
            )
        }

        // Уменьшаем стак или удаляем предмет
        if (item.stackSize > 1) {
            val updatedItem = item.copy(stackSize = item.stackSize - 1)
            inventoryDao.updateItem(updatedItem)
        } else {
            inventoryDao.deleteItem(item)
        }

        characterDao.updateCharacter(updatedCharacter)
        return updatedCharacter
    }

    private suspend fun recalculateCharacterStats(character: Character): Character {
        val equippedItems = inventoryDao.getEquippedItems().first()

        var totalHpBonus = 0
        var totalMpBonus = 0
        var totalAttackBonus = 0
        var totalDefenseBonus = 0

        equippedItems.forEach { item ->
            totalHpBonus += item.hpBonus
            totalMpBonus += item.mpBonus
            totalAttackBonus += item.attackBonus
            totalDefenseBonus += item.defenseBonus
        }

        val baseStats = getBaseStatsForClass(character.characterClass)
        val updatedCharacter = character.copy(
            maxHp = baseStats.hp + totalHpBonus,
            currentHp = minOf(character.currentHp, baseStats.hp + totalHpBonus),
            maxMp = baseStats.mp + totalMpBonus,
            currentMp = minOf(character.currentMp, baseStats.mp + totalMpBonus),
            attack = baseStats.attack + totalAttackBonus,
            defense = baseStats.defense + totalDefenseBonus
        )

        characterDao.updateCharacter(updatedCharacter)
        return updatedCharacter
    }

    private data class BaseStats(val hp: Int, val mp: Int, val attack: Int, val defense: Int)

    private fun getBaseStatsForClass(characterClass: String?): BaseStats {
        return when (characterClass) {
            "WARRIOR" -> BaseStats(150, 30, 15, 15)
            "ARCHER" -> BaseStats(100, 50, 12, 10)
            "MAGE" -> BaseStats(80, 100, 10, 8)
            else -> BaseStats(100, 50, 10, 10)
        }
    }

    // Методы для магазина
    fun getShopItems(): List<InventoryItem> {
        return com.example.habitrpg.data.model.getShopItems()
    }

    suspend fun buyItem(item: InventoryItem, character: Character): Boolean {
        if (character.gold >= item.goldValue) {
            // Проверяем, есть ли уже такой расходник в инвентаре
            if (item.isConsumable) {
                val existingConsumable = inventoryDao.getAllItems().first().find {
                    it.name == item.name && it.itemType == ItemType.CONSUMABLE
                }

                if (existingConsumable != null) {
                    val updatedItem = existingConsumable.copy(
                        stackSize = existingConsumable.stackSize + 1
                    )
                    inventoryDao.updateItem(updatedItem)
                } else {
                    val itemForInventory = item.copy(
                        id = 0,
                        isEquipped = false,
                        stackSize = 1
                    )
                    inventoryDao.insertItem(itemForInventory)
                }
            } else {
                val itemForInventory = item.copy(
                    id = 0,
                    isEquipped = false
                )
                inventoryDao.insertItem(itemForInventory)
            }

            val updatedCharacter = character.copy(
                gold = character.gold - item.goldValue
            )
            characterDao.updateCharacter(updatedCharacter)

            return true
        }
        return false
    }

    // Метод для ежедневного обслуживания задач
    suspend fun performDailyMaintenance(character: Character): Character {
        var updatedCharacter = character

        updatedCharacter = resetDailyQuests(updatedCharacter)
        updatedCharacter = checkOverdueTasks(updatedCharacter)

        return updatedCharacter
    }

    // Сброс ежедневных задач и начисление штрафов
    private suspend fun resetDailyQuests(character: Character): Character {
        var updatedCharacter = character

        val dailies = questDao.getAllQuests().first().filter { it.questType == "DAILY" }
        val today = DateUtils.getStartOfDay()
        val yesterday = today - 24 * 60 * 60 * 1000

        dailies.forEach { daily ->
            if (daily.needsReset()) {
                val wasActiveYesterday = wasQuestActiveOnDate(daily, yesterday)

                if (wasActiveYesterday && !daily.isCompleted) {
                    val damage = daily.penaltyDamage
                    updatedCharacter = updatedCharacter.copy(
                        currentHp = maxOf(0, updatedCharacter.currentHp - damage)
                    )

                    questDao.updateQuest(daily.copy(
                        isFailed = true,
                        isCompleted = true,
                        lastResetDate = today
                    ))
                } else {
                    val shouldResetCompletion = daily.isActiveToday()
                    questDao.updateQuest(daily.copy(
                        isCompleted = if (shouldResetCompletion) false else daily.isCompleted,
                        isFailed = if (shouldResetCompletion) false else daily.isFailed,
                        lastResetDate = today
                    ))
                }
            }
        }

        characterDao.updateCharacter(updatedCharacter)
        return updatedCharacter
    }

    // Вспомогательная функция для проверки активности задачи на определенную дату
    private fun wasQuestActiveOnDate(quest: Quest, date: Long): Boolean {
        if (quest.questType != "DAILY") return false

        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
        }
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Пн"
            Calendar.TUESDAY -> "Вт"
            Calendar.WEDNESDAY -> "Ср"
            Calendar.THURSDAY -> "Чт"
            Calendar.FRIDAY -> "Пт"
            Calendar.SATURDAY -> "Сб"
            Calendar.SUNDAY -> "Вс"
            else -> "Пн"
        }

        return quest.weekDays.contains(dayOfWeek)
    }

    // Проверка и автопровал просроченных задач
    private suspend fun checkOverdueTasks(character: Character): Character {
        var updatedCharacter = character

        val tasks = questDao.getAllQuests().first().filter { it.questType == "TASK" }

        tasks.forEach { task ->
            if (task.deadline != null &&
                !task.isCompleted &&
                !task.autoFailed &&
                DateUtils.isDeadlinePassed(task.deadline)) {

                val damage = task.penaltyDamage
                updatedCharacter = updatedCharacter.copy(
                    currentHp = maxOf(0, updatedCharacter.currentHp - damage)
                )

                questDao.updateQuest(task.copy(
                    isFailed = true,
                    isCompleted = true,
                    autoFailed = true,
                    isOverdue = true
                ))
            }
        }

        characterDao.updateCharacter(updatedCharacter)
        return updatedCharacter
    }
}

private fun Character.checkLevelUp(): Character {
    var currentCharacter = this
    var remainingExp = currentCharacter.exp

    while (remainingExp >= currentCharacter.getExpForNextLevel()) {
        val expNeeded = currentCharacter.getExpForNextLevel()
        remainingExp -= expNeeded

        currentCharacter = currentCharacter.copy(
            level = currentCharacter.level + 1,
            exp = remainingExp,
            maxHp = currentCharacter.maxHp + 10,
            currentHp = currentCharacter.maxHp + 10,
            maxMp = currentCharacter.maxMp + 5,
            currentMp = currentCharacter.maxMp + 5,
            attack = currentCharacter.attack + 1,
            defense = currentCharacter.defense + 1
        )
    }
    return currentCharacter
}