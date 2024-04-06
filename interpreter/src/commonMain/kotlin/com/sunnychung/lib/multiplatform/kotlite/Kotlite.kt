package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.RuntimeValue

fun KotliteInterpreter(filename: String, code: String, executionEnvironment: ExecutionEnvironment): Interpreter {
    val ast = Parser(Lexer(filename = filename, code = code)).script()
    SemanticAnalyzer(rootNode = ast, executionEnvironment = executionEnvironment).analyze()
    return Interpreter(rootNode = ast, executionEnvironment = executionEnvironment)
}

fun evalKotliteExpression(filename: String, code: String, executionEnvironment: ExecutionEnvironment): RuntimeValue {
    val ast = Parser(Lexer(filename = filename, code = code)).expression()
    SemanticAnalyzer(rootNode = ast, executionEnvironment = executionEnvironment).analyze()
    return Interpreter(rootNode = ast, executionEnvironment = executionEnvironment).eval()
}

fun kotliteAstNodeMermaidDiagram(code: String, direction: MermaidFlowchartDirection): String {
    val ast = Parser(Lexer(filename = "ToMermaid", code = code)).script()
    return buildString {
        append("flowchart ")
        append(when(direction) {
            MermaidFlowchartDirection.TopDown -> "TD"
            MermaidFlowchartDirection.LeftToRight -> "LR"
        })
        append("\n")
        append(ast.toMermaid())
        append("\n")
    }.replace("\n+".toRegex(), "\n")
}

enum class MermaidFlowchartDirection {
    TopDown, LeftToRight
}
