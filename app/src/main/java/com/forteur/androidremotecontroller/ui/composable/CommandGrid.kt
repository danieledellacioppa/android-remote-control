package com.forteur.androidremotecontroller.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forteur.androidremotecontroller.TermuxViewModel
import com.forteur.androidremotecontroller.tools.termux.AdbCommands

/**
 * Displays a grid of command cards, each corresponding to a different action or command
 * that can be executed. This grid layout is dynamically filled with command options
 * defined in the AdbCommands object.
 *
 * Each card within the grid represents a command and is interactive. When a user taps on
 * one of these cards, the associated command is executed via the viewModel.
 *
 * ### Usage:
 * This function is meant to be used in a UI where multiple commands need to be displayed
 * and accessed easily. The grid format provides a scalable way to handle multiple commands
 * and can be extended or modified with additional commands as needed. It ensures that
 * the commands are spaced evenly and are visually accessible.
 *
 * ### Example:
 * The `CommandGrid` is typically used in a main screen or a command control panel where
 * users can quickly view and interact with various device management options, enhancing
 * the user experience by providing direct and efficient access to important functionalities.
 *
 * @param viewModel The TermuxViewModel instance used for executing commands when command cards
 *        are interacted with. This view model handles the command execution logic and updates based on the result.
 *
 */
@Composable
fun CommandGrid(viewModel: TermuxViewModel) {
    val ip = viewModel.deviceIp.observeAsState()
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(AdbCommands.commands) { command ->
            CommandCard(
                label = command.first,
                command = command.second.getFullCommand(ip.value ?: "192.168.0.159"),
                icon = command.second.icon,
                viewModel = viewModel
            )
        }
    }
}