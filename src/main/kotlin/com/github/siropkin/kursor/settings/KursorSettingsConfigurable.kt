package com.github.siropkin.kursor.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import javax.swing.JPanel


class KursorSettingsConfigurable: Configurable {
    private var settingsComponent: KursorSettingsComponent? = null

    override fun getDisplayName(): String {
        return "Kursor"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return settingsComponent?.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        settingsComponent = KursorSettingsComponent()
        return settingsComponent?.panel
    }

    override fun isModified(): Boolean {
        val settings = KursorSettings.getInstance()
        settingsComponent?.let {
            return it.defaultLanguage != settings.defaultLanguage
                    || it.changeColorOnNonDefaultLanguage != settings.changeColorOnNonDefaultLanguage
                    || it.colorOnNonDefaultLanguage != settings.colorOnNonDefaultLanguage
                    || it.showIndicator != settings.showIndicator
                    || it.indicateCapsLock != settings.indicateCapsLock
                    || it.indicateDefaultLanguage != settings.indicateDefaultLanguage
                    || it.indicatorFontFamily != settings.indicatorFontFamily
                    || it.indicatorFontStyle != settings.indicatorFontStyle
                    || it.indicatorFontSize != settings.indicatorFontSize
                    || it.indicatorFontAlpha != settings.indicatorFontAlpha
                    || it.indicatorVerticalPosition != settings.indicatorVerticalPosition
                    || it.indicatorHorizontalOffset != settings.indicatorHorizontalOffset
        }
        return false
    }

    override fun apply() {
        val settings = KursorSettings.getInstance()
        settings.defaultLanguage = settingsComponent!!.defaultLanguage
        settings.changeColorOnNonDefaultLanguage = settingsComponent!!.changeColorOnNonDefaultLanguage
        settings.colorOnNonDefaultLanguage = settingsComponent!!.colorOnNonDefaultLanguage!!
        settings.showIndicator = settingsComponent!!.showIndicator
        settings.indicateCapsLock = settingsComponent!!.indicateCapsLock
        settings.indicateDefaultLanguage = settingsComponent!!.indicateDefaultLanguage
        settings.indicatorFontFamily = settingsComponent!!.indicatorFontFamily
        settings.indicatorFontStyle = settingsComponent!!.indicatorFontStyle
        settings.indicatorFontSize = settingsComponent!!.indicatorFontSize
        settings.indicatorFontAlpha = settingsComponent!!.indicatorFontAlpha
        settings.indicatorVerticalPosition = settingsComponent!!.indicatorVerticalPosition
        settings.indicatorHorizontalOffset = settingsComponent!!.indicatorHorizontalOffset
    }

    override fun reset() {
        val settings = KursorSettings.getInstance()
        settingsComponent?.let {
            it.defaultLanguage = settings.defaultLanguage
            it.changeColorOnNonDefaultLanguage = settings.changeColorOnNonDefaultLanguage
            it.colorOnNonDefaultLanguage = settings.colorOnNonDefaultLanguage
            it.showIndicator = settings.showIndicator
            it.indicateCapsLock = settings.indicateCapsLock
            it.indicateDefaultLanguage = settings.indicateDefaultLanguage
            it.indicatorFontFamily = settings.indicatorFontFamily
            it.indicatorFontStyle = settings.indicatorFontStyle
            it.indicatorFontSize = settings.indicatorFontSize
            it.indicatorFontAlpha = settings.indicatorFontAlpha
            it.indicatorVerticalPosition = settings.indicatorVerticalPosition
            it.indicatorHorizontalOffset = settings.indicatorHorizontalOffset
        }
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}
