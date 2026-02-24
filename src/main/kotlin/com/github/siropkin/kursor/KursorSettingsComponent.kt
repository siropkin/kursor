package com.github.siropkin.kursor

import com.github.siropkin.kursor.keyboard.Keyboard
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel


private const val LABEL_SPACING = 10
private const val COMPONENT_SPACING = 35

class KursorSettingsComponent {
    private val keyboardLayout = Keyboard()

    private val defaultLanguageComponent = JBTextField("", 5)
    private val detectKeyboardLayoutButton = JButton("Detect Keyboard Layout")

    private val cursorColorComponent = ColorPanel()

    private val showTextIndicatorComponent = JBCheckBox("Show text indicator")
    private val indicateDefaultLanguageComponent = JBCheckBox("Show default language")
    private val indicateCapsLockComponent = JBCheckBox("Indicate 'Caps Lock'")
    private val textIndicatorColorComponent = ColorPanel()
    private val textIndicatorBackgroundColorComponent = ColorPanel()

    var panel: JPanel = FormBuilder.createFormBuilder()
        .addComponent(createLanguagePanel())
        .addComponent(createCursorPanel())
        .addComponent(createIndicatorPanel())
        .addComponentFillVertically(JPanel(), 0)
        .panel

    val preferredFocusedComponent: JComponent
        get() = defaultLanguageComponent

    var defaultLanguage: String
        get() = defaultLanguageComponent.text
        set(value) {
            defaultLanguageComponent.text = value.lowercase()
        }

    var cursorColor: Color?
        get() = cursorColorComponent.selectedColor
        set(value) {
            cursorColorComponent.selectedColor = value
        }

    var showTextIndicator: Boolean
        get() = showTextIndicatorComponent.isSelected
        set(value) {
            showTextIndicatorComponent.isSelected = value
        }

    var indicateCapsLock: Boolean
        get() = indicateCapsLockComponent.isSelected
        set(value) {
            indicateCapsLockComponent.isSelected = value
        }

    var indicateDefaultLanguage: Boolean
        get() = indicateDefaultLanguageComponent.isSelected
        set(value) {
            indicateDefaultLanguageComponent.isSelected = value
        }

    var textIndicatorColor: Color?
        get() = textIndicatorColorComponent.selectedColor
        set(value) {
            textIndicatorColorComponent.selectedColor = value
        }

    var textIndicatorBackgroundColor: Color?
        get() = textIndicatorBackgroundColorComponent.selectedColor
        set(value) {
            textIndicatorBackgroundColorComponent.selectedColor = value
        }

    private fun createLanguagePanel(): JPanel {
        val languagePanel = JPanel()
        languagePanel.layout = GridBagLayout()
        languagePanel.add(JBLabel("Default language:"), createRbc(0, 0, 0.0))
        languagePanel.add(defaultLanguageComponent, createRbc(1, 0, 0.0, LABEL_SPACING))
        languagePanel.add(detectKeyboardLayoutButton, createRbc(2, 0, 1.0, COMPONENT_SPACING))

        detectKeyboardLayoutButton.addActionListener {
            defaultLanguageComponent.text = keyboardLayout.getLayout().toString().lowercase()
        }

        return languagePanel
    }

    private fun createCursorPanel(): JPanel {
        val cursorPanel = JPanel()
        cursorPanel.layout = GridBagLayout()
        cursorPanel.add(JBLabel("Cursor color:"), createRbc(0, 0, 0.0))
        cursorPanel.add(cursorColorComponent, createRbc(1, 0, 1.0, LABEL_SPACING))

        return cursorPanel
    }

    private fun createIndicatorPanel(): JPanel {
        val checkBoxPanel = JPanel()
        checkBoxPanel.layout = GridBagLayout()
        checkBoxPanel.add(showTextIndicatorComponent, createRbc(0, 0, 0.0))
        checkBoxPanel.add(indicateDefaultLanguageComponent, createRbc(1, 0, 0.0, COMPONENT_SPACING))
        checkBoxPanel.add(indicateCapsLockComponent, createRbc(2, 0, 1.0, COMPONENT_SPACING))

        val colorPanel = JPanel()
        colorPanel.layout = GridBagLayout()
        colorPanel.add(JBLabel("Text color:"), createRbc(0, 0, 0.0))
        colorPanel.add(textIndicatorColorComponent, createRbc(1, 0, 0.0, LABEL_SPACING))
        colorPanel.add(JBLabel("Background color:"), createRbc(2, 0, 0.0, COMPONENT_SPACING))
        colorPanel.add(textIndicatorBackgroundColorComponent, createRbc(3, 0, 1.0, LABEL_SPACING))

        showTextIndicatorComponent.addChangeListener {
            val enabled = showTextIndicator
            indicateDefaultLanguageComponent.isEnabled = enabled
            indicateCapsLockComponent.isEnabled = enabled
            textIndicatorColorComponent.isEnabled = enabled
            textIndicatorBackgroundColorComponent.isEnabled = enabled
        }

        val container = JPanel()
        container.layout = BoxLayout(container, BoxLayout.Y_AXIS)
        container.add(checkBoxPanel)
        container.add(colorPanel)

        return container
    }

    private fun createRbc(x: Int, y: Int, weightx: Double): GridBagConstraints {
        return createRbc(x, y, weightx, null)
    }

    private fun createRbc(x: Int, y: Int, weightx: Double, paddingLeft: Int?): GridBagConstraints {
        val gbc = GridBagConstraints()
        gbc.gridx = x
        gbc.gridy = y
        gbc.gridwidth = 1
        gbc.gridheight = 1

        gbc.anchor = GridBagConstraints.WEST
        gbc.fill = GridBagConstraints.NONE

        gbc.weightx = weightx
        gbc.weighty = 1.0
        gbc.insets = JBUI.insets(10, paddingLeft ?: 0, 0, 0)
        return gbc
    }
}
