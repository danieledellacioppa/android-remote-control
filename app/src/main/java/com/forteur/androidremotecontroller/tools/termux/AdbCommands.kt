package com.forteur.androidremotecontroller.tools.termux

import com.forteur.androidremotecontroller.R

object AdbCommands {
    const val ADB_PATH = "/data/data/com.termux/files/usr/bin/adb"
    val CONNECT = CommandDetails(arrayOf(ADB_PATH), arrayOf("connect"), R.drawable.icon_connect, true)
    val DEVICES = CommandDetails(arrayOf(ADB_PATH), arrayOf("devices"), R.drawable.icon_devices)
    val REBOOT = CommandDetails(arrayOf(ADB_PATH), arrayOf("reboot"), R.drawable.icon_reboot)
    val HOME = CommandDetails(arrayOf(ADB_PATH), arrayOf("shell", "input", "keyevent", "KEYCODE_HOME"), R.drawable.icon_home)
    val SHUTDOWN = CommandDetails(arrayOf(ADB_PATH), arrayOf("shell", "reboot", "-p"), R.drawable.icon_shutdown)
    val BACK = CommandDetails(arrayOf(ADB_PATH), arrayOf("shell", "input", "keyevent", "KEYCODE_BACK"), R.drawable.icon_back)
    val TOGGLE_SCREEN = CommandDetails(arrayOf(ADB_PATH), arrayOf("shell", "input", "keyevent", "KEYCODE_POWER"), R.drawable.toggle_screen)
    val SELECT_VIDEO_INPUT = CommandDetails(arrayOf(ADB_PATH), arrayOf("shell", "input", "keyevent", "KEYCODE_TV_INPUT"), R.drawable.video_input_source)
    val SIGNAL_SOURCE = CommandDetails(arrayOf(ADB_PATH), arrayOf("shell", "input", "keyevent", "2158"), R.drawable.signale_source)
    val SETTINGS = CommandDetails(arrayOf(ADB_PATH), arrayOf("shell", "input", "keyevent", "KEYCODE_SETTINGS"), R.drawable.icon_settings)
    val MENU = CommandDetails(arrayOf(ADB_PATH), arrayOf("shell", "input", "keyevent", "KEYCODE_MENU"), R.drawable.icon_menu)
    val PULL_HOSTS = CommandDetails(arrayOf(ADB_PATH), arrayOf("pull", "/etc/hosts", "."), R.drawable.icon_pull_hosts)
    val SHOW_HOSTS = CommandDetails(arrayOf(ADB_PATH), arrayOf("shell", "cat", "/etc/hosts"), R.drawable.icon_pull_hosts)
    val PUSH_HOSTS = CommandDetails(arrayOf(ADB_PATH), arrayOf("push", "./hosts", "/etc/hosts"), R.drawable.icon_push_hosts)
    val ADB_ROOT = CommandDetails(arrayOf(ADB_PATH), arrayOf("root"), R.drawable.adb_root)
    val ADB_REMOUNT = CommandDetails(arrayOf(ADB_PATH), arrayOf("remount"), R.drawable.adb_remount)

    // Lista di tutti i comandi per l'uso in LazyVerticalGrid
    val commands = listOf(
        Pair("Connect to device", CONNECT),
        Pair("Show device list", DEVICES),
        Pair("Reboot device", REBOOT),
        Pair("Home", HOME),
        Pair("Shutdown", SHUTDOWN),
        Pair("Back", BACK),
        Pair("Toggle screen", TOGGLE_SCREEN),
        Pair("Select video input", SELECT_VIDEO_INPUT),
        Pair("Signal source", SIGNAL_SOURCE),
        Pair("Settings", SETTINGS),
        Pair("Menu", MENU),
        Pair("Pull hosts file", PULL_HOSTS),
        Pair("Push hosts file", PUSH_HOSTS),
        Pair("Show hosts file", SHOW_HOSTS),
        Pair("ADB root", ADB_ROOT),
        Pair("ADB remount", ADB_REMOUNT)
    )
}
data class CommandDetails(val commandPrefix: Array<String>, val commandSuffix: Array<String>, val icon: Int, val ipAtEnd: Boolean = false) {
    fun getFullCommand(ip: String): Array<String> {
        if (ipAtEnd) {
            // Aggiunge l'IP alla fine del comando
            return commandPrefix + commandSuffix + arrayOf(ip)
        } else {
            // Inserisce l'IP tra il prefisso e il suffisso
            return commandPrefix + arrayOf("-s", ip) + commandSuffix
        }
    }
}

