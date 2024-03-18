package com.sunnychung.lib.multiplatform.kotlite.error

import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition

open class SemanticException(position: SourcePosition, message: String, cause: Throwable? = null)
    : Exception("$message at [${position.filename}:${position.lineNum}:${position.col}]", cause)
