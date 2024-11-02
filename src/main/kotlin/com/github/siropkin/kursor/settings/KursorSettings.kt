package com.github.siropkin.kursor.settings

import com.github.siropkin.kursor.IndicatorPosition
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import java.awt.Color
import java.awt.Font


val COLOR_ON_NON_DEFAULT_LANGUAGE = Color(255, 140, 0)

@State(
    name = "package com.github.siropkin.kursor.settings.KursorSettings",
    storages = [Storage("Kursor.xml")]
)
class KursorSettings : PersistentStateComponent<KursorSettings> {
    var defaultLanguage: String = "us"

    var changeColorOnNonDefaultLanguage: Boolean = true
    @OptionTag("colorOnNonDefaultLanguage_", converter = ColorConverter::class)
    var colorOnNonDefaultLanguage: Color = COLOR_ON_NON_DEFAULT_LANGUAGE

    var showTextIndicator: Boolean = true

    var textIndicatorFontName: String = EditorColorsManager.getInstance().globalScheme.editorFontName
    var textIndicatorFontStyle: Int = Font.PLAIN
    var textIndicatorFontSize: Int = 11
    var textIndicatorFontAlpha: Int = 180

    var textIndicatorVerticalPosition: String = IndicatorPosition.TOP
    var textIndicatorHorizontalOffset: Int = 4

    var indicateCapsLock: Boolean = true
    var indicateDefaultLanguage: Boolean = false

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

class ColorConverter : Converter<Color>() {
    override fun fromString(value: String): Color {
        val parts = value.split(",")
        return Color(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
    }

    override fun toString(value: Color): String {
        return "${value.red},${value.green},${value.blue},${value.alpha}"
    }
}
