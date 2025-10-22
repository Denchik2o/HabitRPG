package com.example.habitrpg.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habitrpg.data.model.Character
import com.example.habitrpg.data.model.InventoryItem
import com.example.habitrpg.data.model.ItemType
import com.example.habitrpg.utils.SpriteUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    character: Character,
    inventoryItems: List<InventoryItem>,
    equippedItems: List<InventoryItem>,
    onEquipItem: (InventoryItem) -> Unit,
    onUnequipItem: (InventoryItem) -> Unit,
    onUseConsumable: (InventoryItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎒 Инвентарь",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Секция экипировки
            Text(
                text = "Экипировка",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            EquipmentGrid(
                equippedItems = equippedItems,
                character = character,
                onUnequipItem = onUnequipItem,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Разделитель
            Spacer(modifier = Modifier.height(24.dp))
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))

            // Секция инвентаря
            Text(
                text = "Предметы",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            InventoryGrid(
                inventoryItems = inventoryItems.filter { !it.isEquipped },
                character = character,
                onEquipItem = onEquipItem,
                onUseConsumable = onUseConsumable,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun EquipmentGrid(
    equippedItems: List<InventoryItem>,
    character: Character,
    onUnequipItem: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Группируем экипированные предметы по типам для удобного доступа
    val equippedByType = equippedItems.associateBy { it.itemType }

    Column(modifier = modifier) {
        // Первая строка: шлем, нагрудник, поножи
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Шлем
            EquipmentSlot(
                itemType = ItemType.HELMET,
                equippedItem = equippedByType[ItemType.HELMET],
                character = character,
                onUnequip = { item -> onUnequipItem(item) },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Нагрудник
            EquipmentSlot(
                itemType = ItemType.BREASTPLATE,
                equippedItem = equippedByType[ItemType.BREASTPLATE],
                character = character,
                onUnequip = { item -> onUnequipItem(item) },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Поножи
            EquipmentSlot(
                itemType = ItemType.GREAVES,
                equippedItem = equippedByType[ItemType.GREAVES],
                character = character,
                onUnequip = { item -> onUnequipItem(item) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Вторая строка: оружие, аксессуар
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Оружие
            EquipmentSlot(
                itemType = ItemType.WEAPON,
                equippedItem = equippedByType[ItemType.WEAPON],
                character = character,
                onUnequip = { item -> onUnequipItem(item) },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Аксессуар
            EquipmentSlot(
                itemType = ItemType.ACCESSORY,
                equippedItem = equippedByType[ItemType.ACCESSORY],
                character = character,
                onUnequip = { item -> onUnequipItem(item) },
                modifier = Modifier.weight(1f)
            )

            // Пустой слот для выравнивания (третья ячейка во второй строке)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun EquipmentSlot(
    itemType: ItemType,
    equippedItem: InventoryItem?,
    character: Character,
    onUnequip: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Название слота
        Text(
            text = getSlotName(itemType),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Ячейка для предмета
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (equippedItem != null) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    width = 2.dp,
                    color = if (equippedItem != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (equippedItem != null) {
                // Отображаем экипированный предмет с PNG спрайтом
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // PNG спрайт предмета
                    val spriteResId = SpriteUtils.getSpriteResId(equippedItem.spriteResId)
                    Image(
                        painter = painterResource(id = spriteResId),
                        contentDescription = equippedItem.name,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = equippedItem.name,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                // Пустой слот с иконкой
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = getEmptySlotEmoji(itemType),
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Пусто",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Кнопка "Снять"
        if (equippedItem != null) {
            Button(
                onClick = { onUnequip(equippedItem) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(36.dp), // Увеличили высоту кнопки
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    "Снять",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 12.sp
                )
            }
        } else {
            // Пустое место для выравнивания
            Spacer(modifier = Modifier.height(44.dp))
        }
    }
}

@Composable
fun InventoryGrid(
    inventoryItems: List<InventoryItem>,
    character: Character,
    onEquipItem: (InventoryItem) -> Unit,
    onUseConsumable: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(inventoryItems) { item ->
            InventoryItemCell(
                item = item,
                character = character,
                onEquip = { onEquipItem(item) },
                onUse = { onUseConsumable(item) },
                modifier = Modifier
            )
        }
    }
}

@Composable
fun InventoryItemCell(
    item: InventoryItem,
    character: Character,
    onEquip: () -> Unit,
    onUse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canUse = item.canUse(character)
    val isConsumable = item.itemType == ItemType.CONSUMABLE

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ячейка предмета
        Box(
            modifier = Modifier
                .size(80.dp) // Увеличили размер ячейки
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (canUse) {
                        when (item.rarity) {
                            com.example.habitrpg.data.model.ItemRarity.COMMON -> MaterialTheme.colorScheme.surface
                            com.example.habitrpg.data.model.ItemRarity.UNCOMMON -> Color(0xFFE8F5E8)
                            com.example.habitrpg.data.model.ItemRarity.RARE -> Color(0xFFE8F0FF)
                            com.example.habitrpg.data.model.ItemRarity.EPIC -> Color(0xFFF0E8FF)
                            com.example.habitrpg.data.model.ItemRarity.LEGENDARY -> Color(0xFFFFF0E8)
                        }
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                )
                .border(
                    width = 1.dp,
                    color = when (item.rarity) {
                        com.example.habitrpg.data.model.ItemRarity.COMMON -> Color.Gray
                        com.example.habitrpg.data.model.ItemRarity.UNCOMMON -> Color.Green
                        com.example.habitrpg.data.model.ItemRarity.RARE -> Color.Blue
                        com.example.habitrpg.data.model.ItemRarity.EPIC -> Color.Magenta
                        com.example.habitrpg.data.model.ItemRarity.LEGENDARY -> Color(0xFFFF8000)
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                    // При тапе на ячейку можно показать детальную информацию
                },
            contentAlignment = Alignment.Center
        ) {
            // PNG спрайт предмета
            if (item.spriteResId.isNotBlank()) {
                val spriteResId = SpriteUtils.getSpriteResId(item.spriteResId)
                Image(
                    painter = painterResource(id = spriteResId),
                    contentDescription = item.name,
                    modifier = Modifier.size(45.dp)
                )
            } else {
                // Fallback на эмодзи если нет спрайта
                Text(
                    text = getItemEmoji(item),
                    fontSize = 24.sp
                )
            }

            // Количество для расходников (в правом верхнем углу)
            if (isConsumable && item.stackSize > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.stackSize.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Название предмета
        Text(
            text = getShortItemName(item.name),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .height(32.dp)
                .padding(top = 4.dp),
            fontSize = 11.sp
        )

        // Кнопка действия
        when {
            isConsumable -> {
                Button(
                    onClick = onUse,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    enabled = canUse,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Использовать",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 10.sp
                    )
                }
            }
            else -> {
                Button(
                    onClick = onEquip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    enabled = canUse,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        "Надеть",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// Вспомогательные функции для получения названий

private fun getSlotName(itemType: ItemType): String {
    return when (itemType) {
        ItemType.HELMET -> "Шлем"
        ItemType.BREASTPLATE -> "Нагрудник"
        ItemType.GREAVES -> "Поножи"
        ItemType.WEAPON -> "Оружие"
        ItemType.ACCESSORY -> "Аксессуар"
        else -> "Слот"
    }
}

private fun getEmptySlotEmoji(itemType: ItemType): String {
    return when (itemType) {
        ItemType.HELMET -> "⛑️"
        ItemType.BREASTPLATE -> "👕"
        ItemType.GREAVES -> "👖"
        ItemType.WEAPON -> "⚔️"
        ItemType.ACCESSORY -> "💍"
        else -> "📦"
    }
}

// функция для эмодзи (если нет PNG спрайта)
private fun getItemEmoji(item: InventoryItem): String {
    return when (item.itemType) {
        ItemType.HELMET -> "⛑️"
        ItemType.BREASTPLATE -> "👕"
        ItemType.GREAVES -> "👖"
        ItemType.WEAPON -> "⚔️"
        ItemType.ACCESSORY -> "💍"
        ItemType.CONSUMABLE -> when {
            item.name.contains("здор", ignoreCase = true) -> "❤️"
            item.name.contains("мана", ignoreCase = true) -> "🔵"
            else -> "🧪"
        }
    }
}

private fun getShortItemName(fullName: String): String {
    return when {
        fullName.length <= 12 -> fullName
        fullName.contains(" ") -> fullName.split(" ").first()
        else -> fullName.take(10) + "..."
    }
}