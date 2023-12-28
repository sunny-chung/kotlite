package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.CodeGenerator
import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.SemanticAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

fun interpreter(code: String, isDebug: Boolean = false) = Parser(Lexer(code)).script().let {
    SemanticAnalyzer(it).analyze()
    if (isDebug) {
        println(CodeGenerator(it).generateCode())
    }
    Interpreter(it)
}
