package com.forteur.androidremotecontroller.ui.composable

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.forteur.androidremotecontroller.TermuxViewModel
import com.forteur.androidremotecontroller.tools.termux.AdbCommands
import java.io.File
import java.io.FileOutputStream

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

        item {
            InstallApkButton(LocalContext.current)
        }
    }
}

@Composable
fun InstallApkButton(context: Context) {
    val activity = context as? Activity

    Button(
        onClick = {
            showInstallDialog(context)
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text("Installa Termux!")
    }
}

private fun showInstallDialog(context: Context) {
    val dialog = AlertDialog.Builder(context)
        .setTitle("Installazione APK")
        .setMessage("Vuoi installare l'APK inclusa nell'app?")
        .setPositiveButton("Installa") { dialog, _ ->
            dialog.dismiss() // Chiude il dialog prima di procedere
            installApk(context)
        }
        .setNegativeButton("Annulla", null)
        .create()

    dialog.show()
}


private fun installApk(context: Context) {
    Handler(Looper.getMainLooper()).post {
        try{
            val apkFileName = "termux-app_apt-android-7-debug_armeabi-v7a.apk" // Nome corretto
            val apkFile = File(context.externalCacheDir, apkFileName)

            val assetFiles = context.assets.list("")?.toList()
            Log.d("Assets", "File disponibili: $assetFiles")


            context.assets.open(apkFileName).use { input ->
                FileOutputStream(apkFile).use { output ->
                    input.copyTo(output)
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("InstallAPK", "Errore durante l'installazione: ${e.message}")
        }
    }
}

