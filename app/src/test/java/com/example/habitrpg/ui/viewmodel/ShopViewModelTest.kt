package com.example.habitrpg.ui.viewmodel

import com.example.habitrpg.data.model.Character
import com.example.habitrpg.data.model.InventoryItem
import com.example.habitrpg.data.model.ItemRarity
import com.example.habitrpg.data.model.ItemType
import com.example.habitrpg.data.repository.GameRepository
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ShopViewModelTest {

    private lateinit var viewModel: ShopViewModel
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

    private val testShopItems = listOf(
        InventoryItem(
            id = 1,
            name = "Steel Sword",
            description = "A sturdy steel sword",
            itemType = ItemType.WEAPON,
            rarity = ItemRarity.UNCOMMON,
            requiredLevel = 3,
            attackBonus = 8,
            goldValue = 120,
            isEquipped = false,
            isConsumable = false,
            stackSize = 1,
            spriteResId = "item_sword_steel"
        ),
        InventoryItem(
            id = 2,
            name = "Health Potion",
            description = "Restores 50 HP",
            itemType = ItemType.CONSUMABLE,
            rarity = ItemRarity.COMMON,
            requiredLevel = 1,
            hpBonus = 50,
            goldValue = 25,
            isEquipped = false,
            isConsumable = true,
            stackSize = 1,
            spriteResId = "item_potion_health"
        ),
        InventoryItem(
            id = 3,
            name = "Dragon Scale Armor",
            description = "Legendary armor made from dragon scales",
            itemType = ItemType.BREASTPLATE,
            rarity = ItemRarity.LEGENDARY,
            requiredLevel = 10,
            defenseBonus = 15,
            hpBonus = 30,
            goldValue = 500,
            isEquipped = false,
            isConsumable = false,
            stackSize = 1,
            spriteResId = "item_armor_dragon"
        )
    )

    @Before
    fun setUp() {
        mockRepository = mock()
        viewModel = ShopViewModel(mockRepository)
    }

    @Test
    fun `shop items should be loaded on initialization`() = runTest {
        whenever(mockRepository.getShopItems()).thenReturn(testShopItems)

        viewModel.loadShopItems()
        val result = viewModel.shopItems.single()

        assertEquals(testShopItems, result)
        verify(mockRepository).getShopItems()
    }

    @Test
    fun `buyItem should call repository and update purchase result on success`() = runTest {
        val itemToBuy = testShopItems[0]
        whenever(mockRepository.buyItem(itemToBuy, testCharacter)).thenReturn(true)

        viewModel.buyItem(itemToBuy, testCharacter)

        verify(mockRepository).buyItem(itemToBuy, testCharacter)
        assertTrue(viewModel.isLoading.value)

        val purchaseResult = viewModel.purchaseResult.single()
        assertTrue(purchaseResult is PurchaseResult.Success)
        assertEquals("Предмет \"${itemToBuy.name}\" куплен!", (purchaseResult as PurchaseResult.Success).message)
    }

    @Test
    fun `buyItem should update purchase result on failure`() = runTest {
        val expensiveItem = testShopItems[2]
        whenever(mockRepository.buyItem(expensiveItem, testCharacter)).thenReturn(false)

        viewModel.buyItem(expensiveItem, testCharacter)

        verify(mockRepository).buyItem(expensiveItem, testCharacter)

        val purchaseResult = viewModel.purchaseResult.single()
        assertTrue(purchaseResult is PurchaseResult.Error)
        assertEquals("Недостаточно золота!", (purchaseResult as PurchaseResult.Error).message)
    }

    @Test
    fun `clearPurchaseResult should reset purchase result to null`() = runTest {
        val itemToBuy = testShopItems[0]
        whenever(mockRepository.buyItem(itemToBuy, testCharacter)).thenReturn(true)
        viewModel.buyItem(itemToBuy, testCharacter)

        assertNotNull(viewModel.purchaseResult.value)

        viewModel.clearPurchaseResult()

        assertNull(viewModel.purchaseResult.value)
    }

    @Test
    fun `isLoading should be managed during buy operation`() = runTest {
        val itemToBuy = testShopItems[0]
        whenever(mockRepository.buyItem(itemToBuy, testCharacter)).thenReturn(true)

        viewModel.buyItem(itemToBuy, testCharacter)

        assertTrue(viewModel.isLoading.value)
        verify(mockRepository).buyItem(itemToBuy, testCharacter)
    }

    @Test
    fun `loadShopItems should update shop items and loading state`() = runTest {
        val newShopItems = listOf(
            InventoryItem(
                name = "New Item",
                description = "A new shop item",
                itemType = ItemType.ACCESSORY,
                rarity = ItemRarity.RARE,
                goldValue = 75
            )
        )
        whenever(mockRepository.getShopItems()).thenReturn(newShopItems)

        viewModel.loadShopItems()

        verify(mockRepository).getShopItems()
        assertEquals(newShopItems, viewModel.shopItems.value)
        assertTrue(viewModel.isLoading.value)
    }

    @Test
    fun `purchase result should be null initially`() = runTest {
        val initialPurchaseResult = viewModel.purchaseResult.value

        assertNull(initialPurchaseResult)
    }

    @Test
    fun `isLoading should be false initially`() = runTest {
        val initialLoadingState = viewModel.isLoading.value

        assertFalse(initialLoadingState)
    }

    @Test
    fun `multiple buy operations should handle state correctly`() = runTest {
        val affordableItem = testShopItems[0]
        val expensiveItem = testShopItems[2]
        whenever(mockRepository.buyItem(affordableItem, testCharacter)).thenReturn(true)
        whenever(mockRepository.buyItem(expensiveItem, testCharacter)).thenReturn(false)

        viewModel.buyItem(affordableItem, testCharacter)
        viewModel.buyItem(expensiveItem, testCharacter)

        verify(mockRepository).buyItem(affordableItem, testCharacter)
        verify(mockRepository).buyItem(expensiveItem, testCharacter)

        val finalResult = viewModel.purchaseResult.value
        assertTrue(finalResult is PurchaseResult.Error)
    }

    @Test
    fun `empty shop items list should be handled correctly`() = runTest {
        val emptyShopItems = emptyList<InventoryItem>()
        whenever(mockRepository.getShopItems()).thenReturn(emptyShopItems)

        viewModel.loadShopItems()
        val result = viewModel.shopItems.single()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `buyItem should not call repository when isLoading is true`() = runTest {
        val itemToBuy = testShopItems[0]

        viewModel.buyItem(itemToBuy, testCharacter)

        verify(mockRepository).buyItem(itemToBuy, testCharacter)
    }

    @Test
    fun `purchase result flow should emit correct values`() = runTest {
        val itemToBuy = testShopItems[0]
        whenever(mockRepository.buyItem(itemToBuy, testCharacter)).thenReturn(true)

        val results = mutableListOf<PurchaseResult?>()

        viewModel.buyItem(itemToBuy, testCharacter)
        viewModel.clearPurchaseResult()

        assertEquals(3, results.size)
        assertNull(results[0])
        assertTrue(results[1] is PurchaseResult.Success)
        assertNull(results[2])
    }

    @Test
    fun `shop items should be accessible without calling loadShopItems first`() = runTest {

        val initialShopItems = viewModel.shopItems.value

        assertTrue(initialShopItems.isEmpty())
    }
}