package com.sunnychung.lib.multiplatform.kotlite.test

import com.sunnychung.lib.multiplatform.kotlite.Parser
import com.sunnychung.lib.multiplatform.kotlite.error.ExpectTokenMismatchException
import com.sunnychung.lib.multiplatform.kotlite.error.ParseException
import com.sunnychung.lib.multiplatform.kotlite.error.UnexpectedTokenException
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.Token
import com.sunnychung.lib.multiplatform.kotlite.model.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

fun parser(code: String) = Parser(Lexer(filename = "<Test>", code = code))

class ParserTest {

    fun Token.assertToken(type: TokenType, value: Any) {
        assertEquals(type, this.type)
        assertEquals(value, this.value)
    }

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
    fun canParseFunctionCalls() {
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

    @Test
    fun read() {
        val parser = parser("a = b + c")
        assertEquals(0, parser.tokenIndex)
        assertEquals(1, parser.allTokens.size)
        assertEquals(2, parser.tokenCharIndexes.size)
        assertEquals(0, parser.tokenCharIndexes[0])
        parser.currentToken.assertToken(TokenType.Identifier, "a")

        parser.readToken().assertToken(TokenType.Symbol, "=")
        parser.currentToken.assertToken(TokenType.Symbol, "=")
        assertEquals(1, parser.tokenIndex)
        assertEquals(2, parser.allTokens.size)
        assertEquals(3, parser.tokenCharIndexes.size)
        assertEquals(0, parser.tokenCharIndexes[0])
        assertEquals(1, parser.tokenCharIndexes[1])

        parser.readToken().assertToken(TokenType.Identifier, "b")
        parser.currentToken.assertToken(TokenType.Identifier, "b")
        assertEquals(2, parser.tokenIndex)
        assertEquals(3, parser.allTokens.size)
        assertEquals(4, parser.tokenCharIndexes.size)
        assertEquals(0, parser.tokenCharIndexes[0])
        assertEquals(1, parser.tokenCharIndexes[1])
        assertEquals(3, parser.tokenCharIndexes[2])

        parser.resetTokenToIndex(1)
        parser.currentToken.assertToken(TokenType.Symbol, "=")
        assertEquals(1, parser.tokenIndex)
        assertEquals(2, parser.allTokens.size)
        assertEquals(3, parser.tokenCharIndexes.size)
        assertEquals(0, parser.tokenCharIndexes[0])
        assertEquals(1, parser.tokenCharIndexes[1])

        parser.readToken().assertToken(TokenType.Identifier, "b")
        parser.currentToken.assertToken(TokenType.Identifier, "b")
        assertEquals(2, parser.tokenIndex)
        assertEquals(3, parser.allTokens.size)
        assertEquals(4, parser.tokenCharIndexes.size)
        assertEquals(0, parser.tokenCharIndexes[0])
        assertEquals(1, parser.tokenCharIndexes[1])
        assertEquals(3, parser.tokenCharIndexes[2])
    }

    @Test
    fun currentTokenExcludingNL() {
        val parser = parser("a = \n\n\n\n b + c")
        assertEquals(0, parser.tokenIndex)
        assertEquals(1, parser.allTokens.size)
        assertEquals(2, parser.tokenCharIndexes.size)
        assertEquals(0, parser.tokenCharIndexes[0])
        parser.currentToken.assertToken(TokenType.Identifier, "a")
        parser.currentTokenExcludingNL().assertToken(TokenType.Identifier, "a")
        parser.currentTokenExcludingNL().assertToken(TokenType.Identifier, "a")

        parser.readToken().assertToken(TokenType.Symbol, "=")
        parser.currentTokenExcludingNL().assertToken(TokenType.Symbol, "=")
        parser.currentTokenExcludingNL().assertToken(TokenType.Symbol, "=")

        parser.readToken().assertToken(TokenType.NewLine, "\n")
        parser.currentTokenExcludingNL().assertToken(TokenType.Identifier, "b")
        parser.currentTokenExcludingNL().assertToken(TokenType.Identifier, "b")

        parser.readToken().assertToken(TokenType.NewLine, "\n")
        parser.readToken().assertToken(TokenType.NewLine, "\n")
        parser.readToken().assertToken(TokenType.NewLine, "\n")

        parser.readToken().assertToken(TokenType.Identifier, "b")
        parser.currentTokenExcludingNL().assertToken(TokenType.Identifier, "b")
        parser.currentTokenExcludingNL().assertToken(TokenType.Identifier, "b")

        parser.readToken().assertToken(TokenType.Operator, "+")
        parser.currentTokenExcludingNL().assertToken(TokenType.Operator, "+")
        parser.currentTokenExcludingNL().assertToken(TokenType.Operator, "+")

        parser.readToken().assertToken(TokenType.Identifier, "c")
        parser.currentTokenExcludingNL().assertToken(TokenType.Identifier, "c")
        parser.currentTokenExcludingNL().assertToken(TokenType.Identifier, "c")

        parser.readToken().assertToken(TokenType.EOF, '\u0000')
    }

    @Test
    fun invalidQuestionMarkInExtensionFunctionDeclaration1() {
        assertFailsWith<ParseException> {
            parser("""
                fun Int.func?(): Int = 2
            """.trimIndent()
            ).script()
        }
    }

    @Test
    fun invalidQuestionMarkInExtensionFunctionDeclaration2() {
        assertFailsWith<ExpectTokenMismatchException> {
            parser("""
                fun kotlin?.Int.func(): Int = 2
            """.trimIndent()
            ).script()
        }
    }

    @Test
    fun nonAbstractFunctionMustHaveBody() {
        assertFailsWith<ExpectTokenMismatchException> {
            parser("""
                fun f()
            """.trimIndent()).script()
        }
    }
}