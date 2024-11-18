package com.github.siropkin.kursor.keyboard


data class KeyboardLayout(val language: String, val country: String, val variant: String, val asciiMode: Boolean = false) {
    fun isEmpty(): Boolean = language.isEmpty() && country.isEmpty() && variant.isEmpty()

    override fun toString(): String = listOf(variant, country, language).firstOrNull { it.isNotEmpty() } ?: ""
}