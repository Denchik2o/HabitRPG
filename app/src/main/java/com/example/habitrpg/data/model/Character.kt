package com.example.habitrpg.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Character(
    @PrimaryKey val id: Long = 1,
    val nickname: String = "NoName",
    val level: Int = 1,
    val exp: Int = 0,
    val maxHp: Int = 100,
    val currentHp: Int = 100,
    val maxMp: Int = 50,
    val currentMp: Int = 50,
    val attack: Int = 10,
    val defense: Int = 10,
    val gold: Int = 100,
    val characterClass: String = "WARRIOR"
) {
    fun getExpForNextLevel(): Int = (level * 25) + 100

    fun checkLevelUp(): Character {
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
}

enum class CharacterClass(
    val displayName: String,
    val baseHp: Int,
    val baseMp: Int,
    val baseAttack: Int,
    val baseDefense: Int,
    val passiveDescription: String,
    val bonusTags: List<String>
) {
    WARRIOR(
        "Воин",
        baseHp = 150,
        baseMp = 30,
        baseAttack = 15,
        baseDefense = 15,
        passiveDescription = "Стойкость: -20% урон за проваленные задачи",
        bonusTags = listOf("физическая активность", "путешествие")
    ),
    ARCHER(
        "Лучник",
        baseHp = 100,
        baseMp = 50,
        baseAttack = 12,
        baseDefense = 10,
        passiveDescription = "Меткость: 15% шанс двойной награды",
        bonusTags = listOf("рутина", "ежедневное")
    ),
    MAGE(
        "Маг",
        baseHp = 80,
        baseMp = 100,
        baseAttack = 10,
        baseDefense = 8,
        passiveDescription = "Мудрость: +25% опыта за все задачи",
        bonusTags = listOf("творчество", "учеба")
    )
}
