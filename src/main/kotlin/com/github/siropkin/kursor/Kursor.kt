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

    private fun getColorWithAlpha(color: Color, alpha: Int): Color {
        return Color(color.red, color.green, color.blue, alpha)
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
        val caretColor = if (settings.changeColorOnNonDefaultLanguage && keyboardLayout.toString().lowercase() != settings.defaultLanguage.lowercase()) {
            settings.colorOnNonDefaultLanguage
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
        val isDefaultLanguage = keyboardLayout.toString().lowercase() == settings.defaultLanguage.lowercase()
        if (!isCapsLockOn && isDefaultLanguage && !settings.indicateDefaultLanguage) {
            return
        }

        val textIndicatorString = if (isCapsLockOn) {
            keyboardLayout.toString().uppercase()
        } else {
            keyboardLayout.toString().lowercase()
        }
        val caretWidth = getCaretWidth(caret)
        val caretHeight = getCaretHeight(caret)
        val caretPosition = getCaretPosition(caret)

        val indicatorOffsetX = caretWidth + settings.textIndicatorHorizontalOffset
        val indicatorOffsetY = when (settings.textIndicatorVerticalPosition) {
            TextIndicatorVerticalPositions.TOP -> (if (caret.visualPosition.line == 0) settings.textIndicatorFontSize else settings.textIndicatorFontSize / 2) - 1
            TextIndicatorVerticalPositions.MIDDLE -> caretHeight / 2 + settings.textIndicatorFontSize / 2 - 1
            TextIndicatorVerticalPositions.BOTTOM -> caretHeight + 3
            else -> 0
        }

        g.font = Font(settings.textIndicatorFontName, settings.textIndicatorFontStyle, settings.textIndicatorFontSize)
        g.color = getColorWithAlpha(caretColor ?: getDefaultCaretColor()!!, settings.textIndicatorFontAlpha)
        g.drawString(textIndicatorString, caretPosition.x + indicatorOffsetX, caretPosition.y + indicatorOffsetY)
    }
}
