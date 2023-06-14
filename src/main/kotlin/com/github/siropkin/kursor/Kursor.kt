package com.github.siropkin.kursor

import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import java.awt.*
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.KeyEvent
import java.awt.im.InputContext
import java.util.*
import javax.swing.JComponent

object Position {
    const val TOP = "top"
    const val BOTTOM = "bottom"
    const val MIDDLE = "middle"
}

class Kursor(private var editor: Editor): JComponent(), ComponentListener, CaretListener {
    private val defaultLanguage = "en"

    private val colorizeCaretOnNonDefaultLanguage = true
    private val caretColorOnDefaultLanguage = editor.colorsScheme.getColor(EditorColors.CARET_COLOR)
    private val caretColorOnNonDefaultLanguage = Color(255, 0, 0, 255)

    private val showIndicator = true
    private val indicateCapsLock = true
    private val indicateDefaultLanguage = true

    private val indicatorFontFamily = editor.colorsScheme.fontPreferences.fontFamily
    private val indicatorFontStyle = Font.PLAIN
    private val indicatorFontSize = 11
    private val indicatorFontAlpha = 180 // 0..255

    private val indicatorVerticalPosition = Position.TOP
    private val indicatorHorizontalOffset = 4

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

    private fun getLanguage(): String {
        val context: InputContext = InputContext.getInstance()
        return context.locale.toString().substring(0, 2)
    }

    private fun isCapsLockOn(): Boolean {
        val toolkit = Toolkit.getDefaultToolkit()
        return toolkit.getLockingKeyState(KeyEvent.VK_CAPS_LOCK)
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
        val area = editor.scrollingModel.visibleArea
        return Rectangle(area.x, area.y, area.width, area.height)
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

    private fun setCaretColor(caret: Caret, color: Color?) {
        caret.visualAttributes = CaretVisualAttributes(color, CaretVisualAttributes.DEFAULT.weight)
    }

    private fun colorWithAlpha(color: Color, alpha: Int): Color {
        return Color(color.red, color.green, color.blue, alpha)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if (!isEditorFocused()) {
            return
        }

        val language = getLanguage()
        val isCapsLockOn = isCapsLockOn()

        val caret = getPrimaryCaret()
        val caretColor = if (colorizeCaretOnNonDefaultLanguage && language != defaultLanguage) {
            caretColorOnNonDefaultLanguage
        } else {
            caretColorOnDefaultLanguage
        }
        setCaretColor(caret, caretColor)

        val isIndicatorVisible = showIndicator && (indicateDefaultLanguage || language != defaultLanguage || indicateCapsLock && isCapsLockOn)
        if (!isIndicatorVisible) {
            return
        }

        val indicatorText = if (indicateCapsLock && isCapsLockOn) language.uppercase(Locale.getDefault()) else language

        val caretWidth = getCaretWidth(caret)
        val caretHeight = getCaretHeight(caret)
        val caretPosition = getCaretPosition(caret)

        val indicatorOffsetX = caretWidth + indicatorHorizontalOffset
        var indicatorOffsetY = 0
        when (indicatorVerticalPosition) {
            Position.TOP -> indicatorOffsetY = (if (caret.visualPosition.line == 0) indicatorFontSize else indicatorFontSize / 2) - 1
            Position.BOTTOM -> indicatorOffsetY = caretHeight + 3
            Position.MIDDLE -> indicatorOffsetY = caretHeight / 2 + indicatorFontSize / 2 - 1
        }

        g.color = caretColor?.let { colorWithAlpha(it, indicatorFontAlpha) }
        g.font = Font(indicatorFontFamily, indicatorFontStyle, indicatorFontSize)
        g.drawString(indicatorText, caretPosition.x + indicatorOffsetX, caretPosition.y + indicatorOffsetY)
    }
}
