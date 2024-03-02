package com.sunnychung.lib.multiplatform.kotlite.error

import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition

class CannotInferTypeException(position: SourcePosition, what: String? = null) : SemanticException(position, "Cannot infer ${what ?: "at least one type"}. Please specify types manually")
