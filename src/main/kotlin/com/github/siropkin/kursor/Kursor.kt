package com.github.siropkin.kursor

import com.github.siropkin.kursor.keyboard.Keyboard
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.CaretVisualAttributes
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import java.awt.*
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.KeyEvent
import javax.swing.JComponent


class Kursor(private var editor: Editor): JComponent(), ComponentListener, CaretListener {
    private val keyboard = Keyboard()

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

    private fun getIsCapsLockOn(): Boolean {
        return Toolkit
            .getDefaultToolkit()
            .getLockingKeyState(KeyEvent.VK_CAPS_LOCK)
    }

    private fun getIsOverwriteModeOn(): Boolean {
        return !editor.isInsertMode
    }

    private fun getIsBlockCursorModeOn(): Boolean {
        return editor.settings.isBlockCursor
    }

    private fun getIsEditorFocused(): Boolean {
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
        val isBlockCursorModeOn = getIsBlockCursorModeOn()
        val isOverwriteModeOn = getIsOverwriteModeOn()
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

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if (!getIsEditorFocused()) {
            return
        }

        val keyboardLayout = keyboard.getLayout()
        if (keyboardLayout.isEmpty()) {
            return
        }

        val settings = getSettings()
        val caret = getPrimaryCaret()
        val isNonDefaultLanguage = keyboardLayout.toString().lowercase() != settings.defaultLanguage.lowercase()
        val caretColor = if (settings.cursorColor != null && isNonDefaultLanguage) {
            settings.cursorColor
        } else {
            null
        }

        if (caret.visualAttributes.color != caretColor) {
            setCaretColor(caret, caretColor)
        }

        if (!settings.showTextIndicator) {
            return
        }

        val isCapsLockOn = settings.indicateCapsLock && getIsCapsLockOn()
        val isDefaultLanguage = !isNonDefaultLanguage
        if (!isCapsLockOn && isDefaultLanguage && !settings.indicateDefaultLanguage) {
            return
        }

        val textIndicatorString = if (isCapsLockOn) {
            keyboardLayout.toString().uppercase()
        } else {
            keyboardLayout.toString().lowercase()
        }
        val caretWidth = getCaretWidth(caret)
        val caretPosition = getCaretPosition(caret)

        val editorFontName = editor.colorsScheme.editorFontName
        val editorFontSize = editor.colorsScheme.editorFontSize
        val indicatorFontSize = (editorFontSize * 0.8).toInt().coerceAtLeast(8)

        val indicatorOffsetX = caretWidth + 4
        val indicatorOffsetY = (if (caret.visualPosition.line == 0) indicatorFontSize else indicatorFontSize / 2) - 1

        val font = Font(editorFontName, Font.PLAIN, indicatorFontSize)
        g.font = font

        val textX = caretPosition.x + indicatorOffsetX
        val textY = caretPosition.y + indicatorOffsetY

        if (settings.textIndicatorBackgroundColor != null) {
            val metrics = g.fontMetrics
            val textWidth = metrics.stringWidth(textIndicatorString)
            val textHeight = metrics.height
            val bgX = textX - 1
            val bgY = textY - metrics.ascent
            g.color = settings.textIndicatorBackgroundColor
            g.fillRect(bgX, bgY, textWidth + 2, textHeight)
        }

        g.color = settings.textIndicatorColor
        g.drawString(textIndicatorString, textX, textY)
    }
}
