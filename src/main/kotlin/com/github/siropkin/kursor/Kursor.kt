package com.github.siropkin.kursor

import com.intellij.openapi.editor.*
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

class Kursor(private var editor: Editor): JComponent(), ComponentListener, CaretListener {
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
        if (isCapsLockOn) {
            language = language.uppercase(Locale.getDefault())
        }
        val offsetX = 4
        var offsetY = (editor.colorsScheme.editorFontSize2D / 2).toInt()
        // TODO: fix offsetY for different fonts
        // val bounds = g.fontMetrics.getStringBounds(language, g)
        offsetY += if (isCapsLockOn) { 5 } else { 2 }
        editor.caretModel.allCarets.forEach { caret ->
            val caretPosition = getCaretPosition(caret)
            g.drawString(language, caretPosition.x + offsetX, caretPosition.y + offsetY)
        }
    }
}
