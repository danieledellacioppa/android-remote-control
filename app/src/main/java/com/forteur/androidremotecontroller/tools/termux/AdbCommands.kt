package com.forteur.androidremotecontroller.tools.termux

import com.forteur.androidremotecontroller.R

object AdbCommands {
    const val ADB_PATH = "/data/data/com.termux/files/usr/bin/adb"
    val CONNECT = CommandDetails(arrayOf(ADB_PATH, "connect", "192.168.0.152"), R.drawable.icon_connect)
    val DEVICES = CommandDetails(arrayOf(ADB_PATH, "devices"), R.drawable.icon_devices)
    val REBOOT = CommandDetails(arrayOf(ADB_PATH, "reboot"), R.drawable.icon_reboot)
    val HOME = CommandDetails(arrayOf(ADB_PATH, "shell", "input", "keyevent", "KEYCODE_HOME"), R.drawable.icon_home)
    val SHUTDOWN = CommandDetails(arrayOf(ADB_PATH, "shell", "reboot", "-p"), R.drawable.icon_shutdown)
    val BACK = CommandDetails(arrayOf(ADB_PATH, "shell", "input", "keyevent", "KEYCODE_BACK"), R.drawable.icon_back)
    val TOGGLE_SCREEN = CommandDetails(arrayOf(ADB_PATH, "shell", "input", "keyevent", "KEYCODE_POWER"), R.drawable.toggle_screen)

    // Lista di tutti i comandi per l'uso in LazyVerticalGrid
    val commands = listOf(
        Pair("Connect to device", CONNECT),
        Pair("Devices connected", DEVICES),
        Pair("Reboot device", REBOOT),
        Pair("Home", HOME),
        Pair("Shutdown", SHUTDOWN),
        Pair("Back", BACK),
        Pair("Toggle screen", TOGGLE_SCREEN)
    )
}
data class CommandDetails(val command: Array<String>, val icon: Int)