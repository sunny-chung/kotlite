package com.sunnychung.lib.multiplatform.kotlite.error

import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition

class TypeMismatchException(position: SourcePosition, expected: String, actual: String)
    : SemanticException(position, "Expected type is `$expected`, but actual type is `$actual`")
