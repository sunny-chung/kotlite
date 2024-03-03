package com.sunnychung.lib.multiplatform.kotlite.extension

actual val Any.fullClassName: String
    get() = this::class.simpleName!!
