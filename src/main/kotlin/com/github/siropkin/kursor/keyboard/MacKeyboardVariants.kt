package com.github.siropkin.kursor.keyboard


// Standard Layouts
// https://github.com/acidanthera/OpenCorePkg/blob/master/Utilities/AppleKeyboardLayouts/AppleKeyboardLayouts.txt
val MacStandardKeyboardVariants = mapOf(
    "19458" to "RU", // Russian - PC
    "-23205" to "UK", // Ukrainian-QWERTY
    "-2354" to "UK", // Ukrainian
)

// Sogou Pinyin Layouts https://pinyin.sogou.com/mac
val MacSogouPinyinVariants = mapOf(
    "com.sogou.inputmethod.pinyin" to "ZH"
)

// Apple Chinese Input Methods
val MacAppleChineseVariants = mapOf(
    "com.apple.inputmethod.SCIM" to "ZH",
    "com.apple.inputmethod.SCIM.ITABC" to "ZH",
    "com.apple.inputmethod.SCIM.WBX" to "ZH",
    "com.apple.inputmethod.SCIM.Shuangpin" to "ZH",
    "com.apple.inputmethod.TCIM" to "ZH",
    "com.apple.inputmethod.TCIM.Zhuyin" to "ZH",
    "com.apple.inputmethod.TCIM.Cangjie" to "ZH",
    "com.apple.inputmethod.TCIM.Pinyin" to "ZH",
)

// Rime Squirrel Layouts https://rime.im
val MacRimeSquirrelVariants = mapOf(
    "im.rime.inputmethod.Squirrel.Hans" to "ZH", // Simplified
    "im.rime.inputmethod.Squirrel.Hant" to "ZH" // Traditional
)