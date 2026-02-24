package com.github.siropkin.kursor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import java.awt.Color


@State(
    name = "package com.github.siropkin.kursor.settings.KursorSettings",
    storages = [Storage("Kursor.xml")]
)
class KursorSettings : PersistentStateComponent<KursorSettings> {
    var defaultLanguage: String = "us"

    @OptionTag("cursorColor_", converter = NullableColorConverter::class)
    var cursorColor: Color? = Color(255, 140, 0)

    var showTextIndicator: Boolean = true
    var indicateCapsLock: Boolean = true
    var indicateDefaultLanguage: Boolean = false

    @OptionTag("textIndicatorColor_", converter = ColorConverter::class)
    var textIndicatorColor: Color = Color(255, 140, 0)

    @OptionTag("textIndicatorBackgroundColor_", converter = NullableColorConverter::class)
    var textIndicatorBackgroundColor: Color? = null

    override fun getState(): KursorSettings {
        return this
    }

    override fun loadState(state: KursorSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): KursorSettings {
            return try {
                ApplicationManager.getApplication().getService(KursorSettings::class.java)
            } catch (e: Exception) {
                KursorSettings()
            }
        }
    }
}

class ColorConverter : Converter<Color>() {
    override fun fromString(value: String): Color {
        val parts = value.split(",")
        return Color(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }

    override fun toString(value: Color): String {
        return "${value.red},${value.green},${value.blue}"
    }
}

class NullableColorConverter : Converter<Color?>() {
    override fun fromString(value: String): Color? {
        if (value.isBlank()) return null
        val parts = value.split(",")
        return Color(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }

    override fun toString(value: Color?): String {
        if (value == null) return ""
        return "${value.red},${value.green},${value.blue}"
    }
}
