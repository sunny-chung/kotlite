package com.sunnychung.lib.multiplatform.kotlite.error

import com.sunnychung.lib.multiplatform.kotlite.model.NullPointerExceptionValue
import com.sunnychung.lib.multiplatform.kotlite.model.SymbolTable

class EvaluateNullPointerException(currentScope: SymbolTable, stacktrace: List<String>)
    : EvaluateRuntimeException(stacktrace, NullPointerExceptionValue(currentScope = currentScope, stacktrace = stacktrace))
