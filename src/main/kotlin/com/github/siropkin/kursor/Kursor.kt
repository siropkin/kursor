package com.github.siropkin.kursor

import com.github.siropkin.kursor.settings.KursorSettings
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import java.awt.*
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.KeyEvent
import java.awt.im.InputContext
import java.io.*
import javax.swing.JComponent

const val unknownCountry = "unk"

object IndicatorPosition {
    const val TOP = "top"
    const val MIDDLE = "middle"
    const val BOTTOM = "bottom"
}

interface KeyboardLocale {
    val language: String
    val country: String
    fun getIndicatorText(useLayout: Boolean): String = if (useLayout) country.lowercase() else language.lowercase()
}

class Kursor(private var editor: Editor): JComponent(), ComponentListener, CaretListener {
    private val os = System.getProperty("os.name").lowercase()
    private var linuxDistribution = System.getenv("DESKTOP_SESSION")?.lowercase() ?: ""
    private var linuxNonUbuntuKeyboardCountries: List<String> = emptyList()

    init {
        editor.contentComponent.add(this)
        isVisible = true
        bounds = editor.contentComponent.bounds
        editor.caretModel.addCaretListener(this)
        editor.component.addComponentListener(this)
    }

    override fun componentShown(e: ComponentEvent?) {}

    override fun componentHidden(e: ComponentEvent?) {}

    override fun componentResized(e: ComponentEvent?) = repaintComponent()

    override fun componentMoved(e: ComponentEvent?) = repaintComponent()

    override fun caretPositionChanged(e: CaretEvent) = repaintComponent()

    private fun repaintComponent() {
        bounds = editor.scrollingModel.visibleArea
        repaint()
    }

    private fun getSettings(): KursorSettings {
        return KursorSettings.getInstance()
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

    private fun getLinuxKeyboardLocale(): KeyboardLocale {
        if (linuxDistribution == "ubuntu") {
            val country = executeNativeCommand(arrayOf("gsettings", "get", "org.gnome.desktop.input-sources", "mru-sources"))
                .substringAfter("('xkb', '")
                .substringBefore("')")
                .substring(0, 2)
            return object : KeyboardLocale {
                override val language: String = country
                override val country: String = country
            }
        }

        // For non Ubuntu OS we know only keyboard layout and do not know keyboard language
        if (linuxNonUbuntuKeyboardCountries.isEmpty()) {
            linuxNonUbuntuKeyboardCountries = executeNativeCommand(arrayOf("setxkbmap", "-query"))
                .substringAfter("layout:")
                .substringBefore("\n")
                .trim()
                .split(",")
        }

        // This is a bad solution because it returns 0 if it's a default layout and 1 in other cases,
        // and if user has more than two layouts, we do not know which one is really on
        val linuxCurrentKeyboardCountryIndex = executeNativeCommand(arrayOf("xset", "-q"))
            .substringAfter("LED mask:")
            .substringBefore("\n")
            .trim()
            .substring(4, 5)
            .toInt(16)
        val country = if (linuxNonUbuntuKeyboardCountries.size > 2 && linuxCurrentKeyboardCountryIndex > 0) unknownCountry else linuxNonUbuntuKeyboardCountries[linuxCurrentKeyboardCountryIndex]
        return object : KeyboardLocale {
            override val language: String = country
            override val country: String = country
        }
    }

    private fun getMacKeyboardLocale(): KeyboardLocale {
        // if locale in format _US_UserDefined_252 then we need to take the first two letters without _ symbol
        // otherwise we are expecting the locale in format en_US and taking the first two letters
        val locale = InputContext.getInstance().locale.toString()
        val language = if (locale.startsWith("_")) {
            locale.substring(1, 3)
        } else {
            locale.substring(0, 2)
        }
        val layout = if (locale.startsWith("_")) {
            ""
        } else {
            locale.substring(3, 5)
        }
        return object : KeyboardLocale {
            override val language: String = language
            override val country: String = layout
        }
    }

    private fun getWindowsKeyboardLocale(): KeyboardLocale {
        val locale = InputContext.getInstance().locale
        return object : KeyboardLocale {
            override val language: String = locale.language
            override val country: String = locale.country
        }
    }

    private fun getKeyboardLocale(): KeyboardLocale {
        // This is not the ideal solution because it involves executing a shell command to know the current keyboard layout
        // which might affect the performance. And we have different commands for different Linux distributions.
        // But it is the only solution I found that works on Linux.
        return if (os == "linux") {
            getLinuxKeyboardLocale()
        } else {
            if (os.startsWith("win")) {
                getWindowsKeyboardLocale()
            } else {
                getMacKeyboardLocale()
            }
        }
    }

    private fun isCapsLockOn(): Boolean {
        return Toolkit
            .getDefaultToolkit()
            .getLockingKeyState(KeyEvent.VK_CAPS_LOCK)
    }

    private fun isOverwriteModeOn(): Boolean {
        return !editor.isInsertMode
    }

    private fun isBlockCursorModeOn(): Boolean {
        return editor.settings.isBlockCursor
    }

    private fun isEditorFocused(): Boolean {
        return editor.contentComponent.isFocusOwner
    }

    private fun getPrimaryCaret(): Caret {
        return editor.caretModel.primaryCaret
    }

    private fun getCaretPosition(caret: Caret): Point {
        val p: Point = editor.visualPositionToXY(caret.visualPosition)
        p.translate(-location.x, -location.y)
        return p
    }

    private fun getCaretWidth(caret: Caret): Int {
        val isBlockCursorModeOn = isBlockCursorModeOn()
        val isOverwriteModeOn = isOverwriteModeOn()
        val isCursorWide = (isBlockCursorModeOn && !isOverwriteModeOn) || (!isBlockCursorModeOn && isOverwriteModeOn)
        if (isCursorWide) {
            val p1 = editor.visualPositionToXY(caret.visualPosition)
            val p2 = editor.visualPositionToXY(VisualPosition(caret.visualPosition.line, caret.visualPosition.column + 1))
            return p2.x - p1.x
        }
        return caret.visualAttributes.thickness.toInt()
    }

    private fun getCaretHeight(caret: Caret): Int {
        val p1 = editor.visualPositionToXY(caret.visualPosition)
        val p2 = editor.visualPositionToXY(VisualPosition(caret.visualPosition.line + 1, caret.visualPosition.column))
        return p2.y - p1.y
    }

    private fun getDefaultCaretColor(): Color? {
        return editor.colorsScheme.getColor(EditorColors.CARET_COLOR)
    }

    private fun setCaretColor(caret: Caret, color: Color?) {
        caret.visualAttributes = CaretVisualAttributes(color, caret.visualAttributes.weight)
    }

    private fun colorWithAlpha(color: Color, alpha: Int): Color {
        return Color(color.red, color.green, color.blue, alpha)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if (!isEditorFocused()) {
            return
        }

        val settings = getSettings()

        val keyboardLocale = getKeyboardLocale()
        val isCapsLock = settings.indicateCapsLock && isCapsLockOn()
        var indicatorText = keyboardLocale.getIndicatorText(settings.useKeyboardLayout)
        if (indicatorText.isEmpty()) {
            indicatorText = settings.defaultLanguage
        }

        val caret = getPrimaryCaret()
        var caretColor: Color? = null
        if (settings.changeColorOnNonDefaultLanguage) {
            if (indicatorText != settings.defaultLanguage) {
                caretColor = settings.colorOnNonDefaultLanguage
            }
        }

        if (caret.visualAttributes.color != caretColor) {
            setCaretColor(caret, caretColor)
        }

        if (!settings.showIndicator) {
            return
        }

        val showIndicator = settings.indicateDefaultLanguage || isCapsLock || indicatorText.lowercase() != settings.defaultLanguage.lowercase()
        if (!showIndicator) {
            return
        }

        if (isCapsLock) {
            indicatorText = indicatorText.uppercase()
        }

        val caretWidth = getCaretWidth(caret)
        val caretHeight = getCaretHeight(caret)
        val caretPosition = getCaretPosition(caret)

        val indicatorOffsetX = caretWidth + settings.indicatorHorizontalOffset
        val indicatorOffsetY = when (settings.indicatorVerticalPosition) {
            IndicatorPosition.TOP -> (if (caret.visualPosition.line == 0) settings.indicatorFontSize else settings.indicatorFontSize / 2) - 1
            IndicatorPosition.MIDDLE -> caretHeight / 2 + settings.indicatorFontSize / 2 - 1
            IndicatorPosition.BOTTOM -> caretHeight + 3
            else -> 0
        }

        g.font = Font(settings.indicatorFontName, settings.indicatorFontStyle, settings.indicatorFontSize)
        g.color = if (caretColor == null) {
            colorWithAlpha(getDefaultCaretColor()!!, settings.indicatorFontAlpha)
        } else {
            colorWithAlpha(caretColor, settings.indicatorFontAlpha)
        }
        g.drawString(indicatorText, caretPosition.x + indicatorOffsetX, caretPosition.y + indicatorOffsetY)
    }
}
