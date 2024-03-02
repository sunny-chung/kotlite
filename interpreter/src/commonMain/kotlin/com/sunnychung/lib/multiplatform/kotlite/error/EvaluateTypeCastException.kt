package com.sunnychung.lib.multiplatform.kotlite.error

import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.TypeCastExceptionValue

class EvaluateTypeCastException(currentScope: SymbolTable, stacktrace: List<String>, val valueType: String, val targetType: String)
    : EvaluateRuntimeException(stacktrace, TypeCastExceptionValue(
    currentScope = currentScope,
    valueType = valueType,
    targetType = targetType,
    stacktrace = stacktrace,
))
