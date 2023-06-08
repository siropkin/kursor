package com.github.siropkin.kursor.listeners

import com.intellij.ide.FrameStateListener
import com.intellij.openapi.editor.CaretVisualAttributes
import com.intellij.openapi.editor.EditorFactory
import java.awt.Color
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.im.InputContext

internal class MyFrameStateListener : FrameStateListener {
    // variable to store default caret visual attributes
    private var defaultCaretVisualAttributes: CaretVisualAttributes? = null

    // get current language
    private fun getLanguage(): String {
        val context: InputContext = InputContext.getInstance()
        return context.locale.toString().substring(0, 2)
    }

    // save default caret visual attributes
    private fun saveDefaultCaretVisualAttributes() {
        EditorFactory.getInstance().allEditors.forEach {
            val caret = it.caretModel.primaryCaret
            if (defaultCaretVisualAttributes == null) {
                defaultCaretVisualAttributes = caret.visualAttributes
            }
        }
    }

    // restore default caret visual attributes
    private fun restoreDefaultCaretVisualAttributes() {
        EditorFactory.getInstance().allEditors.forEach {
            val caret = it.caretModel.primaryCaret
            if (defaultCaretVisualAttributes != null) {
                caret.visualAttributes = defaultCaretVisualAttributes as CaretVisualAttributes
            }
        }
    }

    // set caret visual attributes to red
    private fun setCaretVisualAttributesToRed() {
        EditorFactory.getInstance().allEditors.forEach {
            val caret = it.caretModel.primaryCaret
            caret.visualAttributes = CaretVisualAttributes(Color.RED, CaretVisualAttributes.Weight.HEAVY, CaretVisualAttributes.Shape.BLOCK, 1.0f)
        }
    }

    // update caret visual attributes based on current language
    private fun updateCaretVisualAttributes(language: String) {
        // set caret visual attributes to red if language is not English
        if (language != "en") {
            // change caret visual attributes
            setCaretVisualAttributesToRed()
        } else {
            // restore default caret visual attributes
            restoreDefaultCaretVisualAttributes()
        }
    }

    override fun onFrameActivated() {
        // save default caret visual attributes
        saveDefaultCaretVisualAttributes()

        // update caret visual attributes based on current language
        updateCaretVisualAttributes(getLanguage())

        // add key listener to editor
        EditorFactory.getInstance().allEditors.forEach {
            val editor = it
            editor.contentComponent.addKeyListener(object : KeyListener {
                override fun keyTyped(e: KeyEvent) {
                }

                override fun keyPressed(e: KeyEvent?) {
                }

                override fun keyReleased(e: KeyEvent?) {
                    Thread {
                        Thread.sleep(100)
                        updateCaretVisualAttributes(getLanguage())
                    }.start()
                }
            })
        }
    }

    override fun onFrameDeactivated() {
        // restore default caret visual attributes
        restoreDefaultCaretVisualAttributes()
    }
}
