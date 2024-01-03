package com.sunnychung.lib.multiplatform.kotlite.extension

fun <T> MutableList<T>.removeAfterIndex(afterIndex: Int) {
    while (lastIndex > afterIndex) {
        removeLast()
    }
}
