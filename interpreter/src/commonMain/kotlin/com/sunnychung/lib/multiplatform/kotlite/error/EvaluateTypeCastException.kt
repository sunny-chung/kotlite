package com.sunnychung.lib.multiplatform.kotlite.error

class EvaluateTypeCastException(val valueType: String, val targetType: String)
    : EvaluateRuntimeException("`$valueType` cannot be casted to type `$targetType`")
