package com.forteur.androidremotecontroller.tools.termux

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.akhter.siliconlauncher13.tools.termux.LogMessageRepository
import com.termux.shared.termux.TermuxConstants

// we should covert IntentService to JobIntentService or WorkManager
class PluginResultsService : IntentService("PluginResultsService") {

    override fun onHandleIntent(intent: Intent?) {
        intent ?: return

        Log.d(LOG_TAG, "$PLUGIN_SERVICE_LABEL received execution result")

        val resultBundle = intent.getBundleExtra(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE)
        if (resultBundle == null) {
            Log.e(LOG_TAG, "The intent does not contain the result bundle at the" +
                    " \"${TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE}\" key.")
            return
        }

        val executionId = intent.getIntExtra(EXTRA_EXECUTION_ID, 0)

        // Qui puoi gestire i risultati come preferisci
        Log.d(LOG_TAG, "Execution id $executionId result:\n" +
                "stdout:\n```\n${resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT, "")}\n```\n" +
                "stdout_original_length: `" + resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT_ORIGINAL_LENGTH) + "`\n" +
                "stderr:\n```\n" + resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR, "") + "\n```\n" +
                "stderr_original_length: `" + resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR_ORIGINAL_LENGTH) + "`\n" +
                "exitCode: `" + resultBundle.getInt(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE) + "`\n" +
                "errCode: `" + resultBundle.getInt(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERR) + "`\n" +
                "errmsg: `${resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG, "")}`")

        LogMessageRepository.postLogMessage(
                  "Termux:Result id:$executionId:" +
                  "${resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT, "")}"
//                "stdout_original_length: `" + resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT_ORIGINAL_LENGTH) + "`\n" +
//                "stderr:\n```\n" + resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR, "") + "\n```\n" +
//                "stderr_original_length: `" + resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR_ORIGINAL_LENGTH) + "`\n" +
//                "exitCode: `" + resultBundle.getInt(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE) + "`\n" +
//                "errCode: `" + resultBundle.getInt(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERR) + "`\n" +
//                "errmsg: `${resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG, "")}`"
        )
    }

    companion object {
        const val EXTRA_EXECUTION_ID = "execution_id"
        private var EXECUTION_ID = 1000
        const val PLUGIN_SERVICE_LABEL = "PluginResultsService"
        private val LOG_TAG = PluginResultsService::class.java.simpleName

        @Synchronized
        fun getNextExecutionId() = EXECUTION_ID++
    }
}
