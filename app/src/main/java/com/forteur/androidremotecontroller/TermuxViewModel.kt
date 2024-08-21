package com.forteur.androidremotecontroller

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akhter.siliconlauncher13.tools.termux.LogMessageRepository
import com.forteur.androidremotecontroller.tools.termux.AdbCommands
import com.forteur.androidremotecontroller.tools.termux.TermuxCommandException
import com.forteur.androidremotecontroller.tools.termux.TermuxCommandExecutor
import java.io.File

class TermuxViewModel(application: Application) : AndroidViewModel(application) {
    private val _events = MutableLiveData<List<CommandEvent>>(listOf())
    val events: LiveData<List<CommandEvent>> = _events

    private val _hostsFileContent = MutableLiveData<String>()
    val hostsFileContent: LiveData<String> = _hostsFileContent

    // Variabile per memorizzare il contenuto modificato degli hosts
    private var modifiedHostsContent: String? = null

    private var isCapturingHosts = false

    init {
        LogMessageRepository.setViewModel(this)
    }

    private val _deviceIp = MutableLiveData<String>("192.168.0.159")
    val deviceIp: LiveData<String> = _deviceIp

    fun updateDeviceIp(newIp: String) {
        _deviceIp.value = newIp
    }

    fun sendCommand(command: String, args: Array<String>) {
        appendEvent(CommandEvent.Output("Executing command: $command ${args.joinToString(" ")}"))
        try {
            val executor = TermuxCommandExecutor(getApplication())
            executor.executeCommand(command, args)
            appendEvent(CommandEvent.Success("Command executed successfully"))
        } catch (e: TermuxCommandException) {
            appendEvent(CommandEvent.Error("Failed to execute command: ${e.message}"))
        }
    }

    fun appendEvent(event: CommandEvent) {
        val updatedEvents = _events.value?.toMutableList() ?: mutableListOf()
        updatedEvents.add(event)
        _events.postValue(updatedEvents)
    }

    override fun onCleared() {
        super.onCleared()
        LogMessageRepository.logMessages.removeObserver { /* Implement proper removal */ }
    }

    fun modifyHostsFile(content: String) {
        val file = File("./hosts")
        file.writeText(content)
    }

    fun readHostsFile(): String {
        val file = File("./hosts")
        return if (file.exists()) {
            file.readText()
        } else {
            "File not found."
        }
    }

    fun saveHostsFile(content: String) {
        val file = File("./hosts")
        file.writeText(content)
    }

    // Aggiorna il contenuto del file hosts in LiveData
    fun updateHostsFileContent(content: String) {
        _hostsFileContent.postValue(content.trim())
    }

    // Salva il contenuto modificato in una variabile
    fun saveModifiedHostsContent(content: String) {
        modifiedHostsContent = content
    }

    // Metodo per inviare il comando ad adb per aggiornare il file hosts
    fun pushModifiedHostsToRemote() {
        val content = modifiedHostsContent ?: return
        val command = "/data/data/com.termux/files/usr/bin/adb"
        val args = arrayOf("shell", "echo", content, ">", "/etc/hosts")
        sendCommand(command, args)
    }
}



sealed class CommandEvent {
    data class Success(val message: String) : CommandEvent()
    data class Error(val error: String) : CommandEvent()
    data class Output(val output: String) : CommandEvent()
}



