package com.sunnychung.lib.multiplatform.kotlite.test.lexer

import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.Token
import com.sunnychung.lib.multiplatform.kotlite.model.TokenType
import com.sunnychung.lib.multiplatform.kotlite.test.lexer
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerReadTest {

    fun Token.assertToken(type: TokenType, value: Any) {
        assertEquals(type, this.type)
        assertEquals(value, this.value)
    }

    @Test
    fun resetIndex() {
        val lexer = lexer("a = b + c")
        assertEquals(0, lexer.currentCursor())
        lexer.readToken().assertToken(TokenType.Identifier, "a")
        assertEquals(1, lexer.currentCursor())
        lexer.readToken().assertToken(TokenType.Symbol, "=")
        assertEquals(3, lexer.currentCursor())
        lexer.readToken().assertToken(TokenType.Identifier, "b")
        assertEquals(5, lexer.currentCursor())
        lexer.readToken().assertToken(TokenType.Operator, "+")
        assertEquals(7, lexer.currentCursor())
        lexer.resetCursorTo(1)
        lexer.readToken().assertToken(TokenType.Symbol, "=")
    }
}
