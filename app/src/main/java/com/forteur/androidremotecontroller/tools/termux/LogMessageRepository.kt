package com.akhter.siliconlauncher13.tools.termux

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.forteur.androidremotecontroller.CommandEvent
import com.forteur.androidremotecontroller.TermuxViewModel

object LogMessageRepository {
    private val _logMessages = MutableLiveData<String?>()
    val logMessages: MutableLiveData<String?> = _logMessages

    private var termuxViewModel: TermuxViewModel? = null

    fun postLogMessage(message: String?) {
        if (message.isNullOrBlank()) {
            Log.d("LogMessageRepository", "Received empty or null message, skipping...")
            return
        }

        Log.d("LogMessageRepository", "Posting log message: $message")
        _logMessages.postValue(message)

        // Aggiungi l'output del comando agli eventi del ViewModel
        termuxViewModel?.appendEvent(CommandEvent.Output(message))

        // Controlla se il messaggio contiene l'output di "cat /etc/hosts"
        if (message.contains("Termux:Result") && message.lines().any { it.contains("localhost") }) {
            Log.d("LogMessageRepository", "Captured hosts content")
            val hostsContent = message.substringAfter("Termux:Result id:").trim()
            termuxViewModel?.updateHostsFileContent(hostsContent)
        }
    }


    fun setViewModel(viewModel: TermuxViewModel) {
        termuxViewModel = viewModel
    }
}



