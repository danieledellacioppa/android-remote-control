package com.forteur.androidremotecontroller.ui.composable

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.forteur.androidremotecontroller.TermuxViewModel
import java.lang.reflect.Modifier

@Composable
fun EditHostsDialog(viewModel: TermuxViewModel, onDismiss: () -> Unit) {
    val hostsContent by viewModel.hostsFileContent.observeAsState("")

    var content by remember { mutableStateOf(hostsContent) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Edit Hosts File") },
        text = {
            Column {
                TextField(
                    value = content,
                    onValueChange = {
                        content = it
                        Log.d("EditHostsDialog", "Content updated to: $content")
                    },
                    label = { Text("Hosts Content") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Log.d("EditHostsDialog", "Saving content: $content")
                    viewModel.saveModifiedHostsContent(content)
                    viewModel.pushModifiedHostsToRemote()
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}




