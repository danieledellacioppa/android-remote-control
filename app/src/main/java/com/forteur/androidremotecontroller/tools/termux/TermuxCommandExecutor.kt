package com.forteur.androidremotecontroller.tools.termux

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.akhter.siliconlauncher13.tools.termux.LogMessageRepository
import com.termux.shared.termux.TermuxConstants

class TermuxCommandExecutor(private val context: Context) {

    fun executeCommand(commandPath: String, arguments: Array<String>) {
        val intent = Intent().apply {
            setClassName(
                TermuxConstants.TERMUX_PACKAGE_NAME,
                TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE_NAME
            )
            action = TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND
            putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_COMMAND_PATH, commandPath)
            putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_ARGUMENTS, arguments)
            putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_BACKGROUND, true)
            putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_SESSION_ACTION, "0")
            putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_COMMAND_LABEL, "run $commandPath ${arguments.joinToString(" ")}")
            putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_COMMAND_DESCRIPTION, "Runs $commandPath ${arguments.joinToString(" ")}")
        }

        // Preparazione per ricevere i risultati del comando
        val executionId = PluginResultsService.getNextExecutionId()
        val pluginResultsServiceIntent = Intent(context, PluginResultsService::class.java).apply {
            putExtra(PluginResultsService.EXTRA_EXECUTION_ID, executionId)
        }
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        val pendingIntent = PendingIntent.getService(context, executionId, pluginResultsServiceIntent, PendingIntent.FLAG_ONE_SHOT or pendingIntentFlag)

        intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_PENDING_INTENT, pendingIntent)


        try {
            Log.d(LOG_TAG, "Sending execution command: $commandPath ${arguments.joinToString(" ")}")
            LogMessageRepository.postLogMessage("Sending execution command: $executionId $commandPath ${arguments.joinToString(" ")}")
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to start execution command: ${e.message}")
            throw TermuxCommandException("Failed to execute command: ${e.message}")
        }
    }

    companion object {
        private const val LOG_TAG = "TermuxCommandExecutor"
    }
}

class TermuxCommandException(message: String): Exception(message)

