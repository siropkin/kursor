package com.github.siropkin.kursor.keyboard

import com.sun.jna.Platform
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HKL
import java.awt.im.InputContext


private const val UNKNOWN = "UNK"

class Keyboard {
    private var linuxConfig: LinuxConfig? = null

    fun getLayout(): KeyboardLayout {
        if (Platform.isLinux() && linuxConfig == null) {
            linuxConfig = LinuxConfig()
        }
        return when {
            Platform.isLinux() -> getLinuxLayout()
            Platform.isMac() -> getMacLayout()
            Platform.isWindows() -> getWindowsLayout()
            else -> getUnknownLayout()
        }
    }

    private fun getUnknownLayout(): KeyboardLayout {
        return KeyboardLayout(UNKNOWN, UNKNOWN, UNKNOWN)
    }

    private fun getLinuxLayout(): KeyboardLayout {
        // InputContext.getInstance().locale is not working on Linux: it always returns "en_US"
        // This is not the ideal solution because it involves executing a shell command to know the current keyboard layout
        // which might affect the performance. And we have different commands for different Linux distributions.
        // But it is the only solution I found that works on Linux.
        // For Linux we know only keyboard layout and do not know keyboard language
        val config = linuxConfig ?: return getUnknownLayout()
        return when {
            config.distribution == "ubuntu" -> getUbuntuLayout()
            config.desktopGroup == "wayland" -> getWaylandLayout()
            else -> getOtherLinuxLayout()
        }
    }

    private fun getUbuntuLayout(): KeyboardLayout {
        // Output example: [('xkb', 'us'), ('xkb', 'ru'), ('xkb', 'ca+eng')]
        val split = Utils.executeNativeCommand(arrayOf("gsettings", "get", "org.gnome.desktop.input-sources", "mru-sources"))
            .substringAfter("('xkb', '")
            .substringBefore("')")
            .split("+")
        val language = if (split.size > 1) split[1] else ""
        val country = split[0]
        return KeyboardLayout(language, country, "")
    }

    private fun getWaylandLayout(): KeyboardLayout {
        // FIXME: Other Linux distribution commands not working "Wayland",
        //  see: https://github.com/siropkin/kursor/issues/3
        return getUnknownLayout()
    }

    private fun getOtherLinuxLayout(): KeyboardLayout {
        val config = linuxConfig ?: return getUnknownLayout()
        if (config.availableKeyboardLayouts.isEmpty()) {
            // Output example: rules:      evdev
            //model:      pc105
            //layout:     us
            //options:    grp:win_space_toggle,terminate:ctrl_alt_bksp
            config.availableKeyboardLayouts = Utils.executeNativeCommand(arrayOf("setxkbmap", "-query"))
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
        val linuxCurrentKeyboardLayoutIndex = Utils.executeNativeCommand(arrayOf("xset", "-q"))
            .substringAfter("LED mask:")
            .substringBefore("\n")
            .trim()
            .substring(4, 5)
            .toInt(16)

        // Additional check to avoid out-of-bounds exception
        if (linuxCurrentKeyboardLayoutIndex >= config.availableKeyboardLayouts.size) {
            return getUnknownLayout()
        }

        // This is a bad solution because it returns 0 if it's a default layout and 1 in other cases,
        // and if user has more than two layouts, we do not know which one is really on
        if (config.availableKeyboardLayouts.size > 2 && linuxCurrentKeyboardLayoutIndex > 0) {
            return getUnknownLayout()
        }

        val country = config.availableKeyboardLayouts[linuxCurrentKeyboardLayoutIndex]
        return KeyboardLayout("", country, "")
    }

    private fun getMacLayout(): KeyboardLayout {
        val locale = InputContext.getInstance().locale
        val localeVariant = locale.variant.removePrefix("UserDefined_")
        val variant = MacStandardKeyboardVariants[localeVariant]
            ?: MacSogouPinyinVariants[localeVariant]
            ?: MacRimeSquirrelVariants[localeVariant]
            ?: ""
        return KeyboardLayout(locale.language, locale.country, variant)
    }

    private fun getWindowsLayout(): KeyboardLayout {
        val locale = InputContext.getInstance().locale
        // Standard locale object does not return correct info in case user set different keyboard inputs for one language
        // see: https://github.com/siropkin/kursor/issues/4
        val user32 = User32.INSTANCE
        val fgWindow: WinDef.HWND? = user32.GetForegroundWindow()
        if (fgWindow == null) {
            // Fallback: foreground window is not available, return locale-based layout
            return KeyboardLayout(locale.language, locale.country, "")
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
        return KeyboardLayout(locale.language, locale.country, variant)
    }
}

