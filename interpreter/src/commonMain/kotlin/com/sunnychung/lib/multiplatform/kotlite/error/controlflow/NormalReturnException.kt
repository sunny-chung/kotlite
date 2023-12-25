package com.sunnychung.lib.multiplatform.kotlite.error.controlflow

import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue

class NormalReturnException(val returnToAddress: String, val value: RuntimeValue) : NormalControlFlowException("Return")