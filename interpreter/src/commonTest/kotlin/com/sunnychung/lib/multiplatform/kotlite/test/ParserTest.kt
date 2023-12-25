package com.sunnychung.lib.multiplatform.kotlite.test

import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {
    private fun parser(code: String) = Parser(Lexer(code))

    @Test
    fun expressionWithBracketsAndBinaryOpsAndUnaryOps() {
        val expr = parser("1 + 5 * (7 + 3 )  - - - + - 2 ").expression()
        println(expr.toMermaid())
//        assertTrue(expr is BinaryOpNode)
//        assertEquals("+", expr.operator)
//        assertTrue(expr.node1 is IntegerNode)
//        assertEquals(1, (expr.node1 as IntegerNode).token.value)
//        assertTrue(expr.node2 is BinaryOpNode)
//        assertTrue((expr.node2 as BinaryOpNode).node1 is IntegerNode)
//        assertEquals(5, ((expr.node2 as BinaryOpNode).node1 as IntegerNode).token.value)
//        assertTrue((expr.node2 as BinaryOpNode).node2 is BinaryOpNode)
//        assertTrue(((expr.node2 as BinaryOpNode).node2 as BinaryOpNode).operator == "*")
//        assertTrue((((expr.node2 as BinaryOpNode).node2 as BinaryOpNode).node1 as IntegerNode) == "*")
    }

    @Test
    fun canParseFunctionCals() {
        parser("""
            fun abc(a: Int, b : Int) {
                val x: Int = 1 + a - b
                val y: Int = x * x
            }
            
            abc(1 + 3, 2)
            val a: Unit = abc(3, 4 * 5)
        """.trimIndent()).script()
            .nodes.any { it is FunctionCallNode }
            .apply { assertTrue(this) }
    }
}