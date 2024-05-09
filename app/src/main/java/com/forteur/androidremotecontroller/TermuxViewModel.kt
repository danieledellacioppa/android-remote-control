package com.forteur.androidremotecontroller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akhter.siliconlauncher13.tools.termux.LogMessageRepository
import com.forteur.androidremotecontroller.tools.termux.TermuxCommandException
import com.forteur.androidremotecontroller.tools.termux.TermuxCommandExecutor

class TermuxViewModel(application: Application) : AndroidViewModel(application) {
    private val _events = MutableLiveData<List<CommandEvent>>(listOf())
    val events: LiveData<List<CommandEvent>> = _events

    init {
        LogMessageRepository.logMessages.observeForever { output ->
            appendEvent(CommandEvent.Output(output))
        }
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
}


sealed class CommandEvent {
    data class Success(val message: String) : CommandEvent()
    data class Error(val error: String) : CommandEvent()
    data class Output(val output: String) : CommandEvent()
}



