package com.github.siropkin.kursor

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent


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
                    || it.showTextIndicator != settings.showTextIndicator
                    || it.indicateCapsLock != settings.indicateCapsLock
                    || it.indicateDefaultLanguage != settings.indicateDefaultLanguage
                    || it.textIndicatorFontName != settings.textIndicatorFontName
                    || it.textIndicatorFontStyle != settings.textIndicatorFontStyle
                    || it.textIndicatorFontSize != settings.textIndicatorFontSize
                    || it.textIndicatorFontAlpha != settings.textIndicatorFontAlpha
                    || it.textIndicatorVerticalPosition != settings.textIndicatorVerticalPosition
                    || it.textIndicatorHorizontalOffset != settings.textIndicatorHorizontalOffset
        }
        return false
    }

    override fun apply() {
        val settings = KursorSettings.getInstance()
        settings.defaultLanguage = settingsComponent!!.defaultLanguage
        settings.changeColorOnNonDefaultLanguage = settingsComponent!!.changeColorOnNonDefaultLanguage
        settings.colorOnNonDefaultLanguage = settingsComponent!!.colorOnNonDefaultLanguage!!
        settings.showTextIndicator = settingsComponent!!.showTextIndicator
        settings.indicateCapsLock = settingsComponent!!.indicateCapsLock
        settings.indicateDefaultLanguage = settingsComponent!!.indicateDefaultLanguage
        settings.textIndicatorFontName = settingsComponent!!.textIndicatorFontName
        settings.textIndicatorFontStyle = settingsComponent!!.textIndicatorFontStyle
        settings.textIndicatorFontSize = settingsComponent!!.textIndicatorFontSize
        settings.textIndicatorFontAlpha = settingsComponent!!.textIndicatorFontAlpha
        settings.textIndicatorVerticalPosition = settingsComponent!!.textIndicatorVerticalPosition
        settings.textIndicatorHorizontalOffset = settingsComponent!!.textIndicatorHorizontalOffset
    }

    override fun reset() {
        val settings = KursorSettings.getInstance()
        settingsComponent?.let {
            it.defaultLanguage = settings.defaultLanguage
            it.changeColorOnNonDefaultLanguage = settings.changeColorOnNonDefaultLanguage
            it.colorOnNonDefaultLanguage = settings.colorOnNonDefaultLanguage
            it.showTextIndicator = settings.showTextIndicator
            it.indicateCapsLock = settings.indicateCapsLock
            it.indicateDefaultLanguage = settings.indicateDefaultLanguage
            it.textIndicatorFontName = settings.textIndicatorFontName
            it.textIndicatorFontStyle = settings.textIndicatorFontStyle
            it.textIndicatorFontSize = settings.textIndicatorFontSize
            it.textIndicatorFontAlpha = settings.textIndicatorFontAlpha
            it.textIndicatorVerticalPosition = settings.textIndicatorVerticalPosition
            it.textIndicatorHorizontalOffset = settings.textIndicatorHorizontalOffset
        }
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}
