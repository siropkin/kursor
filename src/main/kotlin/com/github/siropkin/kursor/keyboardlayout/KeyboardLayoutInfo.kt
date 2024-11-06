package com.github.siropkin.kursor.keyboardlayout


class KeyboardLayoutInfo(private val language: String, private val country: String, private val variant: String) {
    override fun toString(): String = variant.ifEmpty {
        country.ifEmpty {
            language
        }
    }
}
