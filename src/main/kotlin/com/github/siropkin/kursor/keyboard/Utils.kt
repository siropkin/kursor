package com.github.siropkin.kursor.keyboard

import java.io.BufferedReader
import java.io.IOException

object Utils {
//    fun simulateMacKeyPress(key: String): String {
//        return executeNativeCommand(arrayOf("osascript", "-e", String.format("tell application \"System Events\" to keystroke \"%s\"", key)))
//    }
//
//    fun simulateMacKeyPress(key: String): String {
//        return executeNativeCommand(arrayOf("osascript", "-e", """
//            tell application "System Events"
//                --set oldClipboard to the clipboard
//                --set the clipboard to ""
//                keystroke "$key"
//                --keystroke "a" using command down
//                --keystroke "c" using command down
//                --key code 51
//                --set result to the clipboard
//                --set the clipboard to oldClipboard
//                return result
//            end tell
//        """.trimIndent()))
//    }

    fun executeNativeCommand(command: Array<String>): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            process.inputStream.bufferedReader().use(BufferedReader::readText)
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
}