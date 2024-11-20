package com.github.siropkin.kursor.keyboard

class LinuxConfig {
    var distribution: String = System.getenv("DESKTOP_SESSION")?.lowercase() ?: ""
    var desktopGroup: String = System.getenv("XDG_SESSION_TYPE")?.lowercase() ?: ""
    var availableKeyboardLayouts: List<String> = emptyList()
}