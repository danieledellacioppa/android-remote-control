package com.forteur.androidremotecontroller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akhter.siliconlauncher13.tools.termux.LogMessageRepository
import com.forteur.androidremotecontroller.tools.termux.TermuxCommandException
import com.forteur.androidremotecontroller.tools.termux.TermuxCommandExecutor
import java.io.File

class TermuxViewModel(application: Application) : AndroidViewModel(application) {
    private val _events = MutableLiveData<List<CommandEvent>>(listOf())
    val events: LiveData<List<CommandEvent>> = _events

    // Variabile per memorizzare l'output del file hosts
    private val _hostsFileContent = MutableLiveData<String>()
    val hostsFileContent: LiveData<String> = _hostsFileContent

    init {
        LogMessageRepository.logMessages.observeForever { output ->
            appendEvent(CommandEvent.Output(output))
        }
    }

    private val _deviceIp = MutableLiveData<String>("192.168.0.159")  // Default IP address
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

    private fun appendEvent(event: CommandEvent) {
        val updatedEvents = _events.value?.toMutableList() ?: mutableListOf()
        updatedEvents.add(event)
        _events.postValue(updatedEvents)
    }

    override fun onCleared() {
        super.onCleared()
        LogMessageRepository.logMessages.removeObserver { /* Implement proper removal */ }
    }

//    fun modifyHostsFile() {
//        val file = File("./hosts")
//        val content = file.readText()
//
//        // Esempio di modifica - aggiungere una nuova riga al file hosts
//        val modifiedContent = content + "\n127.0.0.1 my.local.dev"
//
//        file.writeText(modifiedContent)
//    }

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


}


sealed class CommandEvent {
    data class Success(val message: String) : CommandEvent()
    data class Error(val error: String) : CommandEvent()
    data class Output(val output: String) : CommandEvent()
}



