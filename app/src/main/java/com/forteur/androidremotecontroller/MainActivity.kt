package com.forteur.androidremotecontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.forteur.androidremotecontroller.tools.termux.AdbCommands
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



@Composable
fun TermuxInterface(viewModel: TermuxViewModel) {
    val events = viewModel.events.observeAsState(listOf())
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .verticalScroll(scrollState)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                events.value?.forEach { event ->
                    Text(
                        text = when (event) {
                            is CommandEvent.Output -> event.output
                            is CommandEvent.Error -> event.error
                            is CommandEvent.Success -> event.message
                        },
                        color = when (event) {
                            is CommandEvent.Output -> Color.White
                            is CommandEvent.Error -> Color.Red
                            is CommandEvent.Success -> Color.Green
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LaunchedEffect(key1 = events.value) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        Spacer(modifier = Modifier.height(16.dp))

        CommandGrid(viewModel = viewModel)
    }
}



@Composable
fun CommandGrid(viewModel: TermuxViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Set the number of columns
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(AdbCommands.commands) { command ->
            CommandCard(
                label = command.first,
                command = command.second.command,
                icon = command.second.icon,
                viewModel = viewModel
            )
        }
    }
}


@Composable
fun CommandCard(label: String, command: Array<String>, icon: Int, viewModel: TermuxViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)  // Adds visual separation space around the card
            .clickable {
                // Here we update how sendCommand is called, we only need to pass the command and its arguments.
                viewModel.sendCommand(command[0], command.sliceArray(1 until command.size))
            },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)  // Internal padding to evenly space the contents
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,  // Centers the content horizontally
            verticalArrangement = Arrangement.Center  // Centers the content vertically
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier
                    .padding(bottom = 8.dp)  // Space between the icon and the text
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface  // Ensures the text is readable on the card background
            )
        }
    }
}
