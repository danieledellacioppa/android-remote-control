package com.forteur.androidremotecontroller

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.forteur.androidremotecontroller.ui.theme.AndroidRemoteControllerTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: TermuxViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[TermuxViewModel::class.java]

        window.statusBarColor = getColor(R.color.status_bar_color) // probably not producing any effect
        window.navigationBarColor = getColor(R.color.status_bar_color) // Assicurati che status_bar_color sia definito in colors.xml

        setContent {
            AndroidRemoteControllerTheme {
                // Imposta lo sfondo della tua app
                val backgroundColor = MaterialTheme.colorScheme.background
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Gray) {
                    TermuxInterface(viewModel)
                }
            }
        }

    }
}

object AdbCommands {
    const val ADB_PATH = "/data/data/com.termux/files/usr/bin/adb"
    val CONNECT = CommandDetails(arrayOf(ADB_PATH, "connect", "192.168.0.152"), R.drawable.icon_connect)
    val DEVICES = CommandDetails(arrayOf(ADB_PATH, "devices"), R.drawable.icon_devices)
    val REBOOT = CommandDetails(arrayOf(ADB_PATH, "reboot"), R.drawable.icon_reboot)
    val HOME = CommandDetails(arrayOf(ADB_PATH, "shell", "input", "keyevent", "KEYCODE_HOME"), R.drawable.icon_home)
    val SHUTDOWN = CommandDetails(arrayOf(ADB_PATH, "shell", "reboot", "-p"), R.drawable.icon_shutdown)
    val BACK = CommandDetails(arrayOf(ADB_PATH, "shell", "input", "keyevent", "KEYCODE_BACK"), R.drawable.icon_back)
    val TOGGLE_SCREEN = CommandDetails(arrayOf(ADB_PATH, "shell", "input", "keyevent", "KEYCODE_POWER"), R.drawable.toggle_screen)

    // Lista di tutti i comandi per l'uso in LazyVerticalGrid
    val commands = listOf(
        Pair("Connect to device", CONNECT),
        Pair("Devices connected", DEVICES),
        Pair("Reboot device", REBOOT),
        Pair("Home", HOME),
        Pair("Shutdown", SHUTDOWN),
        Pair("Back", BACK),
        Pair("Toggle screen", TOGGLE_SCREEN)
    )
}
data class CommandDetails(val command: Array<String>, val icon: Int)

@Composable
fun TermuxInterface(viewModel: TermuxViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val log = viewModel.log.observeAsState("")

    Column(modifier = Modifier.fillMaxSize()) {  // Main column that wraps everything
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                .fillMaxWidth()
                .heightIn(max = 200.dp)  // Limits the Box height to a maximum of 200.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Output Termux:", fontWeight = FontWeight.Bold)
                // Adding vertical scroll to text if content is larger than the box
                Text(text = log.value ?: "", modifier = Modifier.verticalScroll(rememberScrollState()))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))  // Adds space between the box and the grid

        // LazyVerticalGrid that displays the command buttons
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // Set the number of columns
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)  // Takes up all remaining space
        ) {
            items(AdbCommands.commands) { command ->
                CommandCard(
                    label = command.first,
                    command = command.second.command,
                    icon = command.second.icon,
                    viewModel = viewModel,
                    lifecycleOwner = lifecycleOwner
                )
            }
        }
    }
}

@Composable
fun CommandCard(label: String, command: Array<String>, icon: Int, viewModel: TermuxViewModel, lifecycleOwner: LifecycleOwner) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)  // Aggiunge spazio intorno alla card per una separazione visiva
            .clickable {
                viewModel.sendCommand(lifecycleOwner, command[0], command.sliceArray(1 until command.size))
            },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)  // Padding interno per spaziare uniformemente i contenuti
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,  // Centra il contenuto orizzontalmente
            verticalArrangement = Arrangement.Center  // Centra il contenuto verticalmente
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier
                    .padding(bottom = 8.dp)  // Spazio tra l'icona e il testo
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface  // Assicura che il testo sia leggibile sullo sfondo della card
            )
        }
    }
}