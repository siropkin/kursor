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
    private val defaultLanguage = null // "en"
    private val defaultIndicateCapsLock = true

    private val defaultFontFamily = editor.colorsScheme.fontPreferences.fontFamily
    private val defaultFontStyle = Font.PLAIN
    private val defaultFontSize = 11

    private val defaultColor = editor.colorsScheme.getColor(EditorColors.CARET_COLOR)
    private val defaultAlpha = 180 // 0..255

    private val defaultVerticalPosition = Position.TOP
    private val defaultHorizontalOffset = 4

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

    private fun isEditorFocused(): Boolean {
        return editor.contentComponent.isFocusOwner
    }

    private fun getEditorBounds(): Rectangle {
        val area = editor.scrollingModel.visibleArea
        return Rectangle(area.x, area.y, area.width, area.height)
    }

    private fun getCaretPosition(caret: Caret): Point {
        val p: Point = editor.visualPositionToXY(caret.visualPosition)
        p.translate(-location.x, -location.y)
        return p
    }

    private fun getCaretHeight(): Int {
        val caret = editor.caretModel.primaryCaret
        val p1 = editor.visualPositionToXY(caret.visualPosition)
        val p2 = editor.visualPositionToXY(VisualPosition(caret.visualPosition.line + 1, caret.visualPosition.column))
        return p2.y - p1.y
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if (!isEditorFocused()) {
            return
        }

        var language = getLanguage()
        val isCapsLockOn = isCapsLockOn()

        if (language == defaultLanguage && (!defaultIndicateCapsLock || !isCapsLockOn)) {
            return
        }

        if (defaultIndicateCapsLock && isCapsLockOn) {
            language = language.uppercase(Locale.getDefault())
        }

        val font = Font(defaultFontFamily, defaultFontStyle, defaultFontSize)
        val color = defaultColor?.let { Color(it.red, it.green, it.blue, defaultAlpha) }

        val offsetX = defaultHorizontalOffset
        var offsetY = 0
        when (defaultVerticalPosition) {
            Position.TOP -> offsetY = defaultFontSize / 2 - 1
            Position.BOTTOM -> offsetY = getCaretHeight() + 3
            Position.MIDDLE -> offsetY = getCaretHeight() / 2 + defaultFontSize / 2 - 1
        }

        editor.caretModel.allCarets.forEach { caret ->
            val caretPosition = getCaretPosition(caret)
            var firstLineOffsetY = 0
            if (defaultVerticalPosition == Position.TOP && caret.visualPosition.line == 0) {
                firstLineOffsetY = defaultFontSize / 2
            }
            g.color = color!!
            g.font = font
            g.drawString(language, caretPosition.x + offsetX, caretPosition.y + offsetY + firstLineOffsetY)
        }
    }
}
