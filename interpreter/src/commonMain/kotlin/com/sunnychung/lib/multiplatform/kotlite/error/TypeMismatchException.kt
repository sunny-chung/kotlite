package com.sunnychung.lib.multiplatform.kotlite.error

class TypeMismatchException(expected: String, actual: String)
    : SemanticException("Expected type is `$expected`, but actual type is `$actual`")
