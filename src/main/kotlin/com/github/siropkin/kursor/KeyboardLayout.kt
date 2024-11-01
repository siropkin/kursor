package com.github.siropkin.kursor

import com.sun.jna.Platform
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HKL
import java.awt.im.InputContext
import java.io.BufferedReader
import java.io.IOException


private const val unknown = "unk"

// https://learn.microsoft.com/en-us/windows-hardware/manufacture/desktop/windows-language-pack-default-values
private val windowsKeyboardVariantMap = mapOf(
    "00000402" to "BG",
    "00000404" to "CH",
    "00000405" to "CZ",
    "00000406" to "DK",
    "00000407" to "DE",
    "00000408" to "GK",
    "00000409" to "US",
    "0000040A" to "SP",
    "0000040B" to "SU",
    "0000040C" to "FR",
    "0000040E" to "HU",
    "0000040F" to "IS",
    "00000410" to "IT",
    "00000411" to "JP",
    "00000412" to "KO",
    "00000413" to "NL",
    "00000414" to "NO",
    "00000415" to "PL",
    "00000416" to "BR",
    "00000418" to "RO",
    "00000419" to "RU",
    "0000041A" to "YU",
    "0000041B" to "SL",
    "0000041C" to "US",
    "0000041D" to "SV",
    "0000041F" to "TR",
    "00000422" to "US",
    "00000423" to "US",
    "00000424" to "YU",
    "00000425" to "ET",
    "00000426" to "US",
    "00000427" to "US",
    "00000804" to "CH",
    "00000809" to "UK",
    "0000080A" to "LA",
    "0000080C" to "BE",
    "00000813" to "BE",
    "00000816" to "PO",
    "00000C0C" to "CF",
    "00000C1A" to "US",
    "00001009" to "CAFR",
    "0000100C" to "SF",
    "00001809" to "US",
    "00010402" to "US",
    "00010405" to "CZ",
    "00010407" to "DEI",
    "00010408" to "GK",
    "00010409" to "DV",
    "0001040A" to "SP",
    "0001040E" to "HU",
    "00010410" to "IT",
    "00010415" to "PL",
    "00010419" to "RUT",
    "0001041B" to "SL",
    "0001041F" to "TRF",
    "00010426" to "US",
    "00010C0C" to "CF",
    "00010C1A" to "US",
    "00020408" to "GK",
    "00020409" to "US",
    "00030409" to "USL",
    "00040409" to "USR",
    "00050408" to "GK"
)

private val macKeyboardVariantMap = mapOf(
    "UserDefined_com.sogou.inputmethod.pinyin" to "ZH", // https://pinyin.sogou.com/mac
    "UserDefined_im.rime.inputmethod.Squirrel.Hans" to "SS", // Squirrel - Simplified： https://rime.im
    "UserDefined_im.rime.inputmethod.Squirrel.Hant" to "ST" // Squirrel - Traditional： https://rime.im
)

class KeyboardLayoutInfo(private val language: String, private val country: String, private val variant: String) {
    override fun toString(): String = variant.lowercase().ifEmpty {
        country.lowercase().ifEmpty {
            language.lowercase()
        }
    }
}

class KeyboardLayout {
    private var linuxDistribution: String = System.getenv("DESKTOP_SESSION")?.lowercase() ?: ""
    private var linuxDesktopGroup: String = System.getenv("XDG_SESSION_TYPE")?.lowercase() ?: ""
    private var linuxNonUbuntuKeyboardLayouts: List<String> = emptyList()

    fun getInfo(): KeyboardLayoutInfo {
        return when {
            Platform.isLinux() -> getLinuxKeyboardLayout()
            Platform.isMac() -> getMacKeyboardLayout()
            Platform.isWindows() -> getWindowsKeyboardLayout()
            else -> KeyboardLayoutInfo(unknown, unknown, unknown)
        }
    }

    private fun getLinuxKeyboardLayout(): KeyboardLayoutInfo {
        // InputContext.getInstance().locale is not working on Linux: it always returns "en_US"
        // This is not the ideal solution because it involves executing a shell command to know the current keyboard layout
        // which might affect the performance. And we have different commands for different Linux distributions.
        // But it is the only solution I found that works on Linux.
        // For Linux we know only keyboard layout and do not know keyboard language
        if (linuxDistribution == "ubuntu") {
            // output example: [('xkb', 'us'), ('xkb', 'ru'), ('xkb', 'ca+eng')]
            val split = executeNativeCommand(arrayOf("gsettings", "get", "org.gnome.desktop.input-sources", "mru-sources"))
                .substringAfter("('xkb', '")
                .substringBefore("')")
                .split("+")
            val language = if (split.size > 1) split[1] else ""
            val country = split[0]
            return KeyboardLayoutInfo(language, country, "")
        }

        // FIXME: This command does not work on linuxDesktopGroup = "wayland",
        //  see: https://github.com/siropkin/kursor/issues/3
        if (linuxDesktopGroup == "wayland") {
            return KeyboardLayoutInfo(unknown, unknown, unknown)
        }

        if (linuxNonUbuntuKeyboardLayouts.isEmpty()) {
            // output example: rules:      evdev
            //model:      pc105
            //layout:     us
            //options:    grp:win_space_toggle,terminate:ctrl_alt_bksp
            linuxNonUbuntuKeyboardLayouts = executeNativeCommand(arrayOf("setxkbmap", "-query"))
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
        val linuxCurrentKeyboardLayoutIndex = executeNativeCommand(arrayOf("xset", "-q"))
            .substringAfter("LED mask:")
            .substringBefore("\n")
            .trim()
            .substring(4, 5)
            .toInt(16)

        // Additional check to avoid out-of-bounds exception
        if (linuxCurrentKeyboardLayoutIndex >= linuxNonUbuntuKeyboardLayouts.size) {
            return KeyboardLayoutInfo(unknown, unknown, unknown)
        }

        // This is a bad solution because it returns 0 if it's a default layout and 1 in other cases,
        // and if user has more than two layouts, we do not know which one is really on
        if (linuxNonUbuntuKeyboardLayouts.size > 2 && linuxCurrentKeyboardLayoutIndex > 0) {
            return KeyboardLayoutInfo(unknown, unknown, unknown)
        }

        val country = linuxNonUbuntuKeyboardLayouts[linuxCurrentKeyboardLayoutIndex]
        return KeyboardLayoutInfo("", country, "")
    }

    private fun getMacKeyboardLayout(): KeyboardLayoutInfo {
        val locale = InputContext.getInstance().locale
        val variant = macKeyboardVariantMap[locale.variant] ?: "" // variant example for US: UserDefined_252
        return KeyboardLayoutInfo(locale.language, locale.country, variant)
    }

    private fun getWindowsKeyboardLayout(): KeyboardLayoutInfo {
        val locale = InputContext.getInstance().locale
        // Standard locale object does not return correct info in case user set different keyboard inputs for one language
        // see: https://github.com/siropkin/kursor/issues/4
        val user32 = User32.INSTANCE
        val fgWindow: WinDef.HWND? = user32.GetForegroundWindow() // Get the handle of the foreground window

        if (fgWindow == null) {
            return KeyboardLayoutInfo(locale.language, locale.country, "")
        }

        val threadId = user32.GetWindowThreadProcessId(fgWindow, null) // Get the thread ID of the foreground window
        val hkl: HKL = user32.GetKeyboardLayout(threadId) // Get the keyboard layout for the thread
        // FIXME: It should be a better way how to convert pointer to string
        //  hkl.pointer returns native@0x4090409: last 4 digits are language id, the rest is layout id
        val inputMethod = hkl.pointer.toString().split("@")[1]
        var layoutId = inputMethod.substring(0, inputMethod.length - 4)
        layoutId = when (layoutId) {
            "0xfffffffff008" -> {
                "00010419"
            }
            "0xfffffffff014" -> {
                "0001041F"
            }
            "0xfffffffff012" -> {
                "00010407"
            }
            else -> {
                layoutId.substring(2).padStart(8, '0')
            }
        }
        layoutId = layoutId.uppercase()
        val variant = windowsKeyboardVariantMap[layoutId] ?: ""
        return KeyboardLayoutInfo(locale.language, locale.country, variant)
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

