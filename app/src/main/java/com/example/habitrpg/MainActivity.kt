package com.example.habitrpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.habitrpg.ui.screens.*
import com.example.habitrpg.ui.theme.HabitRPGTheme
import com.example.habitrpg.ui.viewmodel.CharacterViewModel
import com.example.habitrpg.ui.viewmodel.CharacterViewModelFactory
import com.example.habitrpg.ui.viewmodel.InventoryViewModel
import com.example.habitrpg.ui.viewmodel.InventoryViewModelFactory
import com.example.habitrpg.ui.viewmodel.PurchaseResult
import com.example.habitrpg.ui.viewmodel.QuestViewModel
import com.example.habitrpg.ui.viewmodel.QuestViewModelFactory
import com.example.habitrpg.ui.viewmodel.ShopViewModel
import com.example.habitrpg.ui.viewmodel.ShopViewModelFactory

class MainActivity : ComponentActivity() {

    private val characterViewModel: CharacterViewModel by viewModels {
        CharacterViewModelFactory((application as HabitApplication).repository)
    }

    private val questViewModel: QuestViewModel by viewModels {
        QuestViewModelFactory((application as HabitApplication).repository)
    }

    private val inventoryViewModel: InventoryViewModel by viewModels {
        InventoryViewModelFactory((application as HabitApplication).repository)
    }

    private val shopViewModel: ShopViewModel by viewModels { // Добавляем ShopViewModel
        ShopViewModelFactory((application as HabitApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HabitRPGTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val character by characterViewModel.character.collectAsState()
                    val quests by questViewModel.quests.collectAsState()
                    val characterDeath by questViewModel.characterDeathEvent.collectAsState()
                    val inventoryItems by inventoryViewModel.inventoryItems.collectAsState()
                    val equippedItems by inventoryViewModel.equippedItems.collectAsState()
                    val shopItems by shopViewModel.shopItems.collectAsState()
                    val purchaseResult by shopViewModel.purchaseResult.collectAsState()

                    var showSplashScreen by remember { mutableStateOf(true) }
                    var showAddHabitScreen by remember { mutableStateOf(false) }
                    var showAddDailyScreen by remember { mutableStateOf(false) }
                    var showAddTaskScreen by remember { mutableStateOf(false) }
                    var showInventoryScreen by remember { mutableStateOf(false) }
                    var showClassSelectionAfterDeath by remember { mutableStateOf(false) }

                    // Выполняем обслуживание задач при каждом запуске основного экрана
                    LaunchedEffect(character) {
                        if (character != null) {
                            questViewModel.performMaintenanceIfNeeded(character!!)
                        }
                    }

                    // Отслеживаем событие смерти персонажа (включая начальное состояние)
                    LaunchedEffect(characterDeath) {
                        if (characterDeath) {
                            showClassSelectionAfterDeath = true
                        }
                    }

                    // Таймер для заставки (2 секунды)
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        showSplashScreen = false
                    }

                    purchaseResult?.let { result ->
                        LaunchedEffect(result) {
                            kotlinx.coroutines.delay(3000)
                            shopViewModel.clearPurchaseResult()
                        }
                    }

                    // Если персонаж мертв, показываем экран выбора класса
                    if (showClassSelectionAfterDeath) {
                        ClassSelectionScreen(
                            nickname = character?.nickname ?: "Герой",
                            onClassSelected = { newClass ->
                                questViewModel.resetCharacterOnDeathWithNewClass(newClass)
                                showClassSelectionAfterDeath = false
                                characterViewModel.reloadCharacter()
                            },
                            selectionType = ClassSelectionType.AFTER_DEATH
                        )
                    } else if (showSplashScreen) {
                        SplashScreen()
                    } else {
                        // Основная логика приложения
                        if (character == null) {
                            // Процесс создания персонажа
                            var currentStep by remember { mutableStateOf("nickname") }
                            var playerNickname by remember { mutableStateOf("") }

                            when (currentStep) {
                                "nickname" -> {
                                    NicknameScreen(
                                        onNicknameSet = { nickname ->
                                            playerNickname = nickname
                                            currentStep = "class"
                                        }
                                    )
                                }
                                "class" -> {
                                    ClassSelectionScreen(
                                        nickname = playerNickname,
                                        onClassSelected = { selectedClass ->
                                            characterViewModel.createCharacter(playerNickname, selectedClass)
                                        },
                                        selectionType = ClassSelectionType.NEW_CHARACTER
                                    )
                                }
                            }
                        } else {
                            // Навигация между экранами
                            when {
                                showInventoryScreen -> {
                                    InventoryScreen(
                                        character = character!!,
                                        inventoryItems = inventoryItems,
                                        equippedItems = equippedItems,
                                        onEquipItem = { item ->
                                            inventoryViewModel.equipItem(item, character!!)
                                        },
                                        onUnequipItem = { item ->
                                            inventoryViewModel.unequipItem(item, character!!)
                                        },
                                        onUseConsumable = { item ->
                                            inventoryViewModel.useConsumable(item, character!!)
                                        },
                                        onBack = { showInventoryScreen = false }
                                    )
                                }
                                showAddHabitScreen -> {
                                    AddHabitScreen(
                                        onSaveHabit = { title, description, difficulty ->
                                            questViewModel.addHabit(title, description, difficulty)
                                            showAddHabitScreen = false
                                        },
                                        onCancel = { showAddHabitScreen = false }
                                    )
                                }
                                showAddDailyScreen -> {
                                    AddDailyScreen(
                                        onSaveDaily = { title, description, difficulty, days ->
                                            questViewModel.addDaily(title, description, difficulty, days)
                                            showAddDailyScreen = false
                                        },
                                        onCancel = { showAddDailyScreen = false }
                                    )
                                }
                                showAddTaskScreen -> {
                                    AddTaskScreen(
                                        onSaveTask = { title, description, difficulty, deadline ->
                                            questViewModel.addTask(title, description, difficulty, deadline)
                                            showAddTaskScreen = false
                                        },
                                        onCancel = { showAddTaskScreen = false }
                                    )
                                }
                                else -> {
                                    // Основной экран с персонажем
                                    CharacterScreen(
                                        character = character!!,
                                        quests = quests,
                                        shopItems = shopItems,
                                        inventoryItems = inventoryItems,
                                        equippedItems = equippedItems,
                                        onAddHabit = { showAddHabitScreen = true },
                                        onAddDaily = { showAddDailyScreen = true },
                                        onAddTask = { showAddTaskScreen = true },
                                        onCompleteQuest = { quest -> questViewModel.completeQuest(quest, character!!) },
                                        onFailQuest = { quest -> questViewModel.failQuest(quest, character!!) },
                                        onIncrementHabit = { quest -> questViewModel.incrementHabit(quest, character!!) },
                                        onDecrementHabit = { quest -> questViewModel.decrementHabit(quest, character!!) },
                                        onDeleteQuest = { quest -> questViewModel.deleteQuest(quest) },
                                        onOpenInventory = { showInventoryScreen = true },
                                        onBuyItem = { item -> shopViewModel.buyItem(item, character!!) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Экран-заставка
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚔️",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "HabitRPG",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Продуктивность в стиле RPG",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}