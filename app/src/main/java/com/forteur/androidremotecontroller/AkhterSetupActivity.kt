package com.forteur.androidremotecontroller

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.forteur.androidremotecontroller.tools.termux.LogMessageRepository
import com.forteur.androidremotecontroller.tools.termux.TermuxCommandExecutor
import com.forteur.androidremotecontroller.utils.SettingsActivity
import com.forteur.androidremotecontroller.utils.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket

@OptIn(ExperimentalMaterial3Api::class)
class AkhterSetupActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    private val BROADCAST_PORT = 8888
    private lateinit var executor: TermuxCommandExecutor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)

        executor = TermuxCommandExecutor(applicationContext)

        setContent {
            val serverAddress by settingsRepository.serverAddress.collectAsState(initial = "http://akhterlauncherota.duckdns.org:12348")

            // Avvio "wget" con output in wget_log.txt
            val apkUrl = "$serverAddress/com.akhter.aosplauncher.apk"
            // Esegui la wget una sola volta, appena entri in composable
            LaunchedEffect(Unit) {
                executor.executeCommand(
                    "/data/data/com.termux/files/usr/bin/sh",
                    arrayOf(
                        "-c",
                        "wget --progress=dot:mega --header='Authorization: Bearer mio_token_super_segreto' " +
                                "$apkUrl -O /data/data/com.termux/files/home/com.akhter.aosplauncher.apk " +
                                "2>&1 | tee /data/data/com.termux/files/home/wget_log.txt"
                    )
                )
            }

            AkhterSetupScreen { ip -> executeAkhterSetup(ip) }
        }
    }

    @Composable
    fun AkhterSetupScreen(onStartSetup: (String) -> Unit) {
        var ip by remember { mutableStateOf("") }
        var logText by remember { mutableStateOf("Logs:\n") }
        val coroutineScope = rememberCoroutineScope()
        val logMessages by LogMessageRepository.logMessages.observeAsState("")

        // Stato dello scroll per LazyColumn
        val listState = rememberLazyListState()

//        val logLines = logMessages.split("\n")
        val logLines = logText.split("\n")

        // Effetto per scrollare automaticamente quando arriva un nuovo log
        LaunchedEffect(logMessages) {
            listState.animateScrollToItem( logLines.size - 1)
        }

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

        // *** NUOVO: Effetto per fare polling di "cat wget_log.txt" ogni 5 secondi ***
        LaunchedEffect(Unit) {
            val pollingInterval = 3000L       // 5 secondi
            val maxNoChangeTries = 10
            var noChangeCount = 0
            var previousPollOutput = ""       // Contenuto polled in precedenza

            while (true) {
                // 1) Esegui "cat wget_log.txt" tramite TermuxCommandExecutor
                executor.executeCommand(
                    "/data/data/com.termux/files/usr/bin/tail",
                    arrayOf("-n", "2", "/data/data/com.termux/files/home/wget_log.txt")
                )

                // 2) Aspetta 5 secondi
                delay(pollingInterval)

                // 3) Controlla se abbiamo raggiunto "100%"
                if (logMessages.contains("100%")) {
                    // consideriamo la wget terminata
                    Log.d("AkhterSetup", "Rilevato 100% => Fine download, stop polling.")
                    break
                }

                // 4) Controlla se l'output non è cambiato
                if (logMessages == previousPollOutput) {
                    noChangeCount++
                } else {
                    noChangeCount = 0
                    previousPollOutput = logMessages
                }

                if (noChangeCount >= maxNoChangeTries) {
                    Log.d("AkhterSetup", "Log invariato per $maxNoChangeTries polling => stop.")
                    break
                }
            }

            // Qui siamo fuori dal while => non eseguiamo più polling
            LogMessageRepository.postLogMessage(
                "\n** STOP polling wget_log.txt (finito o bloccato) **\n"
            )
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Button(
                    onClick = {
                        val intent = Intent(applicationContext, SettingsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        applicationContext.startActivity(intent)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Impostazioni")
                }

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
                // **Colonna separata per il log**
                Column(
                    modifier = Modifier
                        .weight(0.4f) // Occupa il 40% della larghezza
                        .fillMaxHeight()
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, MaterialTheme.colorScheme.primary)
                ) {

                    Text("Logs:", modifier = Modifier.padding(8.dp))
                    Text("logLines: ${logLines.size}")

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState
                        ) {
                            items(logLines.size) { index ->
                                Text(text = logLines[index])
                            }
                        }
                    }
                }
            }
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
        Log.d("AkhterSetup", "Issuing installAndLaunch command...")
        executor.executeCommand("/data/data/com.termux/files/home/installAndLaunch", arrayOf(ip))
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