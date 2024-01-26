package com.sunnychung.lib.multiplatform.kotlite.extension

fun <T> List<T>.emptyToNull() = ifEmpty { null }
