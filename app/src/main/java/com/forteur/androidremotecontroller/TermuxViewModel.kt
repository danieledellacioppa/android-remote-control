package com.forteur.androidremotecontroller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.forteur.androidremotecontroller.tools.termux.TermuxCommandException
import com.forteur.androidremotecontroller.tools.termux.TermuxCommandExecutor

class TermuxViewModel(application: Application) : AndroidViewModel(application) {
    private val _log = MutableLiveData<String>()
    val log: LiveData<String> = _log

    fun sendCommand(command: String, args: Array<String>) {
        try {
            val executor = TermuxCommandExecutor(getApplication())
            executor.executeCommand(command, args)
            // Aggiorna il log con l'output appropriato
        } catch (e: TermuxCommandException) {
            _log.value = "Error: ${e.message}"
        }
    }
}
