package com.github.siropkin.kursor

import com.github.siropkin.kursor.keyboard.Keyboard
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.*
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

    private val changeColorOnNonDefaultLanguageComponent = JBCheckBox("Change color on non-default language")
    private val colorOnNonDefaultLanguageComponent = ColorPanel()

    private val showTextIndicatorComponent = JBCheckBox("Show text indicator")
    private val indicateCapsLockComponent = JBCheckBox("Indicate 'Caps Lock'")
    private val indicateDefaultLanguageComponent = JBCheckBox("Show default language")

    private val textIndicatorFontNameComponent = ComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames)
    private val textIndicatorFontStyleComponent = ComboBox(arrayOf(Font.PLAIN.toString(), Font.BOLD.toString(), Font.ITALIC.toString()))
    private val textIndicatorFontSizeComponent = JBTextField()
    private val textIndicatorFontAlphaComponent = JBTextField()

    private val textIndicatorVerticalPositionComponent = ComboBox(arrayOf(TextIndicatorVerticalPositions.TOP, TextIndicatorVerticalPositions.MIDDLE, TextIndicatorVerticalPositions.BOTTOM))
    private val textIndicatorHorizontalOffsetComponent = JBTextField()

    var panel: JPanel = FormBuilder.createFormBuilder()
        .addComponent(createLanguagePanel())
        .addComponent(createColorPanel())
        .addComponent(createIndicatorPanel())
        .addComponent(createPositionPanel())
        .addComponentFillVertically(JPanel(), 0)
        .panel

    val preferredFocusedComponent: JComponent
        get() = defaultLanguageComponent

    var defaultLanguage: String
        get() = defaultLanguageComponent.text
        set(value) {
            defaultLanguageComponent.text = value.lowercase()
        }

    var changeColorOnNonDefaultLanguage: Boolean
        get() = changeColorOnNonDefaultLanguageComponent.isSelected
        set(value) {
            changeColorOnNonDefaultLanguageComponent.isSelected = value
        }

    var colorOnNonDefaultLanguage: Color?
        get() = colorOnNonDefaultLanguageComponent.selectedColor
        set(value) {
            value?.let {
                colorOnNonDefaultLanguageComponent.selectedColor = Color(it.red, it.green, it.blue)
            }
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

    var textIndicatorFontName: String
        get() = textIndicatorFontNameComponent.selectedItem as String
        set(value) {
            textIndicatorFontNameComponent.selectedItem = value
        }

    var textIndicatorFontStyle: Int
        get() = (textIndicatorFontStyleComponent.selectedItem as String).toInt()
        set(value) {
            textIndicatorFontStyleComponent.selectedItem = value.toString()
        }

    var textIndicatorFontSize: Int
        get() = textIndicatorFontSizeComponent.text.toInt()
        set(value) {
            try {
                val intValue = Integer.parseInt(value.toString())
                textIndicatorFontSizeComponent.text = intValue.coerceAtLeast(5).coerceAtMost(20).toString()
            } catch (_: NumberFormatException) {
                // indicatorFontSizeComponent.text = ""
            }
        }

    var textIndicatorFontAlpha: Int
        get() = textIndicatorFontAlphaComponent.text.toInt()
        set(value) {
            try {
                val intValue = Integer.parseInt(value.toString())
                textIndicatorFontAlphaComponent.text = intValue.coerceAtLeast(0).coerceAtMost(255).toString()
            } catch (_: NumberFormatException) {
                // indicatorFontAlphaComponent.text = ""
            }
        }

    var textIndicatorVerticalPosition: String
        get() = textIndicatorVerticalPositionComponent.selectedItem as String
        set(value) {
            textIndicatorVerticalPositionComponent.selectedItem = value
        }

    var textIndicatorHorizontalOffset: Int
        get() = textIndicatorHorizontalOffsetComponent.text.toInt()
        set(value) {
            try {
                val intValue = Integer.parseInt(value.toString())
                textIndicatorHorizontalOffsetComponent.text = intValue.coerceAtLeast(-10).coerceAtMost(10).toString()
            } catch (_: NumberFormatException) {
                // indicatorHorizontalOffsetComponent.text = ""
            }
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

    private fun createColorPanel(): JPanel {
        val colorPanel = JPanel()
        colorPanel.layout = GridBagLayout()
        colorPanel.add(changeColorOnNonDefaultLanguageComponent, createRbc(0, 0, 0.0))
        colorPanel.add(colorOnNonDefaultLanguageComponent, createRbc(1, 0, 1.0, LABEL_SPACING))

        changeColorOnNonDefaultLanguageComponent.addChangeListener {
            colorOnNonDefaultLanguageComponent.isEnabled = changeColorOnNonDefaultLanguage
        }

        return colorPanel
    }

    private fun createIndicatorPanel(): JPanel {
        val checkBoxPanel = JPanel()
        checkBoxPanel.layout = GridBagLayout()
        checkBoxPanel.add(showTextIndicatorComponent, createRbc(0, 0, 0.0))
        checkBoxPanel.add(indicateDefaultLanguageComponent, createRbc(1, 0, 0.0, COMPONENT_SPACING))
        checkBoxPanel.add(indicateCapsLockComponent, createRbc(2, 0, 1.0, COMPONENT_SPACING))

        val fontPanel = JPanel()
        fontPanel.layout = GridBagLayout()
        fontPanel.add(JBLabel("Font:"), createRbc(0, 0, 0.0))
        fontPanel.add(textIndicatorFontNameComponent, createRbc(1, 0, 0.0, LABEL_SPACING))
        fontPanel.add(JBLabel("Size:"), createRbc(2, 0, 0.0, COMPONENT_SPACING))
        fontPanel.add(textIndicatorFontSizeComponent, createRbc(3, 0, 0.0, LABEL_SPACING))
        fontPanel.add(JBLabel("Opacity:"), createRbc(4, 0, 0.0, COMPONENT_SPACING))
        fontPanel.add(textIndicatorFontAlphaComponent, createRbc(5, 0, 1.0, LABEL_SPACING))

        showTextIndicatorComponent.addChangeListener {
            indicateDefaultLanguageComponent.isEnabled = showTextIndicator
            indicateCapsLockComponent.isEnabled = showTextIndicator
            textIndicatorFontNameComponent.isEnabled = showTextIndicator
            textIndicatorFontStyleComponent.isEnabled = showTextIndicator
            textIndicatorFontSizeComponent.isEnabled = showTextIndicator
            textIndicatorFontAlphaComponent.isEnabled = showTextIndicator
            textIndicatorVerticalPositionComponent.isEnabled = showTextIndicator
            textIndicatorHorizontalOffsetComponent.isEnabled = showTextIndicator
        }

        val container = JPanel()
        container.layout = BoxLayout(container, BoxLayout.Y_AXIS)
        container.add(checkBoxPanel)
        container.add(fontPanel)

        return container
    }

    private fun createPositionPanel(): JPanel {
        val positionPanel = JPanel()
        positionPanel.layout = GridBagLayout()
        positionPanel.add(JBLabel("Vertical position:"), createRbc(0, 0, 0.0))
        positionPanel.add(textIndicatorVerticalPositionComponent, createRbc(1, 0, 0.0, LABEL_SPACING))
        positionPanel.add(JBLabel("Horizontal offset:"), createRbc(2, 0, 0.0, COMPONENT_SPACING))
        positionPanel.add(textIndicatorHorizontalOffsetComponent, createRbc(3, 0, 1.0, LABEL_SPACING))

        textIndicatorVerticalPositionComponent.maximumSize = Dimension(200, textIndicatorVerticalPositionComponent.preferredSize.height)
        textIndicatorHorizontalOffsetComponent.maximumSize = Dimension(100, textIndicatorHorizontalOffsetComponent.preferredSize.height)

        return positionPanel
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
