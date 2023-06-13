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
    // save init scroll position
    private val initLocation = editor.scrollingModel.visibleArea.location

    init {
        editor.contentComponent.add(this)

        this.isVisible = true
        this.bounds = editor.contentComponent.bounds

        editor.caretModel.addCaretListener(this)
    }

    override fun componentResized(e: ComponentEvent?) {
        this.bounds = editor.contentComponent.bounds
    }

    override fun componentMoved(e: ComponentEvent?) {
        this.bounds = editor.contentComponent.bounds
    }

    override fun componentShown(e: ComponentEvent?) {
    }

    override fun componentHidden(e: ComponentEvent?) {
    }

    override fun caretPositionChanged(e: CaretEvent) {
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

    private fun getPoint(position: VisualPosition, editor: Editor): Point {
        val p: Point = editor.visualPositionToXY(position)
        // translate to scroll position
        val scrollingModel = editor.scrollingModel
        p.translate(scrollingModel.horizontalScrollOffset, scrollingModel.verticalScrollOffset)
        // translate to current scroll location
        val location = scrollingModel.visibleArea.location
        p.translate(-location.x, -location.y)
        // translate to init scroll location
        p.translate(initLocation.x, initLocation.y)
        return p
    }

    private fun getCaretPosition(caret: Caret): Point {
        return getPoint(caret.visualPosition, caret.editor)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        this.bounds = editor.contentComponent.bounds
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
            g.color = caret.visualAttributes.color
            g.drawString(language, caretPosition.x + offsetX, caretPosition.y + offsetY)
        }
    }
}
