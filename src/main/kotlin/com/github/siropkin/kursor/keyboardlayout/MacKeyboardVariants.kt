package com.github.siropkin.kursor.keyboardlayout


// https://github.com/acidanthera/OpenCorePkg/blob/master/Utilities/AppleKeyboardLayouts/AppleKeyboardLayouts.txt
val MacKeyboardVariants = mapOf(
    // Standard layouts
    "UserDefined_19458" to "RU", // Russian - PC
    "UserDefined_-23205" to "UK", // Ukrainian-QWERTY
    "UserDefined_-2354" to "UK", // Ukrainian
    // Additional layouts
    "UserDefined_com.sogou.inputmethod.pinyin" to "ZH", // Zhuyin, Sogou Pinyin: https://pinyin.sogou.com/mac
    "UserDefined_im.rime.inputmethod.Squirrel.Hans" to "ZH", // Zhuyin , Squirrel - Simplified： https://rime.im
    "UserDefined_im.rime.inputmethod.Squirrel.Hant" to "ZH" // Zhuyin, Squirrel - Traditional： https://rime.im
)
