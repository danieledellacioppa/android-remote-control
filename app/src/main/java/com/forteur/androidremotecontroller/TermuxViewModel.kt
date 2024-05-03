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

    fun sendCommand(lifecycleOwner: LifecycleOwner, command: String, args: Array<String>) {
        try {
            val executor = TermuxCommandExecutor(getApplication())
            executor.executeCommand(command, args)
            LogMessageRepository.logMessages.observe(lifecycleOwner, {
                _log.value = it
            })
        } catch (e: TermuxCommandException) {
            _log.value = "Error: ${e.message}"
        }
    }
}

