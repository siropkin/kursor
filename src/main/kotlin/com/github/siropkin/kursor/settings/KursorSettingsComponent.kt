package com.github.siropkin.kursor.settings

import com.github.siropkin.kursor.Position
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.*
import java.util.*
import javax.swing.*


private const val LABEL_SPACING = 10
private const val COMPONENT_SPACING = 35

class KursorSettingsComponent {
    private val availableLanguages = Locale.getAvailableLocales()
        .map { it.language }
        .distinct()
        .filter { it.isNotEmpty() }
        .sorted()
        .toTypedArray()
    private val defaultLanguageComponent = JComboBox(availableLanguages)

    private val changeColorOnNonDefaultLanguageComponent = JBCheckBox("Change color on non-default language")
    private val colorOnNonDefaultLanguageComponent = ColorPanel()

    private val showIndicatorComponent = JBCheckBox("Show text indicator")
    private val indicateCapsLockComponent = JBCheckBox("Indicate Caps Lock")
    private val indicateDefaultLanguageComponent = JBCheckBox("Show default language")

    private val indicatorFontNameComponent = JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames)
    private val indicatorFontStyleComponent = JComboBox(arrayOf(Font.PLAIN.toString(), Font.BOLD.toString(), Font.ITALIC.toString()))
    private val indicatorFontSizeComponent = JBTextField()
    private val indicatorFontAlphaComponent = JBTextField()

    private val indicatorVerticalPositionComponent = JComboBox(arrayOf(Position.TOP, Position.MIDDLE, Position.BOTTOM))
    private val indicatorHorizontalOffsetComponent = JBTextField()

    var panel: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Default language:", defaultLanguageComponent, 1, false)
        .addComponent(createColorPanel())
        .addComponent(createIndicatorPanel())
        .addComponent(createPositionPanel())
        .addComponentFillVertically(JPanel(), 0)
        .panel

    val preferredFocusedComponent: JComponent
        get() = defaultLanguageComponent

    var defaultLanguage: String
        get() = defaultLanguageComponent.selectedItem as String
        set(value) {
            defaultLanguageComponent.selectedItem = value
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

    var showIndicator: Boolean
        get() = showIndicatorComponent.isSelected
        set(value) {
            showIndicatorComponent.isSelected = value
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

    var indicatorFontName: String
        get() = indicatorFontNameComponent.selectedItem as String
        set(value) {
            indicatorFontNameComponent.selectedItem = value
        }

    var indicatorFontStyle: Int
        get() = (indicatorFontStyleComponent.selectedItem as String).toInt()
        set(value) {
            indicatorFontStyleComponent.selectedItem = value.toString()
        }

    var indicatorFontSize: Int
        get() = indicatorFontSizeComponent.text.toInt()
        set(value) {
            try {
                val intValue = Integer.parseInt(value.toString())
                indicatorFontSizeComponent.text = intValue.coerceAtLeast(5).coerceAtMost(20).toString()
            } catch (_: NumberFormatException) {
                // indicatorFontSizeComponent.text = ""
            }
        }

    var indicatorFontAlpha: Int
        get() = indicatorFontAlphaComponent.text.toInt()
        set(value) {
            try {
                val intValue = Integer.parseInt(value.toString())
                indicatorFontAlphaComponent.text = intValue.coerceAtLeast(0).coerceAtMost(255).toString()
            } catch (_: NumberFormatException) {
                // indicatorFontAlphaComponent.text = ""
            }
        }

    var indicatorVerticalPosition: String
        get() = indicatorVerticalPositionComponent.selectedItem as String
        set(value) {
            indicatorVerticalPositionComponent.selectedItem = value
        }

    var indicatorHorizontalOffset: Int
        get() = indicatorHorizontalOffsetComponent.text.toInt()
        set(value) {
            try {
                val intValue = Integer.parseInt(value.toString())
                indicatorHorizontalOffsetComponent.text = intValue.coerceAtLeast(-10).coerceAtMost(10).toString()
            } catch (_: NumberFormatException) {
                // indicatorHorizontalOffsetComponent.text = ""
            }
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
        checkBoxPanel.add(showIndicatorComponent, createRbc(0, 0, 0.0))
        checkBoxPanel.add(indicateDefaultLanguageComponent, createRbc(1, 0, 0.0, COMPONENT_SPACING))
        checkBoxPanel.add(indicateCapsLockComponent, createRbc(2, 0, 1.0, COMPONENT_SPACING))

        val fontPanel = JPanel()
        fontPanel.layout = GridBagLayout()
        fontPanel.add(JBLabel("Font:"), createRbc(0, 0, 0.0))
        fontPanel.add(indicatorFontNameComponent, createRbc(1, 0, 0.0, LABEL_SPACING))
        fontPanel.add(JBLabel("Size:"), createRbc(2, 0, 0.0, COMPONENT_SPACING))
        fontPanel.add(indicatorFontSizeComponent, createRbc(3, 0, 0.0, LABEL_SPACING))
        fontPanel.add(JBLabel("Opacity:"), createRbc(4, 0, 0.0, COMPONENT_SPACING))
        fontPanel.add(indicatorFontAlphaComponent, createRbc(5, 0, 1.0, LABEL_SPACING))

        showIndicatorComponent.addChangeListener {
            indicateDefaultLanguageComponent.isEnabled = showIndicator
            indicateCapsLockComponent.isEnabled = showIndicator
            indicatorFontNameComponent.isEnabled = showIndicator
            indicatorFontStyleComponent.isEnabled = showIndicator
            indicatorFontSizeComponent.isEnabled = showIndicator
            indicatorFontAlphaComponent.isEnabled = showIndicator
            indicatorVerticalPositionComponent.isEnabled = showIndicator
            indicatorHorizontalOffsetComponent.isEnabled = showIndicator
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
        positionPanel.add(indicatorVerticalPositionComponent, createRbc(1, 0, 0.0, LABEL_SPACING))
        positionPanel.add(JBLabel("Horizontal offset:"), createRbc(2, 0, 0.0, COMPONENT_SPACING))
        positionPanel.add(indicatorHorizontalOffsetComponent, createRbc(3, 0, 1.0, LABEL_SPACING))

        indicatorVerticalPositionComponent.maximumSize = Dimension(200, indicatorVerticalPositionComponent.preferredSize.height)
        indicatorHorizontalOffsetComponent.maximumSize = Dimension(100, indicatorHorizontalOffsetComponent.preferredSize.height)

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
        gbc.insets = Insets(10, paddingLeft ?: 0, 0, 0)
        return gbc
    }
}
