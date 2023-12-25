package com.sunnychung.lib.multiplatform.kotlite.lexer

import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.Token
import com.sunnychung.lib.multiplatform.kotlite.model.TokenType

class Lexer(val code: String) {
    var pos: Int = 0
    var lineNum = 1
    var col = 1
    var locationHistory = mutableListOf<SourcePosition>() // TODO optimize

    inline fun currentChar() = if (pos < code.length) code[pos] else null

    inline fun nextChar() = if (pos + 1 < code.length) code[pos + 1] else null

    fun advanceChar() {
        if (pos < code.length) {
            if (currentChar() == '\n') {
                ++lineNum
                col = 1
            } else {
                ++col
            }
            if (pos >= locationHistory.size) {
                locationHistory += SourcePosition(lineNum, col)
            }
            ++pos
        }
    }

    fun backward() {
        if (pos > 0) {
            --pos
            lineNum = locationHistory[pos].lineNum
            col = locationHistory[pos].col
        }
    }

    fun makeSourcePosition() = SourcePosition(lineNum = lineNum, col = col)

    fun Char.isIdentifierChar() = !isWhitespace() && this !in setOf('+', '-', '*', '/', '%', '(', ')', '=', ',', ':', ';', '{', '}', '<', '>', '!', '\n')

    fun readInteger(): Int {
        val sb = StringBuilder()
        while (currentChar()?.isDigit() == true) {
            sb.append(currentChar()!!)
            // TODO validate

            if (nextChar()?.isDigit() != true) break
            advanceChar() // TODO better structure
        }
        if (sb.length > 10) throw RuntimeException("Integer `$sb` is too big.")
        return sb.toString().toIntOrNull() ?: throw RuntimeException("Integer `$sb` is invalid.")
    }

    fun readIdentifier(): String {
        val sb = StringBuilder()
        while (currentChar()?.isIdentifierChar() == true) {
            sb.append(currentChar()!!)
            advanceChar()
        }
        backward()
        return sb.toString()
    }

    fun readToken(): Token {
        while (currentChar() != null) {
            val c = currentChar()!!
            try {
                when {
                    c in setOf('\n') -> return Token(TokenType.NewLine, c.toString(), makeSourcePosition())
                    c.isWhitespace() -> continue
                    c.isDigit() -> return Token(TokenType.Integer, readInteger(), makeSourcePosition())
                    c in setOf('(', ')') -> return Token(TokenType.Operator, c.toString(), makeSourcePosition())
                    c in setOf('+', '-', '*', '/', '%') -> {
                        val position = makeSourcePosition()
                        if (nextChar() == '=') {
                            advanceChar()
                            return Token(TokenType.Symbol, "$c=", position)
                        }
                        val withNextChar = "$c${nextChar()}"
                        if (withNextChar in setOf("++", "--")) {
                            advanceChar()
                            return Token(TokenType.Operator, withNextChar, position)
                        }
                        return Token(TokenType.Operator, c.toString(), position)
                    }
                    c in setOf('<', '>', '=', '!') -> {
                        val position = makeSourcePosition()
                        val token = if (nextChar() == '=') {
                            advanceChar()
                            "$c="
                        } else {
                            "$c"
                        }
                        if (token == "=") {
                            return Token(TokenType.Symbol, token, position)
                        } else {
                            return Token(TokenType.Operator, token, position)
                        }
                    }
                    c in setOf(':', ',', '{', '}') -> return Token(TokenType.Symbol, c.toString(), makeSourcePosition())
                    c in setOf(';') -> return Token(TokenType.Semicolon, c.toString(), makeSourcePosition())
                    c.isIdentifierChar() -> return Token(TokenType.Identifier, readIdentifier(), makeSourcePosition())
                }
            } finally {
                advanceChar()
            }
        }
        return Token(TokenType.EOF, '\u0000', makeSourcePosition())
    }

    /**
     * For testing use only
     */
    fun readAllTokens(): List<Token> {
        val result = mutableListOf<Token>()
        do {
            val t = readToken()
            result += t
        } while (t.type != TokenType.EOF)
        return result
    }
}
