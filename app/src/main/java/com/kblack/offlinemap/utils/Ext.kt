package com.kblack.offlinemap.utils

import java.text.Normalizer
import java.util.Locale

fun String.toGlobalSearchVector(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val regex = "\\p{Mn}+".toRegex()
    val cleanText = regex.replace(normalized, "")
        .replace('đ', 'd').replace('Đ', 'D')
    return cleanText.lowercase(Locale.ROOT).trim()
}

fun String.containsNonLatin(): Boolean {
    return this.codePoints().anyMatch { cp ->
        val script = Character.UnicodeScript.of(cp)
        script != Character.UnicodeScript.LATIN &&
                script != Character.UnicodeScript.COMMON &&
                script != Character.UnicodeScript.INHERITED
    }
}