package com.github.siropkin.kursor

import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import java.awt.Graphics
import java.awt.Point
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.im.InputContext
import javax.swing.JComponent

// TODO: Add blinking cursor
class Kursor(private var editor: Editor): JComponent(), ComponentListener, CaretListener {
    init {
        this.isVisible = true
        this.bounds = editor.contentComponent.bounds

        editor.caretModel.addCaretListener(this)

        editor.contentComponent.add(this)
        editor.contentComponent.repaint()
    }

    private fun getLanguage(): String {
        val context: InputContext = InputContext.getInstance()
        return context.locale.toString().substring(0, 2)
    }

    private fun getCaretPosition(caret: Caret): Point {
        return caret.editor.visualPositionToXY(caret.visualPosition)
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        val language = getLanguage()
        // val fontSize = editor.colorsScheme.editorFontSize * 1.2f
        // val defaultFontColor = editor.colorsScheme.defaultForeground
        // val fontColor = Color(defaultFontColor.red, defaultFontColor.green, defaultFontColor.blue, 80)
        editor.caretModel.allCarets.forEach { caret ->
            // g.font = g.font.deriveFont(fontSize)
            // g.color = fontColor
            val caretPosition = getCaretPosition(caret)
            val offsetX = 4
            val offsetY = (editor.colorsScheme.editorFontSize2D / 2).toInt() + 1
            g.drawString(language, caretPosition.x + offsetX, caretPosition.y + offsetY)
        }
    }

    override fun componentResized(e: ComponentEvent?) {
        this.bounds = editor.contentComponent.bounds
    }

    override fun componentMoved(e: ComponentEvent?) {
        this.bounds = editor.contentComponent.bounds
    }

    override fun componentShown(e: ComponentEvent?) {
        this.bounds = editor.contentComponent.bounds
        this.isVisible = true
    }

    override fun componentHidden(e: ComponentEvent?) {
        this.bounds = editor.contentComponent.bounds
        this.isVisible = false
    }

    override fun caretPositionChanged(e: CaretEvent) {
        repaint()
    }
}
