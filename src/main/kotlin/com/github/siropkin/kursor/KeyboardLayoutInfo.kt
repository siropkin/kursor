package com.github.siropkin.kursor

import java.awt.im.InputContext
import java.io.BufferedReader
import java.io.IOException

class KeyboardLayout(private val country: String, private val language: String) {
    override fun toString(): String = country.lowercase().ifEmpty { language.lowercase() }
}

class KeyboardLayoutInfo {
    private val unknownCountry = "unk"
    private val unknownLanguage = ""

    private val os: String = System.getProperty("os.name").lowercase()
    private var linuxDistribution: String = System.getenv("DESKTOP_SESSION")?.lowercase() ?: ""
    private var linuxNonUbuntuKeyboardCountries: List<String> = emptyList()

    fun getLayout(): KeyboardLayout {
        return when {
            os.startsWith("linux") -> getLinuxKeyboardLayout()
            os.startsWith("win") -> getWindowsKeyboardLayout()
            os.startsWith("mac") -> getMacKeyboardLayout()
            else -> KeyboardLayout(unknownCountry, unknownLanguage)
        }
    }

    private fun getLinuxKeyboardLayout(): KeyboardLayout {
        // This is not the ideal solution because it involves executing a shell command to know the current keyboard layout
        // which might affect the performance. And we have different commands for different Linux distributions.
        // But it is the only solution I found that works on Linux.
        // For Linux we know only keyboard country and do not know keyboard language
        if (linuxDistribution == "ubuntu") {
            val country = executeNativeCommand(arrayOf("gsettings", "get", "org.gnome.desktop.input-sources", "mru-sources"))
                .substringAfter("('xkb', '")
                .substringBefore("')")
                .substring(0, 2)
            return KeyboardLayout(country, unknownLanguage)
        }

        if (linuxNonUbuntuKeyboardCountries.isEmpty()) {
            linuxNonUbuntuKeyboardCountries = executeNativeCommand(arrayOf("setxkbmap", "-query"))
                .substringAfter("layout:")
                .substringBefore("\n")
                .trim()
                .split(",")
        }

        val linuxCurrentKeyboardCountryIndex = executeNativeCommand(arrayOf("xset", "-q"))
            .substringAfter("LED mask:")
            .substringBefore("\n")
            .trim()
            .substring(4, 5)
            .toInt(16)

        // This is a bad solution because it returns 0 if it's a default layout and 1 in other cases,
        // and if user has more than two layouts, we do not know which one is really on
        if (linuxNonUbuntuKeyboardCountries.size > 2 && linuxCurrentKeyboardCountryIndex > 0) {
            KeyboardLayout(unknownCountry, unknownLanguage)
        }

        val country = linuxNonUbuntuKeyboardCountries[linuxCurrentKeyboardCountryIndex]
        return KeyboardLayout(country, unknownLanguage)
    }

    private fun getMacKeyboardLayout(): KeyboardLayout {
        val locale = InputContext.getInstance().locale
        return KeyboardLayout(locale.country, locale.language)
    }

    private fun getWindowsKeyboardLayout(): KeyboardLayout {
        val locale = InputContext.getInstance().locale
        return KeyboardLayout(locale.country, locale.language)
    }

    private fun executeNativeCommand(command: Array<String>): String {
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

