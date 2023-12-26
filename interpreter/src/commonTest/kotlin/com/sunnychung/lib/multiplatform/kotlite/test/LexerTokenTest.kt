package com.sunnychung.lib.multiplatform.kotlite.test

import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.Token
import com.sunnychung.lib.multiplatform.kotlite.model.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LexerTokenTest {
    fun assertToken(expectedType: TokenType, expectedValue: Any?, actual: Token) {
        assertEquals(expectedType, actual.type)
        assertEquals(expectedValue, actual.value)
    }

    @Test
    fun integersAndSpaces() {
        val tokens = Lexer("1 + 5 * 7  - 2 ").readAllTokens()
        println(tokens)
        assertTrue { tokens.size == 8 }
        assertToken(TokenType.Integer, 1, tokens[0])
        assertToken(TokenType.Operator, "+", tokens[1])
        assertToken(TokenType.Integer, 5, tokens[2])
        assertToken(TokenType.Operator, "*", tokens[3])
        assertToken(TokenType.Integer, 7, tokens[4])
        assertToken(TokenType.Operator, "-", tokens[5])
        assertToken(TokenType.Integer, 2, tokens[6])
        assertEquals(TokenType.EOF, tokens[7].type)
    }

    @Test
    fun integersAndSpacesAndBrackets() {
        val tokens = Lexer("1 + 5 * (7 + 3 )  - 2 ").readAllTokens()
        println(tokens)
    }
}