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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.habitrpg.data.model.CharacterClass

enum class ClassSelectionType {
    NEW_CHARACTER,
    AFTER_DEATH
}

@Composable
fun ClassSelectionScreen(
    nickname: String,
    onClassSelected: (CharacterClass) -> Unit,
    selectionType: ClassSelectionType = ClassSelectionType.NEW_CHARACTER,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Заголовок в зависимости от типа выбора
        when (selectionType) {
            ClassSelectionType.NEW_CHARACTER -> {
                Text(
                    text = "Выберите класс для $nickname",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
            ClassSelectionType.AFTER_DEATH -> {
                Text(
                    text = "💀 Возрождение",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "$nickname, вы пали в бою!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Выберите новый класс для возрождения:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }

        ClassCard(
            characterClass = CharacterClass.WARRIOR,
            onSelected = onClassSelected,
            buttonText = if (selectionType == ClassSelectionType.AFTER_DEATH) "Возродиться" else "Выбрать",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        ClassCard(
            characterClass = CharacterClass.ARCHER,
            onSelected = onClassSelected,
            buttonText = if (selectionType == ClassSelectionType.AFTER_DEATH) "Возродиться" else "Выбрать",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        ClassCard(
            characterClass = CharacterClass.MAGE,
            onSelected = onClassSelected,
            buttonText = if (selectionType == ClassSelectionType.AFTER_DEATH) "Возродиться" else "Выбрать",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Дополнительная информация после смерти
        if (selectionType == ClassSelectionType.AFTER_DEATH) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "При возрождении вы сохраните:\n• Никнейм\n• Привычки и задачи",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ClassCard(
    characterClass: CharacterClass,
    onSelected: (CharacterClass) -> Unit,
    buttonText: String = "Выбрать",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = characterClass.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Статы
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("HP", characterClass.baseHp.toString())
                StatItem("MP", characterClass.baseMp.toString())
                StatItem("АТК", characterClass.baseAttack.toString())
                StatItem("ЗЩТ", characterClass.baseDefense.toString())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Пассивная способность
            Text(
                text = "Пассивная способность:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = characterClass.passiveDescription,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Бонусы
            Text(
                text = "Бонусы к: ${characterClass.bonusTags.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onSelected(characterClass) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}