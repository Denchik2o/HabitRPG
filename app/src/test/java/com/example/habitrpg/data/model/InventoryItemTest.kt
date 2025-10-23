package com.example.habitrpg.data.model

import org.junit.Assert.*
import org.junit.Test

class InventoryItemTest {

    @Test
    fun `inventory item should return correct display name with rarity`() {
        val commonItem = InventoryItem(
            name = "Sword",
            description = "A sword",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.COMMON
        )

        val rareItem = InventoryItem(
            name = "Dragon Sword",
            description = "A rare sword",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.RARE
        )

        val legendaryItem = InventoryItem(
            name = "Excalibur",
            description = "Legendary sword",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.LEGENDARY
        )

        assertEquals("Sword", commonItem.getDisplayName())
        assertTrue(rareItem.getDisplayName().contains("🔷"))
        assertTrue(legendaryItem.getDisplayName().contains("🧡"))
    }

    @Test
    fun `item usage eligibility should check level and class`() {
        val warrior = Character(
            nickname = "Warrior",
            characterClass = "WARRIOR",
            level = 5
        )

        val mage = Character(
            nickname = "Mage",
            characterClass = "MAGE",
            level = 3
        )

        val warriorItem = InventoryItem(
            name = "Warrior Sword",
            description = "For warriors only",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.COMMON,
            requiredLevel = 3,
            allowedClass = ItemClass.WARRIOR
        )

        val highLevelItem = InventoryItem(
            name = "High Level Item",
            description = "Requires high level",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.COMMON,
            requiredLevel = 10,
            allowedClass = ItemClass.ALL
        )

        assertTrue(warriorItem.canUse(warrior))
        assertFalse(warriorItem.canUse(mage))
        assertFalse(highLevelItem.canUse(warrior))
        assertFalse(highLevelItem.canUse(mage))
    }

    @Test
    fun `all classes item should be usable by anyone`() {
        val warrior = Character(characterClass = "WARRIOR", level = 1)
        val archer = Character(characterClass = "ARCHER", level = 1)
        val mage = Character(characterClass = "MAGE", level = 1)

        val universalItem = InventoryItem(
            name = "Universal Item",
            description = "Everyone can use this",
            itemType = ItemType.ACCESSORY,
            rarity = ItemRarity.COMMON,
            requiredLevel = 1,
            allowedClass = ItemClass.ALL
        )

        // Then
        assertTrue(universalItem.canUse(warrior))
        assertTrue(universalItem.canUse(archer))
        assertTrue(universalItem.canUse(mage))
    }
}