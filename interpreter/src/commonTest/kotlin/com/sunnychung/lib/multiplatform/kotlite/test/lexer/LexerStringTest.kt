package com.sunnychung.lib.multiplatform.kotlite.test.lexer

import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.TokenType
import com.sunnychung.lib.multiplatform.kotlite.test.lexer
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerStringTest {

    fun lexQuotedString(doubleQuotedString: String) = lexer(doubleQuotedString).also {
        it.advanceChar()
        it.switchToMode(Lexer.Mode.QuotedString)
    }

    @Test
    fun simpleString() {
        val tokens = lexQuotedString("\"abc defg.hij + kl\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("abc defg.hij + kl", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun simpleStringWithEscape() {
        val tokens = lexQuotedString("\"abc \\t\\r\\n\\\$defg.\\nhij + kl\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("abc \t\r\n\$defg.\nhij + kl", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun escapedUnicode1stRange() {
        val tokens = lexQuotedString("\"\\u0041\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("A", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun escapedUnicode2ndRange() {
        val tokens = lexQuotedString("\"\\u03c0\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("π", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun escapedUnicode3rdRange() {
        val tokens = lexQuotedString("\"\\u20AC\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("€", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun escapedUnicodeChinese() {
        val tokens = lexQuotedString("\"\\u6211\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("我", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun escapedUnicodeMultiChinese() {
        val tokens = lexQuotedString("\"\\u6211\\u6211\\u6211\\u6211\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("我我我我", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun escapedUnicodeMultiChar1a() {
        val tokens = lexQuotedString("\"\\ud83d\\ude01\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("😁", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun escapedUnicodeMultiChar1b() {
        val tokens = lexQuotedString("\"\\ud83d\\ude01\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("\ud83d\ude01", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun escapedUnicodeMultiChar2a() {
        val tokens = lexQuotedString("\"\\uD83C\\uDDED\\uD83C\\uDDF0\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("🇭🇰", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun escapedUnicodeMultiChar2b() {
        val tokens = lexQuotedString("\"\\uD83C\\uDDED\\uD83C\\uDDF0\"").readAllTokens()
        assertEquals(3, tokens.size)
        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("\uD83C\uDDED\uD83C\uDDF0", tokens[0].value)
        assertEquals(TokenType.Symbol, tokens[1].type)
        assertEquals("\"", tokens[1].value)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun fieldIdentifier() {
        val tokens = lexQuotedString("\"abc\$defg.\$hij + \$kl\"").readAllTokens()
        assertEquals(8, tokens.size)

        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("abc", tokens[0].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[1].type)
        assertEquals("defg", tokens[1].value)
        assertEquals(TokenType.StringLiteral, tokens[2].type)
        assertEquals(".", tokens[2].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[3].type)
        assertEquals("hij", tokens[3].value)
        assertEquals(TokenType.StringLiteral, tokens[4].type)
        assertEquals(" + ", tokens[4].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[5].type)
        assertEquals("kl", tokens[5].value)

        assertEquals(TokenType.Symbol, tokens[6].type)
        assertEquals("\"", tokens[6].value)
        assertEquals(TokenType.EOF, tokens[7].type)
    }

    @Test
    fun fieldIdentifierMixEscapedDollarSign() {
        val tokens = lexQuotedString("\"abc\$defg.\\\$hij + \$kl\"").readAllTokens()
        assertEquals(6, tokens.size)

        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("abc", tokens[0].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[1].type)
        assertEquals("defg", tokens[1].value)
        assertEquals(TokenType.StringLiteral, tokens[2].type)
        assertEquals(".\$hij + ", tokens[2].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[3].type)
        assertEquals("kl", tokens[3].value)

        assertEquals(TokenType.Symbol, tokens[4].type)
        assertEquals("\"", tokens[4].value)
        assertEquals(TokenType.EOF, tokens[5].type)
    }

    @Test
    fun fieldIdentifierTerminatedByStringInterpolation() {
        val tokens = lexQuotedString("\"abc\$defg.\\\$hij + \$kl\${").readAllTokens()
        assertEquals(6, tokens.size)

        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("abc", tokens[0].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[1].type)
        assertEquals("defg", tokens[1].value)
        assertEquals(TokenType.StringLiteral, tokens[2].type)
        assertEquals(".\$hij + ", tokens[2].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[3].type)
        assertEquals("kl", tokens[3].value)

        assertEquals(TokenType.Symbol, tokens[4].type)
        assertEquals("\${", tokens[4].value)
        assertEquals(TokenType.EOF, tokens[5].type)
    }

    @Test
    fun fieldIdentifierWithEscapedStringInterpolation() {
        val tokens = lexQuotedString("\"abc\$defg.\\\$hij + \$kl\\\${\"").readAllTokens()
        assertEquals(7, tokens.size)

        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("abc", tokens[0].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[1].type)
        assertEquals("defg", tokens[1].value)
        assertEquals(TokenType.StringLiteral, tokens[2].type)
        assertEquals(".\$hij + ", tokens[2].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[3].type)
        assertEquals("kl", tokens[3].value)
        assertEquals(TokenType.StringLiteral, tokens[4].type)
        assertEquals("\${", tokens[4].value)

        assertEquals(TokenType.Symbol, tokens[5].type)
        assertEquals("\"", tokens[5].value)
        assertEquals(TokenType.EOF, tokens[6].type)
    }

    @Test
    fun unescapedDollarSignAsString() {
        val tokens = lexQuotedString("\"abc\$defg.\\\$hij + \$kl\$\"").readAllTokens()
        assertEquals(7, tokens.size)

        assertEquals(TokenType.StringLiteral, tokens[0].type)
        assertEquals("abc", tokens[0].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[1].type)
        assertEquals("defg", tokens[1].value)
        assertEquals(TokenType.StringLiteral, tokens[2].type)
        assertEquals(".\$hij + ", tokens[2].value)
        assertEquals(TokenType.StringFieldIdentifier, tokens[3].type)
        assertEquals("kl", tokens[3].value)
        assertEquals(TokenType.StringLiteral, tokens[4].type)
        assertEquals("\$", tokens[4].value)

        assertEquals(TokenType.Symbol, tokens[5].type)
        assertEquals("\"", tokens[5].value)
        assertEquals(TokenType.EOF, tokens[6].type)
    }

}
