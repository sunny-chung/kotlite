package com.sunnychung.lib.multiplatform.kotlite.lexer

import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.Token
import com.sunnychung.lib.multiplatform.kotlite.model.TokenType

class Lexer(val code: String) {
    var pos: Int = 0
    var lineNum = 1
    var col = 1
    var locationHistory = mutableListOf<SourcePosition>() // TODO optimize

    internal inline fun currentChar() = if (pos < code.length) code[pos] else null

    internal inline fun nextChar() = if (pos + 1 < code.length) code[pos + 1] else null

    internal fun advanceChar() {
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

    internal fun backward() {
        if (pos > 0) {
            --pos
            lineNum = locationHistory[pos].lineNum
            col = locationHistory[pos].col
        }
    }

    internal fun makeSourcePosition() = SourcePosition(lineNum = lineNum, col = col)

    internal fun Char.isIdentifierChar() = !isWhitespace() && this !in setOf('+', '-', '*', '/', '%', '(', ')', '=', ',', ':', ';', '{', '}', '<', '>', '!', '|', '&', '.', '?', '\n')

    internal fun readInteger(): String {
        val sb = StringBuilder()
        while (currentChar()?.isDigit() == true) {
            sb.append(currentChar()!!)
            advanceChar() // TODO better structure
        }
        return sb.toString()
    }

    internal fun readIdentifier(): String {
        val sb = StringBuilder()
        while (currentChar()?.isIdentifierChar() == true) {
            sb.append(currentChar()!!)
            advanceChar()
        }
        backward()
        return sb.toString()
    }

    internal fun readLine(): String {
        val sb = StringBuilder()
        while (currentChar() !in setOf(null, '\n')) {
            sb.append(currentChar()!!)
            advanceChar()
        }
        backward() // preserve new line token
        return sb.toString()
    }

    internal fun readBlockComment(): String {
        val sb = StringBuilder()
        while (!(currentChar() == '*' && nextChar() == '/')) {
            sb.append(currentChar()!!)
            advanceChar()
        }
        advanceChar()
        return sb.toString()
    }

    internal fun readNumber(): Token {
        try {
            val position = makeSourcePosition()
            val number = readInteger()
            val hasDot = if (currentChar() == '.') {
                true
            } else {
                // TODO throw error if not followed by whitespaces, symbols or EOF
                false
            }
            if (!hasDot || !nextChar()!!.isDigit()) { // is an integer
                if (number.length > 10) throw RuntimeException("Integer `$number` is too big.")
                val value = number.toIntOrNull() ?: throw RuntimeException("Integer `$number` is invalid.")
                return Token(TokenType.Integer, value, position)
            }
            advanceChar() // eat the '.'
            val decimal = readInteger()
            val value = "$number.$decimal".toDoubleOrNull() ?: throw RuntimeException("Double `$number` is invalid.")
            return Token(TokenType.Double, value, position)
        } finally {
            backward()
        }
    }

    internal fun readToken(): Token {
        while (currentChar() != null) {
            val c = currentChar()!!
            try {
                when {
                    c in setOf('\n') -> return Token(TokenType.NewLine, c.toString(), makeSourcePosition())
                    c.isWhitespace() -> continue
                    c.isDigit() -> return readNumber()
                    c in setOf('(', ')') -> return Token(TokenType.Operator, c.toString(), makeSourcePosition())
                    c in setOf('+', '-', '*', '/', '%') -> {
                        val position = makeSourcePosition()
                        if (nextChar() == '=') {
                            advanceChar()
                            return Token(TokenType.Symbol, "$c=", position)
                        }
                        val withNextChar = "$c${nextChar()}"
                        when (withNextChar) {
                            "++", "--" -> {
                                advanceChar()
                                return Token(TokenType.Operator, withNextChar, position)
                            }
                            "//" -> {
                                advanceChar()
                                readLine()
                                continue // discard
                            }
                            "/*" -> {
                                advanceChar()
                                readBlockComment()
                                continue // discard
                            }
                        }
                        return Token(TokenType.Operator, c.toString(), position)
                    }
                    c in setOf('|', '&') -> {
                        val position = makeSourcePosition()
                        when (val withNextChar = "$c${nextChar()}") {
                            "||", "&&" -> {
                                advanceChar()
                                return Token(TokenType.Operator, withNextChar, position)
                            }
                        }
//                        return Token(TokenType.Operator, c.toString(), position)
                        throw UnsupportedOperationException("Operator `$c` is not supported")
                    }
                    c in setOf('.', '?') -> {
                        val position = makeSourcePosition()
                        when (val withNextChar = "$c${nextChar()}") {
                            "?." -> {
                                advanceChar()
                                return Token(TokenType.Operator, withNextChar, position)
                            }
                        }
                        if (c == '.') {
                            return Token(TokenType.Operator, c.toString(), position)
                        }
                        return Token(TokenType.Symbol, c.toString(), position)
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
