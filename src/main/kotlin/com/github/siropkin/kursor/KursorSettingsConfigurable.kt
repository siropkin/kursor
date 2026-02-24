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
                    || it.cursorColor != settings.cursorColor
                    || it.showTextIndicator != settings.showTextIndicator
                    || it.indicateCapsLock != settings.indicateCapsLock
                    || it.indicateDefaultLanguage != settings.indicateDefaultLanguage
                    || it.textIndicatorColor != settings.textIndicatorColor
                    || it.textIndicatorBackgroundColor != settings.textIndicatorBackgroundColor
        }
        return false
    }

    override fun apply() {
        val settings = KursorSettings.getInstance()
        settings.defaultLanguage = settingsComponent!!.defaultLanguage
        settings.cursorColor = settingsComponent!!.cursorColor
        settings.showTextIndicator = settingsComponent!!.showTextIndicator
        settings.indicateCapsLock = settingsComponent!!.indicateCapsLock
        settings.indicateDefaultLanguage = settingsComponent!!.indicateDefaultLanguage
        settings.textIndicatorColor = settingsComponent!!.textIndicatorColor ?: settings.textIndicatorColor
        settings.textIndicatorBackgroundColor = settingsComponent!!.textIndicatorBackgroundColor
    }

    override fun reset() {
        val settings = KursorSettings.getInstance()
        settingsComponent?.let {
            it.defaultLanguage = settings.defaultLanguage
            it.cursorColor = settings.cursorColor
            it.showTextIndicator = settings.showTextIndicator
            it.indicateCapsLock = settings.indicateCapsLock
            it.indicateDefaultLanguage = settings.indicateDefaultLanguage
            it.textIndicatorColor = settings.textIndicatorColor
            it.textIndicatorBackgroundColor = settings.textIndicatorBackgroundColor
        }
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}
