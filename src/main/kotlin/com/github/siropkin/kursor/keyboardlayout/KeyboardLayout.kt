package com.github.siropkin.kursor.keyboardlayout

import com.sun.jna.Platform
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HKL
import java.awt.im.InputContext
import java.io.BufferedReader
import java.io.IOException


class KeyboardLayout {
    private val unknown = "UNK"
    private var linuxDistribution: String = System.getenv("DESKTOP_SESSION")?.lowercase() ?: ""
    private var linuxDesktopGroup: String = System.getenv("XDG_SESSION_TYPE")?.lowercase() ?: ""
    private var linuxKeyboardLayoutsCache: List<String> = emptyList()

    fun getLayoutInfo(): KeyboardLayoutInfo {
        return when {
            Platform.isLinux() -> getLinuxLayoutInfo()
            Platform.isMac() -> getMacLayoutInfo()
            Platform.isWindows() -> getWindowsLayoutInfo()
            else -> getUnknownLayoutInfo()
        }
    }

    private fun getUnknownLayoutInfo(): KeyboardLayoutInfo {
        return KeyboardLayoutInfo(unknown, unknown, unknown)
    }

    private fun getLinuxLayoutInfo(): KeyboardLayoutInfo {
        // InputContext.getInstance().locale is not working on Linux: it always returns "en_US"
        // This is not the ideal solution because it involves executing a shell command to know the current keyboard layout
        // which might affect the performance. And we have different commands for different Linux distributions.
        // But it is the only solution I found that works on Linux.
        // For Linux we know only keyboard layout and do not know keyboard language
        return when {
            linuxDistribution == "ubuntu" -> getUbuntuLayoutInfo()
            linuxDesktopGroup == "wayland" -> getWaylandLayoutInfo()
            else -> getOtherLinuxLayoutInfo()
        }
    }

    private fun getUbuntuLayoutInfo(): KeyboardLayoutInfo {
        // Output example: [('xkb', 'us'), ('xkb', 'ru'), ('xkb', 'ca+eng')]
        val split = executeNativeCommand(arrayOf("gsettings", "get", "org.gnome.desktop.input-sources", "mru-sources"))
            .substringAfter("('xkb', '")
            .substringBefore("')")
            .split("+")
        val language = if (split.size > 1) split[1] else ""
        val country = split[0]
        return KeyboardLayoutInfo(language, country, "")
    }

    private fun getWaylandLayoutInfo(): KeyboardLayoutInfo {
        // FIXME: Other Linux distribution commands not working "Wayland",
        //  see: https://github.com/siropkin/kursor/issues/3
        return getUnknownLayoutInfo()
    }

    private fun getOtherLinuxLayoutInfo(): KeyboardLayoutInfo {
        if (linuxKeyboardLayoutsCache.isEmpty()) {
            // Output example: rules:      evdev
            //model:      pc105
            //layout:     us
            //options:    grp:win_space_toggle,terminate:ctrl_alt_bksp
            linuxKeyboardLayoutsCache = executeNativeCommand(arrayOf("setxkbmap", "-query"))
                .substringAfter("layout:")
                .substringBefore("\n")
                .trim()
                .split(",")
        }

        // Output example: Keyboard Control:
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
        if (linuxCurrentKeyboardLayoutIndex >= linuxKeyboardLayoutsCache.size) {
            return getUnknownLayoutInfo()
        }

        // This is a bad solution because it returns 0 if it's a default layout and 1 in other cases,
        // and if user has more than two layouts, we do not know which one is really on
        if (linuxKeyboardLayoutsCache.size > 2 && linuxCurrentKeyboardLayoutIndex > 0) {
            return getUnknownLayoutInfo()
        }

        val country = linuxKeyboardLayoutsCache[linuxCurrentKeyboardLayoutIndex]
        return KeyboardLayoutInfo("", country, "")
    }

    private fun getMacLayoutInfo(): KeyboardLayoutInfo {
        val locale = InputContext.getInstance().locale
        // Variant example for US: UserDefined_252
        val variant = MacKeyboardVariants[locale.variant] ?: ""
        return KeyboardLayoutInfo(locale.language, locale.country, variant)
    }

    private fun getWindowsLayoutInfo(): KeyboardLayoutInfo {
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
            "0xfffffffff008" -> "00010419"
            "0xfffffffff014" -> "0001041F"
            "0xfffffffff012" -> "00010407"
            else -> layoutId.substring(2).padStart(8, '0')
        }
        val variant = WindowsKeyboardVariants[layoutId.uppercase()] ?: ""
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

