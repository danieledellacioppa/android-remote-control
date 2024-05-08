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
    private val _log = MutableLiveData<String>()
    val log: LiveData<String> = _log

    init {
        LogMessageRepository.logMessages.observeForever {
            val currentLog = _log.value ?: ""
            _log.value = if (currentLog.isEmpty()) it else "$currentLog\n$it"
        }
    }

    fun sendCommand(command: String, args: Array<String>) {
        try {
            val executor = TermuxCommandExecutor(getApplication())
            executor.executeCommand(command, args)
        } catch (e: TermuxCommandException) {
            appendToLog("Error: ${e.message}")
        }
    }

    private fun appendToLog(message: String) {
        val currentLog = _log.value ?: ""
        _log.value = if (currentLog.isEmpty()) message else "$currentLog\n$message"
    }

    override fun onCleared() {
        super.onCleared()
        LogMessageRepository.logMessages.removeObserver { /* Observer you added */ }
    }
}


