package com.forteur.androidremotecontroller

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
fun TermuxInterface(viewModel: TermuxViewModel) {
    val log = viewModel.log.observeAsState("")

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Output Termux:", fontWeight = FontWeight.Bold)
        Text(text = log.value)
        Button(onClick = {
            Log.d("MainActivity", "Invio comando")
            viewModel.sendCommand("/data/data/com.termux/files/usr/bin/adb", arrayOf("devices"))
        }) {
            Text("Invia Comando")
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidRemoteControllerTheme {
        Greeting("Android")
    }
}