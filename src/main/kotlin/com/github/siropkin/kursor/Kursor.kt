package com.github.siropkin.kursor

import com.intellij.openapi.editor.CaretVisualAttributes
import com.intellij.openapi.editor.EditorFactory
import java.awt.Color
import java.awt.im.InputContext

class Kursor {
    private var defaultCaretVisualAttributes: CaretVisualAttributes = CaretVisualAttributes(null, CaretVisualAttributes.Weight.NORMAL)
    private var customCaretVisualAttributes: CaretVisualAttributes = CaretVisualAttributes(Color.RED, CaretVisualAttributes.Weight.HEAVY, CaretVisualAttributes.Shape.BLOCK, 1.0f)

    // get current language
    private fun getLanguage(): String {
        val context: InputContext = InputContext.getInstance()
        return context.locale.toString().substring(0, 2)
    }

    // get caret visual attributes
    private fun getCaretVisualAttributes(): CaretVisualAttributes {
        val caret = EditorFactory.getInstance().allEditors.first().caretModel.primaryCaret
        return caret.visualAttributes
    }

    // set caret visual attributes
    private fun setCaretVisualAttributes(caretVisualAttributes: CaretVisualAttributes) {
        EditorFactory.getInstance().allEditors.forEach {
            val caret = it.caretModel.primaryCaret
            caret.visualAttributes = caretVisualAttributes
        }
    }

    // save default caret visual attributes
    fun saveDefaultCaretVisualAttributes() {
        defaultCaretVisualAttributes = getCaretVisualAttributes()
    }

    // update caret visual attributes based on current language
    fun updateCaretVisualAttributes() {
        val language = getLanguage()
        if (language == "en") {
            setCaretVisualAttributes(defaultCaretVisualAttributes)
        } else {
            setCaretVisualAttributes(customCaretVisualAttributes)
        }
    }
}
