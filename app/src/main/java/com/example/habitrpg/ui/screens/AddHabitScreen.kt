package com.example.habitrpg.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habitrpg.data.model.QuestDifficulty
import com.example.habitrpg.data.model.QuestType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onSaveHabit: (
        title: String,
        description: String,
        difficulty: QuestDifficulty
    ) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(QuestDifficulty.MEDIUM) }
    var difficultyExpanded by remember { mutableStateOf(false) }

    val trimmedTitle = title.trim()
    val isTitleValid = trimmedTitle.isNotBlank() && trimmedTitle.length <= 30
    val showTitleError = title.isNotBlank() && !isTitleValid
    val titleCharCount = title.length

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая привычка") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Название
                    OutlinedTextField(
                        value = title,
                        onValueChange = { if (it.length <= 30) title = it },
                        label = { Text("Название привычки*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Например: Утренняя зарядка") },
                        isError = showTitleError,
                        supportingText = { Text("$titleCharCount/30 символов") }
                    )

                    if (showTitleError) {
                        Text(
                            text = when {
                                trimmedTitle.isEmpty() -> "Название не может состоять только из пробелов"
                                else -> "Название не может превышать 30 символов"
                            },
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Описание
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Описание (необязательно)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 3,
                        placeholder = { Text("Описание вашей привычки") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Сложность
                    ExposedDropdownMenuBox(
                        expanded = difficultyExpanded,
                        onExpandedChange = { difficultyExpanded = !difficultyExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDifficulty.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Сложность") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = difficultyExpanded,
                            onDismissRequest = { difficultyExpanded = false }
                        ) {
                            QuestDifficulty.values().forEach { difficulty ->
                                DropdownMenuItem(
                                    text = { Text("${difficulty.displayName} (${difficulty.expReward} EXP)") },
                                    onClick = {
                                        selectedDifficulty = difficulty
                                        difficultyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Кнопки
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text("Отмена")
                        }

                        Button(
                            onClick = {
                                if (isTitleValid) {
                                    onSaveHabit(
                                        title.trim(),
                                        description.trim(),
                                        selectedDifficulty
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                            enabled = isTitleValid
                        ) {
                            Text("Создать привычку")
                        }
                    }
                }
            }
        }
    }
}