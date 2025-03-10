package com.forteur.androidremotecontroller

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.forteur.androidremotecontroller.tools.termux.LogMessageRepository
import com.forteur.androidremotecontroller.tools.termux.TermuxCommandExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket

@OptIn(ExperimentalMaterial3Api::class)
class AkhterSetupActivity : ComponentActivity() {

    private val BROADCAST_PORT = 8888
    private lateinit var executor: TermuxCommandExecutor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        executor = TermuxCommandExecutor(applicationContext)

        setContent {
            AkhterSetupScreen { ip -> executeAkhterSetup(ip) }
        }
    }

    @Composable
    fun AkhterSetupScreen(onStartSetup: (String) -> Unit) {
        var ip by remember { mutableStateOf("") }
        var logText by remember { mutableStateOf("Logs:\n") }
        val coroutineScope = rememberCoroutineScope()
        val logMessages by LogMessageRepository.logMessages.observeAsState("")

        // Unico LaunchedEffect per gestire Broadcast e parsing LogMessageRepository
        LaunchedEffect(Unit) {
            coroutineScope.launch(Dispatchers.IO) {
                listenForBroadcast { message, ip ->
                    coroutineScope.launch(Dispatchers.Main) {
                        logText += "\n[BROADCAST] $message"
                    }
                    when {
                        message.contains("AKHTER PAIR") -> executeAdbCommands(ip)
                        message.contains("AKHTER DONE") -> sendHomeIntent(ip)
                    }
                }
            }
        }

        // Osserva LogMessageRepository per gestire risposte ADB
        LaunchedEffect(logMessages) {
            logText += "\n$logMessages"

            when {
                logMessages.contains("connected to") -> Log.d("AkhterSetup", "ADB connesso a $ip!")
                logMessages.contains("Success") -> Log.d("AkhterSetup", "Comando ADB eseguito con successo.")
                logMessages.contains("Error") -> Log.e("AkhterSetup", "Errore ADB rilevato: $logMessages")
                logMessages.contains("AKHTER PAIR") -> executeAdbCommands(ip)
                logMessages.contains("AKHTER DONE") -> sendHomeIntent(ip)
//                logMessages.contains("package:com.akhter.aosplauncher") &&
//                        logMessages.contains("package:com.xbh.launcher") -> {
//                    Log.d("AkhterSetup", "Entrambi i launcher trovati. Riavvio in recovery.")
//                    runAdbCommand(arrayOf("-s", ip, "reboot", "recovery"))
//                }
                logMessages.contains("package:com.akhter.aosplauncher") -> {
                    Log.d("AkhterSetup", "Akhter Launcher trovato, avvio pairing.")
                    triggerAkhterPair(ip)
                }
                logMessages.contains("package:com.xbh.launcher") -> {
                    Log.d("AkhterSetup", "Chinese launcher trovato, installo Akhter Launcher.")
                    installApk(ip)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Enter Device IP:")
            BasicTextField(
                value = ip,
                onValueChange = { ip = it },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onStartSetup(ip) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Button(
                onClick = { onStartSetup(ip) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Start Setup")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = logText,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    private fun executeAkhterSetup(ip: String) {
        Log.d("AkhterSetup", "Connecting to $ip via ADB...")
        runAdbCommand(arrayOf("connect", ip))
        runAdbCommand(arrayOf("-s", ip, "shell", "pm list packages"))
    }

    private fun listenForBroadcast(onMessageReceived: (String, String) -> Unit) {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val multicastLock = wifiManager.createMulticastLock("AkhterSetupLock")
        multicastLock.setReferenceCounted(true)
        multicastLock.acquire() // Abilita la ricezione di pacchetti UDP broadcast

        try {
            val socket = DatagramSocket(BROADCAST_PORT)
            val buffer = ByteArray(1024)

            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)

                val message = String(packet.data, 0, packet.length).trim()
                val parts = message.split(" ")
                if (parts.size < 2) continue // Messaggio non valido


                val ipAddress = parts[0]
                val command = parts.drop(1).joinToString(" ")

                Log.d("AkhterSetup", "Received UDP message: $command from $ipAddress")
                onMessageReceived(command, ipAddress)
            }
        } catch (e: Exception) {
            Log.e("AkhterSetup", "Error in broadcast receiver: ${e.message}")
        } finally {
            multicastLock.release() // Rilascia il lock per evitare problemi di rete
        }
    }

    private fun installApk(ip: String) {
        // TODO : utilizzeremo Termux per dire scaricare la apk dal mio repo.
        runAdbCommand(arrayOf("-s", ip, "install", "-r", "/path/to/AkhterSecureLauncher.apk"))
    }

    private fun triggerAkhterPair(ip: String) {
        runAdbCommand(arrayOf("-s", ip, "shell", "am", "start", "-n", "com.akhter.aosplauncher/.AkhterPair"))
    }

    private fun runAdbCommand(args: Array<String>) {
        Log.d("AkhterSetup", "Eseguendo comando: ${args.joinToString(" ")}")
        executor.executeCommand("/data/data/com.termux/files/usr/bin/adb", args)
    }

    private fun executeAdbCommands(ipAddress: String) {
        runAdbCommand(arrayOf("connect", ipAddress))
        runAdbCommand(arrayOf("-s", ipAddress, "shell",
            "dpm", "set-device-owner", "com.akhter.aosplauncher/com.akhter.aosplauncher.receiver.MyDeviceAdminReceiver"))
    }

    private fun sendHomeIntent(ipAddress: String) {
        runAdbCommand(arrayOf("-s", ipAddress, "shell", "input", "keyevent", "KEYCODE_HOME"))
    }
}
