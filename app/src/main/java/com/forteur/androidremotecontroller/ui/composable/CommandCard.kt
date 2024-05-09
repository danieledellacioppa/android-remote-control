package com.forteur.androidremotecontroller.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.forteur.androidremotecontroller.TermuxViewModel

/**
 * Creates a clickable card for each command that displays an icon and a label.
 * When clicked, the card triggers the execution of the specified command using the TermuxViewModel.
 *
 * The card layout includes an image icon at the top followed by a label text below it.
 * This structure is used to provide a uniform appearance for all command cards within the app's UI.
 *
 * @param label The text label displayed on the card which describes the command.
 * @param command An array of strings where the first element is the command to be executed and
 *        the subsequent elements are the arguments for the command.
 * @param icon The drawable resource ID for the icon image displayed at the top of the card.
 * @param viewModel The view model instance that provides the function to execute commands.
 *        It is used here to invoke commands when the card is clicked.
 *
 * Usage:
 * The `CommandCard` function is intended to be used within a UI that lists multiple command options,
 * each represented by a card. When a card is clicked, it uses the provided `viewModel` to execute
 * the associated command and handle the result asynchronously.
 */
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