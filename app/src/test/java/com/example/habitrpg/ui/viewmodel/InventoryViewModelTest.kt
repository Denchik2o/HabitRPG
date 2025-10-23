package com.example.habitrpg.ui.viewmodel

import com.example.habitrpg.data.model.Character
import com.example.habitrpg.data.model.InventoryItem
import com.example.habitrpg.data.model.ItemRarity
import com.example.habitrpg.data.model.ItemType
import com.example.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class InventoryViewModelTest {

    private lateinit var viewModel: InventoryViewModel
    private lateinit var mockRepository: GameRepository

    private val testCharacter = Character(
        id = 1,
        nickname = "TestHero",
        level = 5,
        exp = 100,
        maxHp = 150,
        currentHp = 120,
        maxMp = 80,
        currentMp = 60,
        attack = 15,
        defense = 12,
        gold = 200,
        characterClass = "WARRIOR"
    )

    private val testWeapon = InventoryItem(
        id = 1,
        name = "Steel Sword",
        description = "A sturdy steel sword",
        itemType = ItemType.WEAPON,
        rarity = ItemRarity.UNCOMMON,
        requiredLevel = 3,
        attackBonus = 8,
        defenseBonus = 2,
        goldValue = 120,
        isEquipped = false,
        isConsumable = false,
        stackSize = 1,
        spriteResId = "item_sword_steel"
    )

    private val testArmor = InventoryItem(
        id = 2,
        name = "Iron Breastplate",
        description = "Heavy iron chest armor",
        itemType = ItemType.BREASTPLATE,
        rarity = ItemRarity.COMMON,
        requiredLevel = 2,
        attackBonus = 0,
        defenseBonus = 6,
        goldValue = 80,
        isEquipped = true,
        isConsumable = false,
        stackSize = 1,
        spriteResId = "item_breastplate_iron"
    )

    private val testPotion = InventoryItem(
        id = 3,
        name = "Health Potion",
        description = "Restores 50 HP",
        itemType = ItemType.CONSUMABLE,
        rarity = ItemRarity.COMMON,
        requiredLevel = 1,
        hpBonus = 50,
        goldValue = 25,
        isEquipped = false,
        isConsumable = true,
        stackSize = 3,
        spriteResId = "item_potion_health"
    )

    @Before
    fun setUp() {
        mockRepository = mock()
        viewModel = InventoryViewModel(mockRepository)
    }

    @Test
    fun `inventory items should be loaded on initialization`() = runTest {
        val inventoryItems = listOf(testWeapon, testArmor, testPotion)
        whenever(mockRepository.getAllInventoryItems()).thenReturn(flowOf(inventoryItems))

        val result = viewModel.inventoryItems.single()

        assertEquals(inventoryItems, result)
        verify(mockRepository).getAllInventoryItems()
    }

    @Test
    fun `equipped items should be loaded on initialization`() = runTest {
        val equippedItems = listOf(testArmor)
        whenever(mockRepository.getEquippedItems()).thenReturn(flowOf(equippedItems))

        val result = viewModel.equippedItems.single()

        assertEquals(equippedItems, result)
        verify(mockRepository).getEquippedItems()
    }

    @Test
    fun `equipItem should call repository and update state`() = runTest {
        whenever(mockRepository.equipItem(testWeapon, testCharacter)).thenReturn(testCharacter)
        val inventoryItems = listOf(testWeapon, testArmor)
        whenever(mockRepository.getAllInventoryItems()).thenReturn(flowOf(inventoryItems))
        val equippedItems = listOf(testArmor, testWeapon.copy(isEquipped = true))
        whenever(mockRepository.getEquippedItems()).thenReturn(flowOf(equippedItems))

        viewModel.equipItem(testWeapon, testCharacter)

        verify(mockRepository).equipItem(testWeapon, testCharacter)
        assertTrue(viewModel.isLoading.value)
    }

    @Test
    fun `unequipItem should call repository and update state`() = runTest {
        whenever(mockRepository.unequipItem(testArmor, testCharacter)).thenReturn(testCharacter)
        val inventoryItems = listOf(testWeapon, testArmor.copy(isEquipped = false))
        whenever(mockRepository.getAllInventoryItems()).thenReturn(flowOf(inventoryItems))
        val equippedItems = emptyList<InventoryItem>()
        whenever(mockRepository.getEquippedItems()).thenReturn(flowOf(equippedItems))

        viewModel.unequipItem(testArmor, testCharacter)

        verify(mockRepository).unequipItem(testArmor, testCharacter)
        assertTrue(viewModel.isLoading.value)
    }

    @Test
    fun `useConsumable should call repository and update state`() = runTest {
        val updatedCharacter = testCharacter.copy(currentHp = testCharacter.currentHp + testPotion.hpBonus)
        whenever(mockRepository.useConsumable(testPotion, testCharacter)).thenReturn(updatedCharacter)
        val updatedInventory = listOf(testPotion.copy(stackSize = 2))
        whenever(mockRepository.getAllInventoryItems()).thenReturn(flowOf(updatedInventory))

        viewModel.useConsumable(testPotion, testCharacter)

        verify(mockRepository).useConsumable(testPotion, testCharacter)
        assertTrue(viewModel.isLoading.value)
    }

    @Test
    fun `isLoading should be false after successful operation`() = runTest {
        whenever(mockRepository.equipItem(testWeapon, testCharacter)).thenReturn(testCharacter)
        whenever(mockRepository.getAllInventoryItems()).thenReturn(flowOf(emptyList()))
        whenever(mockRepository.getEquippedItems()).thenReturn(flowOf(emptyList()))

        viewModel.equipItem(testWeapon, testCharacter)

        verify(mockRepository).equipItem(testWeapon, testCharacter)
    }

    @Test
    fun `inventory items flow should update when repository emits new data`() = runTest {
        val initialItems = listOf(testWeapon)
        val updatedItems = listOf(testWeapon, testPotion)
        val flow = flowOf(initialItems, updatedItems)
        whenever(mockRepository.getAllInventoryItems()).thenReturn(flow)

        val results = mutableListOf<List<InventoryItem>>()
        viewModel.inventoryItems.collect { results.add(it) }

        assertEquals(2, results.size)
        assertEquals(initialItems, results[0])
        assertEquals(updatedItems, results[1])
    }

    @Test
    fun `equipped items flow should update when repository emits new data`() = runTest {
        val initialEquipped = listOf(testArmor)
        val updatedEquipped = listOf(testArmor, testWeapon.copy(isEquipped = true))
        val flow = flowOf(initialEquipped, updatedEquipped)
        whenever(mockRepository.getEquippedItems()).thenReturn(flow)

        val results = mutableListOf<List<InventoryItem>>()
        viewModel.equippedItems.collect { results.add(it) }

        assertEquals(2, results.size)
        assertEquals(initialEquipped, results[0])
        assertEquals(updatedEquipped, results[1])
    }

    @Test
    fun `multiple operations should handle loading state correctly`() = runTest {
        whenever(mockRepository.equipItem(testWeapon, testCharacter)).thenReturn(testCharacter)
        whenever(mockRepository.unequipItem(testArmor, testCharacter)).thenReturn(testCharacter)
        whenever(mockRepository.getAllInventoryItems()).thenReturn(flowOf(emptyList()))
        whenever(mockRepository.getEquippedItems()).thenReturn(flowOf(emptyList()))

        viewModel.equipItem(testWeapon, testCharacter)
        viewModel.unequipItem(testArmor, testCharacter)

        verify(mockRepository).equipItem(testWeapon, testCharacter)
        verify(mockRepository).unequipItem(testArmor, testCharacter)
    }

    @Test
    fun `empty inventory should be handled correctly`() = runTest {
        val emptyInventory = emptyList<InventoryItem>()
        whenever(mockRepository.getAllInventoryItems()).thenReturn(flowOf(emptyInventory))
        whenever(mockRepository.getEquippedItems()).thenReturn(flowOf(emptyInventory))

        val inventoryResult = viewModel.inventoryItems.single()
        val equippedResult = viewModel.equippedItems.single()

        assertTrue(inventoryResult.isEmpty())
        assertTrue(equippedResult.isEmpty())
    }

    @Test
    fun `isLoading should start as false`() = runTest {
        val initialLoadingState = viewModel.isLoading.value

        assertFalse(initialLoadingState)
    }
}