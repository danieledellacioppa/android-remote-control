package com.forteur.androidremotecontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forteur.androidremotecontroller.ui.theme.AndroidRemoteControllerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidRemoteControllerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TermuxInterface(viewModel = TermuxViewModel(application = application))
                }
            }
        }
    }
}

@Composable
fun TermuxInterface(viewModel: TermuxViewModel = TermuxViewModel(application = android.app.Application())) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val log = viewModel.log.observeAsState("")

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Output Termux:", fontWeight = FontWeight.Bold)
        Text(text = log.value ?: "")

        Button(onClick = {
            viewModel.sendCommand(lifecycleOwner, "/data/data/com.termux/files/usr/bin/adb", arrayOf("connect", "192.168.0.152"))
        }) {
            Text("Connect to device")
        }
        Button(onClick = {
            viewModel.sendCommand(lifecycleOwner, "/data/data/com.termux/files/usr/bin/adb", arrayOf("devices"))
        }) {
            Text("Devices connected")
        }
        Button(onClick = {
            viewModel.sendCommand(lifecycleOwner, "/data/data/com.termux/files/usr/bin/adb", arrayOf("reboot"))
        }) {
            Text("Reboot device")
        }
        Button(onClick = {
            viewModel.sendCommand(lifecycleOwner, "/data/data/com.termux/files/usr/bin/adb", arrayOf("shell", "input", "keyevent", "KEYCODE_HOME"))
        }) {
            Text("Home")
        }
    }
}