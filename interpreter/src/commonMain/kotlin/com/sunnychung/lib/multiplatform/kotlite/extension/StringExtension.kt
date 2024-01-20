package com.sunnychung.lib.multiplatform.kotlite.extension

fun compareString(s1: String, s2: String): Int {
    if (s1.length < s2.length) return -1
    if (s1.length > s2.length) return 1
    return s1.compareTo(s2)
}
