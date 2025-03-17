package com.forteur.androidremotecontroller.utils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)

        setContent {
            SettingsScreen(settingsRepository)
        }
    }
}

@Composable
fun SettingsScreen(settingsRepository: SettingsRepository) {
    var serverAddress by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        settingsRepository.serverAddress.collect { savedAddress ->
            serverAddress = savedAddress
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Server DNS/IP:")
        BasicTextField(
            value = serverAddress,
            onValueChange = { serverAddress = it },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                coroutineScope.launch {
                    settingsRepository.setServerAddress(serverAddress)
                }
            }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    settingsRepository.setServerAddress(serverAddress)
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Salva")
        }
    }
}
