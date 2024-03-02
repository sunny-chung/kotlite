package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.CodeGenerator
import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.SemanticAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

fun interpreter(
    code: String,
    isDebug: Boolean = true,
    executionEnvironment: ExecutionEnvironment = ExecutionEnvironment()
) = Parser(Lexer(filename = "<Test>", code = code)).let { parser ->
    val it = parser.script()
    if (isDebug) {
        println("Tokens: ${parser.allTokens}")
        println("AST:\n---\nflowchart TD\n${it.toMermaid()}\n---")
    }
    SemanticAnalyzer(it, executionEnvironment).analyze()
    if (isDebug) {
        println(CodeGenerator(it, isPrintDebugInfo = true).generateCode())
    }
    Interpreter(it, executionEnvironment)
}
