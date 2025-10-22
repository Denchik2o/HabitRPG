package com.example.habitrpg.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Типы предметов
enum class ItemType(val displayName: String) {
    WEAPON("Оружие"),
    HELMET("Шлем"),
    BREASTPLATE("Нагрудник"),
    GREAVES("Поножи"),
    ACCESSORY("Аксессуар"),
    CONSUMABLE("Расходник")
}

// Редкость предметов
enum class ItemRarity(
    val displayName: String,
    val color: String
) {
    COMMON("Обычный", "#A0A0A0"),
    UNCOMMON("Необычный", "#00FF00"),
    RARE("Редкий", "#0070DD"),
    EPIC("Эпический", "#A335EE"),
    LEGENDARY("Легендарный", "#FF8000")
}

// Классы, которые могут использовать предмет
enum class ItemClass(val displayName: String) {
    WARRIOR("Воин"),
    ARCHER("Лучник"),
    MAGE("Маг"),
    ALL("Все")
}

@Entity
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val itemType: ItemType,
    val rarity: ItemRarity,
    val requiredLevel: Int = 1,
    val allowedClass: ItemClass = ItemClass.ALL,

    // Бонусы к характеристикам
    val hpBonus: Int = 0,
    val mpBonus: Int = 0,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,

    // Цена и свойства
    val goldValue: Int = 0,
    val isEquipped: Boolean = false,
    val isConsumable: Boolean = false,
    val stackSize: Int = 1,

    // sprite
    val spriteResId: String = "",
) {
    fun getDisplayName(): String {
        return when (rarity) {
            ItemRarity.COMMON -> name
            ItemRarity.UNCOMMON -> "$name ✨"
            ItemRarity.RARE -> "$name 🔷"
            ItemRarity.EPIC -> "$name 💜"
            ItemRarity.LEGENDARY -> "$name 🧡"
        }
    }

    fun canUse(character: Character): Boolean {
        return character.level >= requiredLevel &&
                (allowedClass == ItemClass.ALL ||
                        character.characterClass == allowedClass.name)
    }
}

// Категории магазина
enum class ShopCategory(val displayName: String) {
    WEAPONS("Оружие"),
    ARMOR("Броня"),
    ACCESSORIES("Аксессуары"),
    CONSUMABLES("Зелья"),
    ALL("Все товары")
}

// Стартовые предметы для магазина
fun getShopItems(): List<InventoryItem> {
    return listOf(
        // Оружие
        InventoryItem(
            name = "Деревянный меч",
            description = "Простой меч для начинающих воинов",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.COMMON,
            requiredLevel = 1,
            allowedClass = ItemClass.WARRIOR,
            attackBonus = 2,
            goldValue = 50,
            spriteResId = "item_sword_wooden"
        ),
        InventoryItem(
            name = "Стальной меч",
            description = "Качественное оружие воина",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.UNCOMMON,
            requiredLevel = 3,
            allowedClass = ItemClass.WARRIOR,
            attackBonus = 5,
            goldValue = 120,
        ),
        InventoryItem(
            name = "Дубовый лук",
            description = "Надежный лук для тренировок",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.COMMON,
            requiredLevel = 1,
            allowedClass = ItemClass.ARCHER,
            attackBonus = 2,
            goldValue = 50,
            spriteResId = "item_bow_oak"
        ),
        InventoryItem(
            name = "Посох ученика",
            description = "Простейший магический посох",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.COMMON,
            requiredLevel = 1,
            allowedClass = ItemClass.MAGE,
            mpBonus = 5,
            goldValue = 50,
            spriteResId = "item_staff_apprentice"
        ),

        // Броня
        InventoryItem(
            name = "Кожаный шлем",
            description = "Базовая защита для головы",
            itemType = ItemType.HELMET,
            rarity = ItemRarity.COMMON,
            requiredLevel = 1,
            defenseBonus = 1,
            goldValue = 30,
            spriteResId = "item_helmet_leather"
        ),
        InventoryItem(
            name = "Кожаный нагрудник",
            description = "Кожаная броня для торса",
            itemType = ItemType.BREASTPLATE,
            rarity = ItemRarity.COMMON,
            requiredLevel = 1,
            defenseBonus = 2,
            goldValue = 40,
            spriteResId = "item_breastplate_leather"
        ),
        InventoryItem(
            name = "Кожаные поножи",
            description = "Защита для ног",
            itemType = ItemType.GREAVES,
            rarity = ItemRarity.COMMON,
            requiredLevel = 1,
            defenseBonus = 1,
            goldValue = 30,
            spriteResId = "item_greaves_leather"
        ),

        // Аксессуары
        InventoryItem(
            name = "Медное кольцо",
            description = "Простое кольцо с малой магией",
            itemType = ItemType.ACCESSORY,
            rarity = ItemRarity.COMMON,
            requiredLevel = 1,
            hpBonus = 5,
            goldValue = 25,
            spriteResId = "item_ring_copper"
        ),
        InventoryItem(
            name = "Серебряный амулет",
            description = "Амулет с защитной магией",
            itemType = ItemType.ACCESSORY,
            rarity = ItemRarity.UNCOMMON,
            requiredLevel = 3,
            defenseBonus = 3,
            hpBonus = 10,
            goldValue = 80
        ),

        // Зелья
        InventoryItem(
            name = "Зелье здоровья",
            description = "Восстанавливает 30 HP",
            itemType = ItemType.CONSUMABLE,
            rarity = ItemRarity.COMMON,
            isConsumable = true,
            hpBonus = 30,
            stackSize = 1,
            goldValue = 20
        ),
        InventoryItem(
            name = "Зелье маны",
            description = "Восстанавливает 25 MP",
            itemType = ItemType.CONSUMABLE,
            rarity = ItemRarity.COMMON,
            isConsumable = true,
            mpBonus = 25,
            stackSize = 1,
            goldValue = 18
        ),
        InventoryItem(
            name = "Большое зелье здоровья",
            description = "Восстанавливает 60 HP",
            itemType = ItemType.CONSUMABLE,
            rarity = ItemRarity.UNCOMMON,
            isConsumable = true,
            hpBonus = 60,
            stackSize = 1,
            goldValue = 35,
            requiredLevel = 3
        ),
        InventoryItem(
            name = "Большое зелье маны",
            description = "Восстанавливает 50 MP",
            itemType = ItemType.CONSUMABLE,
            rarity = ItemRarity.UNCOMMON,
            isConsumable = true,
            mpBonus = 50,
            stackSize = 1,
            goldValue = 32,
            requiredLevel = 3
        )
    )
}