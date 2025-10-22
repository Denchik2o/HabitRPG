package com.example.habitrpg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.habitrpg.data.model.Character
import com.example.habitrpg.data.model.InventoryItem
import com.example.habitrpg.data.model.ShopCategory

@Composable
fun ShopScreen(
    character: Character,
    shopItems: List<InventoryItem>,
    inventoryItems: List<InventoryItem>,
    onBuyItem: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(ShopCategory.ALL) }

    // Фильтруем предметы по классу персонажа и убираем уже купленные нерасходные предметы
    val availableShopItems = remember(shopItems, inventoryItems, character.characterClass) {
        shopItems.filter { shopItem ->
            // Проверяем класс
            val classMatches = shopItem.allowedClass == com.example.habitrpg.data.model.ItemClass.ALL ||
                    shopItem.allowedClass.name == character.characterClass

            // Для расходников всегда показываем в магазине
            val isAvailable = if (shopItem.isConsumable) {
                true
            } else {
                // Для нерасходных предметов проверяем, не куплен ли уже
                inventoryItems.none { inventoryItem ->
                    inventoryItem.name == shopItem.name &&
                            inventoryItem.itemType == shopItem.itemType
                }
            }

            classMatches && isAvailable
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Фильтры категорий с горизонтальным скроллом
        ScrollableCategoryFilter(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Дополнительная фильтрация по выбранной категории
        val filteredItems = if (selectedCategory == ShopCategory.ALL) {
            availableShopItems
        } else {
            availableShopItems.filter {
                when (selectedCategory) {
                    ShopCategory.WEAPONS -> it.itemType == com.example.habitrpg.data.model.ItemType.WEAPON
                    ShopCategory.ARMOR -> it.itemType in listOf(
                        com.example.habitrpg.data.model.ItemType.HELMET,
                        com.example.habitrpg.data.model.ItemType.BREASTPLATE,
                        com.example.habitrpg.data.model.ItemType.GREAVES
                    )
                    ShopCategory.ACCESSORIES -> it.itemType == com.example.habitrpg.data.model.ItemType.ACCESSORY
                    ShopCategory.CONSUMABLES -> it.itemType == com.example.habitrpg.data.model.ItemType.CONSUMABLE
                    else -> true
                }
            }
        }

        if (filteredItems.isEmpty()) {
            EmptyShopState(
                selectedCategory = selectedCategory,
                characterClass = character.characterClass,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredItems) { item ->
                    ShopItemCard(
                        item = item,
                        character = character,
                        inventoryItems = inventoryItems,
                        onBuy = { onBuyItem(item) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollableCategoryFilter(
    selectedCategory: ShopCategory,
    onCategorySelected: (ShopCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShopCategory.values().forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ShopItemCard(
    item: InventoryItem,
    character: Character,
    inventoryItems: List<InventoryItem>,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canAfford = character.gold >= item.goldValue
    val canUse = item.canUse(character)
    val isConsumable = item.itemType == com.example.habitrpg.data.model.ItemType.CONSUMABLE

    // Для расходников показываем количество в инвентаре
    val consumableCount = if (isConsumable) {
        inventoryItems.find { it.name == item.name }?.stackSize ?: 0
    } else {
        0
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.getDisplayName(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.itemType.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = "${item.goldValue} 🪙",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            // Описание
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Для расходников показываем количество в инвентаре
            if (isConsumable && consumableCount > 0) {
                Text(
                    text = "В инвентаре: $consumableCount шт",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Бонусы - выравнивание по левому краю с отступами
            BonusStatsRow(item = item)

            // Требования (только по уровню, так как по классу уже отфильтровано)
            if (!canUse) {
                Text(
                    text = "Требуется: ${item.requiredLevel} уровень",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Кнопка покупки
            Button(
                onClick = onBuy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = canAfford && canUse,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            ) {
                Text(if (canAfford) "Купить" else "Недостаточно золота")
            }
        }
    }
}

@Composable
fun BonusStatsRow(
    item: InventoryItem,
    modifier: Modifier = Modifier
) {
    val bonuses = mutableListOf<@Composable () -> Unit>()

    if (item.hpBonus > 0) {
        bonuses.add { Text("❤️ +${item.hpBonus}", modifier = Modifier.padding(end = 16.dp)) }
    }
    if (item.mpBonus > 0) {
        bonuses.add { Text("🔵 +${item.mpBonus}", modifier = Modifier.padding(end = 16.dp)) }
    }
    if (item.attackBonus > 0) {
        bonuses.add { Text("⚔️ +${item.attackBonus}", modifier = Modifier.padding(end = 16.dp)) }
    }
    if (item.defenseBonus > 0) {
        bonuses.add { Text("🛡️ +${item.defenseBonus}") }
    }

    if (bonuses.isNotEmpty()) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            bonuses.forEach { bonus ->
                bonus()
            }
        }
    }
}

@Composable
fun EmptyShopState(
    selectedCategory: ShopCategory,
    characterClass: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📦",
            style = MaterialTheme.typography.displayMedium
        )
        Text(
            text = when {
                selectedCategory != ShopCategory.ALL -> "В категории \"${selectedCategory.displayName}\" нет доступных предметов"
                else -> "Нет доступных предметов для покупки"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = "Ваш класс: ${getClassDisplayName(characterClass)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Вспомогательная функция для отображения имени класса
private fun getClassDisplayName(characterClass: String): String {
    return when (characterClass) {
        "WARRIOR" -> "Воин"
        "ARCHER" -> "Лучник"
        "MAGE" -> "Маг"
        else -> "Неизвестно"
    }
}