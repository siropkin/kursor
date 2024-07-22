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
        // InputContext.getInstance().locale is not working on Linux: it always returns "en_US"
        // This is not the ideal solution because it involves executing a shell command to know the current keyboard layout
        // which might affect the performance. And we have different commands for different Linux distributions.
        // But it is the only solution I found that works on Linux.
        // For Linux we know only keyboard country and do not know keyboard language
        if (linuxDistribution == "ubuntu") {
            // output example: [('xkb', 'us'), ('xkb', 'ru'), ('xkb', 'ca+eng')]
            val split = executeNativeCommand(arrayOf("gsettings", "get", "org.gnome.desktop.input-sources", "mru-sources"))
                .substringAfter("('xkb', '")
                .substringBefore("')")
                .split("+")
            val country = split[0]
            val language = if (split.size > 1) split[1] else unknownLanguage
            return KeyboardLayout(country, language)
        }

        if (linuxNonUbuntuKeyboardCountries.isEmpty()) {
            // output example: rules:      evdev
            //model:      pc105
            //layout:     us
            //options:    grp:win_space_toggle,terminate:ctrl_alt_bksp
            linuxNonUbuntuKeyboardCountries = executeNativeCommand(arrayOf("setxkbmap", "-query"))
                .substringAfter("layout:")
                .substringBefore("\n")
                .trim()
                .split(",")
        }

        // output example: Keyboard Control:
        //  auto repeat:  on    key click percent:  0    LED mask:  00000000
        //  XKB indicators:
        //    00: Caps Lock:   off    01: Num Lock:    off    02: Scroll Lock: off
        //    03: Compose:     off    04: Kana:        off    05: Sleep:       off
        //    06: Suspend:     off    07: Mute:        off    08: Misc:        off
        //    09: Mail:        off    10: Charging:    off    11: Shift Lock:  off
        //    12: Group 2:     off    13: Mouse Keys:  off
        //  auto repeat delay:  500    repeat rate:  33
        //  auto repeating keys:  00ffffffdffffbbf
        //                        fadfffefffedffff
        //                        9fffffffffffffff
        //                        fff7ffffffffffff
        //  bell percent:  50    bell pitch:  400    bell duration:  100
        //Pointer Control:
        //  acceleration:  2/1    threshold:  4
        //Screen Saver:
        //  prefer blanking:  yes    allow exposures:  yes
        //  timeout:  0    cycle:  0
        //Colors:
        //  default colormap:  0x20    BlackPixel:  0x0    WhitePixel:  0xffffff
        //Font Path:
        //  /usr/share/fonts/X11/misc,/usr/share/fonts/X11/Type1,built-ins
        //DPMS (Energy Star):
        //  Standby: 0    Suspend: 0    Off: 0
        //  DPMS is Enabled
        //  Monitor is On
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

