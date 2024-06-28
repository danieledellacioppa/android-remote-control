package com.forteur.androidremotecontroller.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forteur.androidremotecontroller.CommandEvent
import com.forteur.androidremotecontroller.TermuxViewModel

/**
 * Constructs the primary user interface for displaying command outputs and a grid of command buttons.
 * This interface includes a scrollable output area at the top and a grid of actionable command cards below.
 *
 * The output area displays the results of executed commands, updating dynamically as new outputs, errors,
 * or success messages are generated. It provides immediate feedback from background operations, enhancing
 * the interactivity of the application. The colors of the text in the output area indicate the nature of the
 * messages: standard outputs are white, errors are red, and success messages are green.
 *
 * Below the output area, a grid layout displays command buttons that users can interact with to execute commands.
 *
 * ### Usage:
 * - This function is designed to be the main content of the MainActivity or any other relevant activity that requires
 *   direct interaction with the command execution functionalities.
 * - It is suitable for scenarios where a user needs to send commands to a system and receive feedback, such as
 *   a remote terminal or a device management interface.
 *
 * ### Example:
 * The `TermuxInterface` would typically be used in an application where users need to manage devices or services
 * through command execution, and immediate feedback on those commands is crucial for effective operation.
 *
 * @param viewModel The TermuxViewModel instance which provides the command execution functionality and holds the
 *        state of command outputs. This ViewModel is central to handling the logic for command processing and
 *        updating the UI accordingly.
 *
 */

@Composable
fun TermuxInterface(viewModel: TermuxViewModel) {
    val events = viewModel.events.observeAsState(listOf())
    val ip = viewModel.deviceIp.observeAsState()
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = ip.value ?: "",
            onValueChange = { viewModel.updateDeviceIp(it) },
            label = { Text("Device IP") },
            singleLine = true,
            modifier = Modifier.padding(16.dp)
        )
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