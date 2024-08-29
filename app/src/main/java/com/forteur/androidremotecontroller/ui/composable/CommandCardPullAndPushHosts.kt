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
import com.forteur.androidremotecontroller.R
import com.forteur.androidremotecontroller.TermuxViewModel
import com.forteur.androidremotecontroller.tools.termux.AdbCommands

@Composable
fun CommandCardPullAndPushHosts(viewModel: TermuxViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                viewModel.sendCommand(AdbCommands.PULL_HOSTS.getFullCommand(viewModel.deviceIp.value ?: "192.168.0.159")[0], arrayOf())
//                viewModel.modifyHostsFile() // Modifica il file una volta scaricato
//                viewModel.sendCommand(AdbCommands.PUSH_HOSTS.getFullCommand(viewModel.deviceIp.value ?: "192.168.0.159")[0], arrayOf())
            },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_push_hosts),
                contentDescription = "Pull and Push Hosts File",
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Pull and Push Hosts File",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
