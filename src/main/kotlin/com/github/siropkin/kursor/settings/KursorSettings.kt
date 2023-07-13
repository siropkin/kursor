package com.github.siropkin.kursor.settings

import com.github.siropkin.kursor.Position
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.util.xmlb.XmlSerializerUtil
import java.awt.Color
import java.awt.Font


@State(
    name = "package com.github.siropkin.kursor.settings.KursorSettings",
    storages = [Storage("Kursor.xml")]
)
class KursorSettings : PersistentStateComponent<KursorSettings> {
    var defaultLanguage: String = "en"

    var changeColorOnNonDefaultLanguage: Boolean = true
    var colorOnNonDefaultLanguage: Color = Color(255, 140, 0)

    var showIndicator: Boolean = true
    var indicateCapsLock: Boolean = true
    var indicateDefaultLanguage: Boolean = false

    var indicatorFontName: String = EditorColorsManager.getInstance().globalScheme.editorFontName
    var indicatorFontStyle: Int = Font.PLAIN
    var indicatorFontSize: Int = 11
    var indicatorFontAlpha: Int = 180

    var indicatorVerticalPosition: String = Position.TOP
    var indicatorHorizontalOffset: Int = 4

    override fun getState(): KursorSettings {
        return this
    }

    override fun loadState(state: KursorSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): KursorSettings {
            return ApplicationManager.getApplication().getService(KursorSettings::class.java)
        }
    }
}
