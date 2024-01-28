package com.sunnychung.lib.multiplatform.kotlite.error

class CannotInferTypeException(what: String? = null) : SemanticException("Cannot infer ${what ?: "at least one type"}. Please specify types manually")
