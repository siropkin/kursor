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

// TODO: show and hide g when caret is blinking
//val blinkPeriod = editor.settings.caretBlinkPeriod
//if (blinkPeriod > 0) { }

object Position {
    const val TOP = "top"
    const val BOTTOM = "bottom"
    const val MIDDLE = "middle"
}

class Kursor(private var editor: Editor): JComponent(), ComponentListener, CaretListener {
    private val defaultLanguage = "en"
    private val defaultAlpha = 180
    private val defaultPosition = Position.TOP
    // TODO: Add option to change color

    init {
        editor.contentComponent.add(this)
        isVisible = true
        bounds = editor.contentComponent.bounds
        editor.caretModel.addCaretListener(this)
        editor.component.addComponentListener(this)
    }

    override fun componentResized(e: ComponentEvent?) {
        bounds = getMyBounds()
        repaint()
    }

    override fun componentMoved(e: ComponentEvent?) {
        bounds = getMyBounds()
        repaint()
    }

    override fun componentShown(e: ComponentEvent?) {
    }

    override fun componentHidden(e: ComponentEvent?) {
    }

    override fun caretPositionChanged(e: CaretEvent) {
        bounds = getMyBounds()
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

    private fun getCaretPosition(caret: Caret): Point {
        val p: Point = editor.visualPositionToXY(caret.visualPosition)
        p.translate(-location.x, -location.y)
        return p
    }

    // get caret height
    private fun getCaretHeight(): Int {
        val caret = editor.caretModel.primaryCaret
        val p1 = editor.visualPositionToXY(caret.visualPosition)
        val p2 = editor.visualPositionToXY(VisualPosition(caret.visualPosition.line + 1, caret.visualPosition.column))
        return p2.y - p1.y
    }

    private fun getMyBounds(): Rectangle {
        val area = editor.scrollingModel.visibleArea
        return Rectangle(area.x, area.y, area.width, area.height)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (!editor.contentComponent.isFocusOwner) {
            return
        }
        var language = getLanguage()
        val isCapsLockOn = isCapsLockOn()
        if (language == defaultLanguage && !isCapsLockOn) {
            return
        }
        if (isCapsLockOn) {
            language = language.uppercase(Locale.getDefault())
        }
        val offsetX = 4
        var offsetY = 0
        when (defaultPosition) {
            Position.TOP -> offsetY = 0
            // TODO: fix offsetY for different fonts
            Position.BOTTOM -> offsetY = getCaretHeight() + if (isCapsLockOn) { 6 } else { 3 }
            Position.MIDDLE -> offsetY = editor.colorsScheme.editorFontSize
        }
        editor.caretModel.allCarets.forEach { caret ->
            val caretPosition = getCaretPosition(caret)
            val color = caret.visualAttributes.color ?: editor.colorsScheme.getColor(EditorColors.CARET_COLOR)
            if (color != null) {
                g.color = Color(color.red, color.green, color.blue, defaultAlpha)
            }
            g.drawString(language, caretPosition.x + offsetX, caretPosition.y + offsetY)
        }
    }
}
