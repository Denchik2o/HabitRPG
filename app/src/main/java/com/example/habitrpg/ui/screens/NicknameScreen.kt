package com.example.habitrpg.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NicknameScreen(
    onNicknameSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var nickname by remember { mutableStateOf("") }
    val isNicknameValid = nickname.isNotBlank() && nickname.length <= 15
    val nicknameCharCount = nickname.length

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Придумайте никнейм",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = {
                        if (it.length <= 15) {
                            nickname = it
                        }
                    },
                    label = { Text("Никнейм*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Введите никнейм (макс. 15 символов)") },
                    isError = nickname.isNotBlank() && !isNicknameValid,
                    supportingText = {
                        Text("$nicknameCharCount/15 символов")
                    }
                )

                if (nickname.isNotBlank() && !isNicknameValid) {
                    Text(
                        text = when {
                            nickname.trim().isEmpty() -> "Никнейм не может состоять только из пробелов"
                            else -> "Никнейм не может превышать 15 символов"
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isNicknameValid) {
                            onNicknameSet(nickname.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = isNicknameValid
                ) {
                    Text(
                        text = "Продолжить",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}