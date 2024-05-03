package com.akhter.siliconlauncher13.tools.termux

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object LogMessageRepository {
    private val _logMessages = MutableLiveData<String>()
    val logMessages: LiveData<String> = _logMessages

    fun postLogMessage(message: String) {
        _logMessages.postValue(message)
    }
}
