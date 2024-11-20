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

// Rime Squirrel Layouts https://rime.im
val MacRimeSquirrelVariants = mapOf(
    "im.rime.inputmethod.Squirrel.Hans" to "ZH", // Simplified
    "im.rime.inputmethod.Squirrel.Hant" to "ZH" // Traditional
)