package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.CodeGenerator
import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.SemanticAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

fun interpreter(code: String, isDebug: Boolean = true) = Parser(Lexer(code)).let { parser ->
    val it = parser.script()
    if (isDebug) {
        println(parser.allTokens)
    }
    SemanticAnalyzer(it).analyze()
    if (isDebug) {
        println(CodeGenerator(it).generateCode())
    }
    Interpreter(it)
}
