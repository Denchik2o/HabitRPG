package com.example.habitrpg.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.habitrpg.data.model.Character
import com.example.habitrpg.data.model.InventoryItem
import com.example.habitrpg.data.model.Quest
import com.example.habitrpg.utils.SpriteUtils

@Composable
fun CharacterScreen(
    character: Character,
    quests: List<Quest>,
    shopItems: List<InventoryItem>,
    inventoryItems: List<InventoryItem>,
    equippedItems: List<InventoryItem>,
    onAddHabit: () -> Unit,
    onAddDaily: () -> Unit,
    onAddTask: () -> Unit,
    onCompleteQuest: (Quest) -> Unit,
    onFailQuest: (Quest) -> Unit,
    onIncrementHabit: (Quest) -> Unit,
    onDecrementHabit: (Quest) -> Unit,
    onDeleteQuest: (Quest) -> Unit,
    onOpenInventory: () -> Unit,
    onBuyItem: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("habits") }
    var questToDelete by remember { mutableStateOf<Quest?>(null) }

    // Диалог подтверждения удаления
    if (questToDelete != null) {
        AlertDialog(
            onDismissRequest = { questToDelete = null },
            title = { Text("Удалить задачу?") },
            text = { Text("Задача \"${questToDelete!!.title}\" будет удалена без награды и без штрафа.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteQuest(questToDelete!!)
                        questToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(
                    onClick = { questToDelete = null }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            when (selectedTab) {
                "habits" -> {
                    FloatingActionButton(
                        onClick = onAddHabit,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить привычку")
                    }
                }
                "dailies" -> {
                    FloatingActionButton(
                        onClick = onAddDaily,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить ежедневную задачу")
                    }
                }
                "tasks" -> {
                    FloatingActionButton(
                        onClick = onAddTask,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Информация о персонаже
            CharacterInfoSection(
                character = character,
                equippedItems = equippedItems,
                onOpenInventory = onOpenInventory,
                modifier = Modifier.padding(16.dp)
            )

            // Контент в зависимости от выбранной вкладки
            when (selectedTab) {
                "habits" -> {
                    HabitsContentSection(
                        habits = quests.filter { it.questType == "HABIT" },
                        character = character,
                        onIncrementHabit = onIncrementHabit,
                        onDecrementHabit = onDecrementHabit,
                        onDeleteQuest = { questToDelete = it }
                    )
                }
                "dailies" -> {
                    DailiesContentSection(
                        dailies = quests.filter { it.questType == "DAILY" },
                        character = character,
                        onCompleteQuest = onCompleteQuest,
                        onFailQuest = onFailQuest,
                        onDeleteQuest = { questToDelete = it }
                    )
                }
                "tasks" -> {
                    TasksContentSection(
                        tasks = quests.filter { it.questType == "TASK" },
                        character = character,
                        onCompleteQuest = onCompleteQuest,
                        onFailQuest = onFailQuest,
                        onDeleteQuest = { questToDelete = it }
                    )
                }
                "shop" -> {
                    ShopScreen(
                        character = character,
                        shopItems = shopItems,
                        inventoryItems = inventoryItems,
                        onBuyItem = onBuyItem,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterInfoSection(
    character: Character,
    equippedItems: List<InventoryItem>,
    onOpenInventory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Никнейм и кнопка инвентаря
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = character.nickname,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Кнопка инвентаря
            IconButton(
                onClick = onOpenInventory,
                modifier = Modifier.size(48.dp)
            ) {
                Text(
                    text = "🎒",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Спрайт и характеристики в ряд
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            CharacterSprite(
                character = character,
                equippedItems = equippedItems,
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight()
            )

            CharacterStats(
                character = character,
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
fun CharacterSprite(
    character: Character,
    equippedItems: List<InventoryItem>,
    modifier: Modifier = Modifier
) {
    val sortedItems = remember(equippedItems) {
        equippedItems.sortedBy { item ->
            when (item.itemType) {
                com.example.habitrpg.data.model.ItemType.HELMET -> 10
                com.example.habitrpg.data.model.ItemType.BREASTPLATE -> 20
                com.example.habitrpg.data.model.ItemType.GREAVES -> 30

                com.example.habitrpg.data.model.ItemType.ACCESSORY -> 40
                com.example.habitrpg.data.model.ItemType.WEAPON -> 50

                else -> 100
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val baseSpriteResId = SpriteUtils.getBaseSpriteForClass(character.characterClass)
        Image(
            painter = painterResource(id = baseSpriteResId),
            contentDescription = "Базовый спрайт персонажа",
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.Center)
        )

        sortedItems.forEach { item ->
            if (item.spriteResId.isNotBlank() &&
                item.itemType != com.example.habitrpg.data.model.ItemType.CONSUMABLE) {

                val itemSpriteResId = SpriteUtils.getSpriteResId(item.spriteResId)
                Image(
                    painter = painterResource(id = itemSpriteResId),
                    contentDescription = "Экипированный предмет: ${item.name}",
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center)
                )
            }
        }

        Text(
            text = getClassDisplayName(character.characterClass),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun CharacterStats(
    character: Character,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Уровень: ${character.level}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Прогресс бар опыта
            ExperienceBar(
                currentExp = character.exp,
                maxExp = character.getExpForNextLevel(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // HP и Мана
            StatRow("HP:", "${character.currentHp}/${character.maxHp}")
            StatRow("Мана:", "${character.currentMp}/${character.maxMp}")

            Spacer(modifier = Modifier.height(8.dp))

            // Золото, Атака, Защита в одну строку со значениями справа
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Золото
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🪙",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = character.gold.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Атака
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💪",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = character.attack.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Защита
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🛡️",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = character.defense.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExperienceBar(
    currentExp: Int,
    maxExp: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (maxExp > 0) currentExp.toFloat() / maxExp else 0f

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.small)
                .background(Color.Gray.copy(alpha = 0.3f))
        ) {
            // Заполненная часть
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Нижняя панель навигации со смайликами
@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = selectedTab == "habits",
            onClick = { onTabSelected("habits") },
            icon = {
                Text(
                    text = "🗿",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            label = { Text("Привычки") }
        )

        NavigationBarItem(
            selected = selectedTab == "dailies",
            onClick = { onTabSelected("dailies") },
            icon = {
                Text(
                    text = "📅",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            label = { Text("Ежедневные") }
        )

        NavigationBarItem(
            selected = selectedTab == "tasks",
            onClick = { onTabSelected("tasks") },
            icon = {
                Text(
                    text = "✅",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            label = { Text("Задачи") }
        )

        NavigationBarItem(
            selected = selectedTab == "shop",
            onClick = { onTabSelected("shop") },
            icon = {
                Text(
                    text = "🛒",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            label = { Text("Магазин") }
        )
    }
}

// Секция Привычек
@Composable
fun HabitsContentSection(
    habits: List<Quest>,
    character: Character,
    onIncrementHabit: (Quest) -> Unit,
    onDecrementHabit: (Quest) -> Unit,
    onDeleteQuest: (Quest) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (habits.isEmpty()) {
            EmptyStateSection(
                title = "Привычек пока нет",
                description = "Нажмите + чтобы добавить первую привычку"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(habits) { habit ->
                    QuestItem(
                        quest = habit,
                        character = character,
                        onComplete = { },
                        onFail = { },
                        onIncrement = { onIncrementHabit(habit) },
                        onDecrement = { onDecrementHabit(habit) },
                        onDelete = { onDeleteQuest(habit) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// Секция Ежедневных задач
@Composable
fun DailiesContentSection(
    dailies: List<Quest>,
    character: Character,
    onCompleteQuest: (Quest) -> Unit,
    onFailQuest: (Quest) -> Unit,
    onDeleteQuest: (Quest) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (dailies.isEmpty()) {
            EmptyStateSection(
                title = "Ежедневных задач пока нет",
                description = "Нажмите + чтобы добавить первую ежедневную задачу"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(dailies) { daily ->
                    QuestItem(
                        quest = daily,
                        character = character,
                        onComplete = { onCompleteQuest(daily) },
                        onFail = { onFailQuest(daily) },
                        onIncrement = { },
                        onDecrement = { },
                        onDelete = { onDeleteQuest(daily) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// Секция Разовых задач
@Composable
fun TasksContentSection(
    tasks: List<Quest>,
    character: Character,
    onCompleteQuest: (Quest) -> Unit,
    onFailQuest: (Quest) -> Unit,
    onDeleteQuest: (Quest) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (tasks.isEmpty()) {
            EmptyStateSection(
                title = "Задач пока нет",
                description = "Нажмите + чтобы добавить первую задачу"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(tasks) { task ->
                    QuestItem(
                        quest = task,
                        character = character,
                        onComplete = { onCompleteQuest(task) },
                        onFail = { onFailQuest(task) },
                        onIncrement = { },
                        onDecrement = { },
                        onDelete = { onDeleteQuest(task) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// Общая секция для пустых состояний
@Composable
fun EmptyStateSection(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun QuestItem(
    quest: Quest,
    character: Character,
    onComplete: () -> Unit,
    onFail: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    val shouldTruncateDescription = quest.description.length > 50
    val displayDescription = if (shouldTruncateDescription && !isDescriptionExpanded) {
        quest.description.take(50) + "..."
    } else {
        quest.description
    }

    val dailyStatus = quest.getDailyStatus()

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок с кнопкой удаления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                }
            }

            // Тип задачи и статус для ежедневных
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quest.getQuestTypeDisplayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (quest.questType == "DAILY") {
                    Text(
                        text = dailyStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (dailyStatus) {
                            "сегодня не нужно" -> MaterialTheme.colorScheme.secondary
                            "выполнено" -> MaterialTheme.colorScheme.primary
                            "провалено" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            // Дополнительная информация в зависимости от типа
            when (quest.questType) {
                "DAILY" -> {
                    val dayNames = mapOf(
                        "Пн" to "Пн", "Вт" to "Вт", "Ср" to "Ср",
                        "Чт" to "Чт", "Пт" to "Пт", "Сб" to "Сб", "Вс" to "Вс"
                    )
                    val activeDays = quest.weekDays.joinToString(", ") { day ->
                        dayNames[day] ?: day
                    }
                    Text(
                        text = "Дни: $activeDays",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                "TASK" -> {
                    quest.deadline?.let { deadline ->
                        val status = quest.getDeadlineStatus()
                        Text(
                            text = "Дедлайн: $status",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                quest.isOverdue -> MaterialTheme.colorScheme.error
                                status.contains("сегодня") -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                "HABIT" -> {
                    Text(
                        text = "Счетчик: ${quest.habitCounter}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (quest.description.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = shouldTruncateDescription) {
                            isDescriptionExpanded = !isDescriptionExpanded
                        }
                ) {
                    Text(
                        text = "Описание: $displayDescription",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (shouldTruncateDescription) {
                        Text(
                            text = if (isDescriptionExpanded) "Нажмите, чтобы свернуть" else "Нажмите, чтобы развернуть",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Сложность
            Text(
                text = "Сложность: ${quest.getDifficultyDisplayName()}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Разные кнопки для разных типов задач
            when (quest.questType) {
                "HABIT" -> {
                    // Для привычек: кнопки + и -
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onDecrement,
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("-")
                        }

                        Button(
                            onClick = onIncrement,
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("+")
                        }
                    }
                }
                "DAILY" -> {
                    // Для ежедневных: кнопки только если задача активна сегодня
                    if (quest.isActiveToday()) {
                        if (!quest.isCompleted) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = onFail,
                                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                                ) {
                                    Text("Провалено")
                                }

                                Button(
                                    onClick = onComplete,
                                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                                ) {
                                    Text("Выполнено")
                                }
                            }
                        } else {
                            Text(
                                text = if (quest.isFailed) "❌ Задача провалена" else "✅ Задача выполнена",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (quest.isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    } else {
                        // Задача неактивна сегодня - показываем информационное сообщение
                        Text(
                            text = "📅 Сегодня не нужно выполнять",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
                else -> {
                    // Для разовых задач
                    if (!quest.isCompleted && quest.canBeCompleted()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = onFail,
                                modifier = Modifier.weight(1f).padding(end = 4.dp),
                                enabled = quest.canBeFailed()
                            ) {
                                Text("Провалено")
                            }

                            Button(
                                onClick = onComplete,
                                modifier = Modifier.weight(1f).padding(start = 4.dp),
                                enabled = quest.canBeCompleted()
                            ) {
                                Text("Выполнено")
                            }
                        }
                    } else {
                        Text(
                            text = when {
                                quest.isFailed -> "❌ Задача провалена"
                                !quest.canBeCompleted() && quest.questType == "TASK" -> "⏰ Время вышло"
                                else -> "✅ Задача выполнена"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                quest.isFailed -> MaterialTheme.colorScheme.error
                                !quest.canBeCompleted() && quest.questType == "TASK" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

// Вспомогательная функция для получения отображаемого имени класса
private fun getClassDisplayName(characterClass: String): String {
    return when (characterClass) {
        "WARRIOR" -> "Воин"
        "ARCHER" -> "Лучник"
        "MAGE" -> "Маг"
        else -> "Неизвестно"
    }
}