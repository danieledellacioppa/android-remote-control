package com.forteur.androidremotecontroller.tools.termux

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * This Temux Command Worker should do in its doWork a while loop which keeps repeating until
 * the TermuxCommandExecutor stops raising exceptions.
 *
 * USAGE:
 *
 *         // Create a WorkRequest in your activity or fragment (MainActivity.kt)
 *         val workRequest = OneTimeWorkRequestBuilder<TermuxCommandsWorker>().build()
 *         // Enqueue the work request
 *         WorkManager.getInstance(this).enqueue(workRequest)
 */
class TermuxCommandsWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val executor = TermuxCommandExecutor(applicationContext)
        var attempts = 0
        val maxAttempts = 50

        while (attempts < maxAttempts) {
            try {
                for (i in 0..2) {
                    executor.executeCommand("/data/data/com.termux/files/usr/bin/adb", arrayOf("shell","rm","-r","/mnt/sdcard/Annotation"))
                    Thread.sleep(900)
                    executor.executeCommand("/data/data/com.termux/files/usr/bin/adb", arrayOf("shell","rm","-r","/mnt/sdcard/whiteboard"))
                    Thread.sleep(900)
                    executor.executeCommand("/data/data/com.termux/files/usr/bin/adb", arrayOf("shell","rm","-r","/mnt/sdcard/Pictures/.thumbnails"))
                    Thread.sleep(900)
                    executor.executeCommand("/data/data/com.termux/files/usr/bin/adb", arrayOf("shell","rm","-r","/mnt/sdcard/Android/data/com.xbh.gallery/cache"))
                    Thread.sleep(900)
                    executor.executeCommand("/data/data/com.termux/files/usr/bin/adb", arrayOf("shell","rm","-r","/mnt/sdcard/Android/data/com.xbh.whiteboard"))
                    Thread.sleep(900)
                    executor.executeCommand("/data/data/com.termux/files/usr/bin/adb", arrayOf("shell","pm","clear","com.xbh.whiteboard"))
                    Thread.sleep(900)
                    executor.executeCommand("/data/data/com.termux/files/usr/bin/adb", arrayOf("shell","pm","clear","com.xbh.whiteboard","-d"))
                    Thread.sleep(900)
                    executor.executeCommand("/data/data/com.termux/files/usr/bin/adb", arrayOf("root"))
                    Thread.sleep(2900)
                    executor.executeCommand("/data/data/com.termux/files/usr/bin/adb", arrayOf("shell","whoami"))
                }
                // Se non ci sono state eccezioni, il comando è stato eseguito con successo
                return Result.success()
            } catch (e: TermuxCommandException) {
                Log.e("TermuxCommandsWorker", "Attempt $attempts failed: ${e.message}")
                attempts++

                val packageManager = applicationContext.packageManager
                launchIntent(packageManager, "com.termux")
                Thread.sleep(1000) // Aspetta un po' prima di riprovare
                launchIntent(packageManager, "com.akhter.siliconlauncher13")
            }
        }

        // Se si arriva qui, significa che tutti i tentativi sono falliti
        return Result.failure()
    }

    private fun launchIntent(packageManager: PackageManager, packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            applicationContext.startActivity(intent)
        }
    }
}

