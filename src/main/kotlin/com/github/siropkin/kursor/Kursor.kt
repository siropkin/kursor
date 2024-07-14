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
import java.util.*
import javax.swing.JComponent


object Position {
    const val TOP = "top"
    const val MIDDLE = "middle"
    const val BOTTOM = "bottom"
}


class Kursor(private var editor: Editor): JComponent(), ComponentListener, CaretListener {
    private val os = System.getProperty("os.name").lowercase()
    private var linuxDistribution = System.getenv("DESKTOP_SESSION")?.lowercase() ?: ""
    private var linuxKeyboardLayouts: List<String> = listOf()

    init {
        editor.contentComponent.add(this)
        isVisible = true
        bounds = editor.contentComponent.bounds
        editor.caretModel.addCaretListener(this)
        editor.component.addComponentListener(this)
    }

    override fun componentResized(e: ComponentEvent?) {
        bounds = getEditorBounds()
        repaint()
    }

    override fun componentMoved(e: ComponentEvent?) {
        bounds = getEditorBounds()
        repaint()
    }

    override fun componentShown(e: ComponentEvent?) {
    }

    override fun componentHidden(e: ComponentEvent?) {
    }

    override fun caretPositionChanged(e: CaretEvent) {
        bounds = getEditorBounds()
        repaint()
    }

    private fun getSettings(): KursorSettings {
        return KursorSettings.getInstance()
    }

    private fun executeNativeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            process.inputStream.bufferedReader().use(BufferedReader::readText)
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    private fun getLinuxUbuntuKeyboardLayout(): String {
        val commandOutput = executeNativeCommand("gsettings get org.gnome.desktop.input-sources mru-sources")
        return commandOutput
            .substringAfter("('xkb', '")
            .substringBefore("')")
            .substring(0, 2)
    }

    private fun getLinuxNonUbuntuKeyboardLayouts(): List<String> {
        if (linuxKeyboardLayouts.isNotEmpty()) {
            return linuxKeyboardLayouts
        }
        linuxKeyboardLayouts = executeNativeCommand("setxkbmap -query")
            .substringAfter("layout:")
            .substringBefore("\n")
            .trim()
            .split(",")
        return linuxKeyboardLayouts
    }

    private fun getLinuxNonUbuntuKeyboardLayoutIndex(): Int {
        return executeNativeCommand("xset -q")
            .substringAfter("LED mask:")
            .substringBefore("\n")
            .trim()
            .substring(4, 5)
            .toInt(16)
    }

    private fun getLinuxNonUbuntuKeyboardLayout(): String {
        val linuxKeyboardLayouts = getLinuxNonUbuntuKeyboardLayouts()
        val linuxCurrentKeyboardLayoutIndex = getLinuxNonUbuntuKeyboardLayoutIndex()
        return linuxKeyboardLayouts[linuxCurrentKeyboardLayoutIndex]
    }

    private fun getOtherOsKeyboardLayout(): String {
        // if locale in format _US_UserDefined_252 then we need to take the first two letters without _ symbol
        // otherwise we are expecting the locale in format en_US and taking the first two letters
        val locale = InputContext.getInstance().locale.toString()
        if (locale.startsWith("_")) {
            return locale.substring(1, 3)
        }
        return locale.substring(0, 2)
    }

    private fun getKeyboardLayout(): String {
        // This is not the ideal solution because it involves executing a shell command to know the current keyboard layout
        // which might affect the performance. And we have different commands for different Linux distributions.
        // But it is the only solution I found that works on Linux.
        var language = when (os) {
            "linux" -> when (linuxDistribution) {
                "ubuntu" -> getLinuxUbuntuKeyboardLayout()
                else -> getLinuxNonUbuntuKeyboardLayout()
            }
            else -> getOtherOsKeyboardLayout()
        }.lowercase()
        if (language == "us") {
            language = "en"
        }
        if (language.isEmpty()) {
            language = getSettings().defaultLanguage
        }
        return language
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

    private fun getEditorBounds(): Rectangle {
        return editor.scrollingModel.visibleArea
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
        val keyboardLayout = getKeyboardLayout()
        val isCapsLockOn = isCapsLockOn()

        val caret = getPrimaryCaret()
        val caretColor = if (settings.changeColorOnNonDefaultLanguage && keyboardLayout != settings.defaultLanguage) {
            settings.colorOnNonDefaultLanguage
        } else {
            null
        }
        if (caret.visualAttributes.color != caretColor) {
            setCaretColor(caret, caretColor)
        }

        val isIndicatorVisible = settings.showIndicator && (settings.indicateDefaultLanguage || keyboardLayout != settings.defaultLanguage || settings.indicateCapsLock && isCapsLockOn)
        if (!isIndicatorVisible) {
            return
        }

        val indicatorText = if (settings.indicateCapsLock && isCapsLockOn) keyboardLayout.uppercase(Locale.getDefault()) else keyboardLayout

        val caretWidth = getCaretWidth(caret)
        val caretHeight = getCaretHeight(caret)
        val caretPosition = getCaretPosition(caret)

        val indicatorOffsetX = caretWidth + settings.indicatorHorizontalOffset
        val indicatorOffsetY = when (settings.indicatorVerticalPosition) {
            Position.TOP -> (if (caret.visualPosition.line == 0) settings.indicatorFontSize else settings.indicatorFontSize / 2) - 1
            Position.MIDDLE -> caretHeight / 2 + settings.indicatorFontSize / 2 - 1
            Position.BOTTOM -> caretHeight + 3
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
