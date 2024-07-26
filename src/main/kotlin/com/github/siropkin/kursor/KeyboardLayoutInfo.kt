package com.github.siropkin.kursor

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HKL
import java.awt.im.InputContext
import java.io.BufferedReader
import java.io.IOException


private const val unknown = "unk"

// https://learn.microsoft.com/en-us/windows-hardware/manufacture/desktop/windows-language-pack-default-values
private val windowsKeyboardLayoutMap = mapOf(
    "00000402" to "BG",
    "00000404" to "CH",
    "00000405" to "CZ",
    "00000406" to "DK",
    "00000407" to "GR",
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
    "00010407" to "GR",
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

// https://www.autoitscript.com/autoit3/docs/appendix/OSLangCodes.htm
private val windowsKeyboardCountryCodeMap = mapOf(
    "0004" to "zh-CHS",
    "0401" to "ar-SA",
    "0402" to "bg-BG",
    "0403" to "ca-ES",
    "0404" to "zh-TW",
    "0405" to "cs-CZ",
    "0406" to "da-DK",
    "0407" to "de-DE",
    "0408" to "el-GR",
    "0409" to "en-US",
    "040A" to "es-ES",
    "040B" to "fi-FI",
    "040C" to "fr-FR",
    "040D" to "he-IL",
    "040E" to "hu-HU",
    "040F" to "is-IS",
    "0410" to "it-IT",
    "0411" to "ja-JP",
    "0412" to "ko-KR",
    "0413" to "nl-NL",
    "0414" to "nb-NO",
    "0415" to "pl-PL",
    "0416" to "pt-BR",
    "0417" to "rm-CH",
    "0418" to "ro-RO",
    "0419" to "ru-RU",
    "041A" to "hr-HR",
    "041B" to "sk-SK",
    "041C" to "sq-AL",
    "041D" to "sv-SE",
    "041E" to "th-TH",
    "041F" to "tr-TR",
    "0420" to "ur-PK",
    "0421" to "id-ID",
    "0422" to "uk-UA",
    "0423" to "be-BY",
    "0424" to "sl-SI",
    "0425" to "et-EE",
    "0426" to "lv-LV",
    "0427" to "lt-LT",
    "0428" to "tg-TJ",
    "0429" to "fa-IR",
    "042A" to "vi-VN",
    "042B" to "hy-AM",
    "042C" to "az-AZ",
    "042D" to "eu-ES",
    "042E" to "hsb-DE",
    "042F" to "mk-MK",
    "0432" to "tn-ZA",
    "0434" to "xh-ZA",
    "0435" to "zu-ZA",
    "0436" to "af-ZA",
    "0437" to "ka-GE",
    "0438" to "fo-FO",
    "0439" to "hi-IN",
    "043A" to "mt-MT",
    "043B" to "se-NO",
    "043E" to "ms-MY",
    "043F" to "kk-KZ",
    "0440" to "ky-KG",
    "0441" to "sw-KE",
    "0442" to "tk-TM",
    "0443" to "uz-UZ",
    "0444" to "tt-RU",
    "0445" to "bn-IN",
    "0446" to "pa-IN",
    "0447" to "gu-IN",
    "0448" to "or-IN",
    "0449" to "ta-IN",
    "044A" to "te-IN",
    "044B" to "kn-IN",
    "044C" to "ml-IN",
    "044D" to "as-IN",
    "044E" to "mr-IN",
    "044F" to "sa-IN",
    "0450" to "mn-MN",
    "0451" to "bo-CN",
    "0452" to "cy-GB",
    "0453" to "km-KH",
    "0454" to "lo-LA",
    "0456" to "gl-ES",
    "0457" to "kok-IN",
    "0459" to "sd-IN",
    "045A" to "syr-SY",
    "045B" to "si-LK",
    "045C" to "chr-US",
    "045D" to "iu-CA",
    "045E" to "am-ET",
    "0461" to "ne-NP",
    "0462" to "fy-NL",
    "0463" to "ps-AF",
    "0464" to "fil-PH",
    "0465" to "dv-MV",
    "0468" to "ha-NG",
    "046A" to "yo-NG",
    "046B" to "quz-BO",
    "046C" to "nso-ZA",
    "046D" to "ba-RU",
    "046E" to "lb-LU",
    "046F" to "kl-GL",
    "0470" to "ig-NG",
    "0473" to "ti-ET",
    "0475" to "haw-US",
    "0478" to "ii-CN",
    "047A" to "arn-CL",
    "047C" to "moh-CA",
    "047E" to "br-FR",
    "0480" to "ug-CN",
    "0481" to "mi-NZ",
    "0482" to "oc-FR",
    "0483" to "co-FR",
    "0484" to "gsw-FR",
    "0485" to "sah-RU",
    "0486" to "quc-GT",
    "0487" to "rw-RW",
    "0488" to "wo-SN",
    "048C" to "prs-AF",
    "0491" to "gd-GB",
    "0492" to "ku-IQ",
    "0801" to "ar-IQ"
)

class KeyboardLayout(private val layout: String, private val country: String, private val language: String) {
    override fun toString(): String = layout.lowercase().ifEmpty {
        country.lowercase().ifEmpty {
            language.lowercase()
        }
    }
}

class KeyboardLayoutInfo {
    private val os: String = System.getProperty("os.name").lowercase()
    private var linuxDistribution: String = System.getenv("DESKTOP_SESSION")?.lowercase() ?: ""
    private var linuxDesktopGroup: String = System.getenv("XDG_SESSION_TYPE")?.lowercase() ?: ""
    private var linuxNonUbuntuKeyboardLayouts: List<String> = emptyList()

    fun getLayout(): KeyboardLayout {
        return when {
            os.startsWith("linux") -> getLinuxKeyboardLayout()
            os.startsWith("win") -> getWindowsKeyboardLayout()
            os.startsWith("mac") -> getMacKeyboardLayout()
            else -> KeyboardLayout(unknown, unknown, unknown)
        }
    }

    private fun getLinuxKeyboardLayout(): KeyboardLayout {
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
            val country = split[0]
            val language = if (split.size > 1) split[1] else ""
            return KeyboardLayout("", country, language)
        }

        // FIXME: This command does not work on linuxDesktopGroup = "wayland",
        //  see: https://github.com/siropkin/kursor/issues/3
        if (linuxDesktopGroup == "wayland") {
            return KeyboardLayout(unknown, unknown, unknown)
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
            return KeyboardLayout(unknown, unknown, unknown)
        }

        // This is a bad solution because it returns 0 if it's a default layout and 1 in other cases,
        // and if user has more than two layouts, we do not know which one is really on
        if (linuxNonUbuntuKeyboardLayouts.size > 2 && linuxCurrentKeyboardLayoutIndex > 0) {
            return KeyboardLayout(unknown, unknown, unknown)
        }

        val country = linuxNonUbuntuKeyboardLayouts[linuxCurrentKeyboardLayoutIndex]
        return KeyboardLayout("", country, "")
    }

    private fun getMacKeyboardLayout(): KeyboardLayout {
        val locale = InputContext.getInstance().locale
        return KeyboardLayout("", locale.country, locale.language)
    }

    private fun getWindowsKeyboardLayout(): KeyboardLayout {
        // Standard "InputContext.getInstance().locale" does not work on Windows
        // in case user set different keyboard layout (inputs) for one language
        // see: https://github.com/siropkin/kursor/issues/4
        val user32 = User32.INSTANCE
        val fgWindow: WinDef.HWND = user32.GetForegroundWindow() // Get the handle of the foreground window
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
            else -> {
                layoutId.substring(2).padStart(8, '0')
            }
        }
        layoutId = layoutId.uppercase()

        val languageId = inputMethod.substring(inputMethod.length - 4, inputMethod.length).uppercase()

        val layout = windowsKeyboardLayoutMap[layoutId] ?: unknown
        val countryCode = windowsKeyboardCountryCodeMap[languageId] ?: unknown
        if (layout == unknown && countryCode == unknown) {
            return KeyboardLayout(unknown, unknown, unknown)
        }
        val split = countryCode.split("-")
        val language = split[0]
        val country = if (split.size > 1) split[1] else ""
        return KeyboardLayout(layout, country, language)
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

