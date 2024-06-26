package com.sunnychung.lib.multiplatform.kotlite.lexer

import com.sunnychung.lib.multiplatform.kotlite.extension.compareString
import com.sunnychung.lib.multiplatform.kotlite.extension.removeAfterIndex
import com.sunnychung.lib.multiplatform.kotlite.log
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.Token
import com.sunnychung.lib.multiplatform.kotlite.model.TokenType

private val NON_IDENTIFIER_CHARACTERS = setOf('+', '-', '*', '/', '%', '(', ')', '=', ',', ':', ';', '{', '}', '[', ']', '<', '>', '!', '|', '&', '.', '?', '"', '@', '\n', '\\')

object BuiltinFilename {
    val BUILTIN = "<Built-in>"
    val GLOBAL = "<Global>"
}

open class Lexer(val filename: String, val code: String, val isParseComment: Boolean = false) {
    private var pos: Int = 0
    private var lineNum = 1
    private var col = 1
    private var locationHistory = mutableListOf(SourcePosition(filename, 1, 1, 0)) // TODO optimize
    val mode = mutableListOf(Mode.Main)

    fun switchToMode(mode: Mode) {
        this.mode += mode
    }

    fun switchToPreviousMode() {
        this.mode.removeLast()
    }

    fun currentMode() = mode.last()

    fun currentCursor() = pos

    internal inline fun currentChar() = if (pos < code.length) code[pos] else null

    internal inline fun nextChar(howMany: Int = 1) = if (pos + howMany < code.length) code[pos + howMany] else null

    internal fun advanceChar(): Char? {
        if (pos < code.length) {
            if (currentChar() == '\n') {
                ++lineNum
                col = 1
            } else {
                ++col
            }
            ++pos
            locationHistory += SourcePosition(filename, lineNum, col, pos)
        }
        return if (pos < code.length) code[pos] else null
    }

    internal fun backward() {
        if (pos > 0) {
            --pos
            lineNum = locationHistory[pos].lineNum
            col = locationHistory[pos].col
            locationHistory.removeLast()
        }
    }

    fun resetCursorTo(index: Int) {
        log.v { "lexer reset to $index" }
        pos = index
        locationHistory.removeAfterIndex(pos)
        lineNum = locationHistory[pos].lineNum
        col = locationHistory[pos].col
    }

    internal fun makeSourcePosition() = SourcePosition(filename = filename, lineNum = lineNum, col = col, index = pos)

    internal fun makeNextCharSourcePosition(): SourcePosition {
        advanceChar()
        return try {
            makeSourcePosition()
        } finally {
            backward()
        }
    }

    internal fun Char.isIdentifierChar() = !isWhitespace() && this !in NON_IDENTIFIER_CHARACTERS

    internal fun Char.isFieldIdentifierChar() = isIdentifierChar() && this !in setOf('$')

    internal fun readInteger(): String {
        val sb = StringBuilder()
        while (currentChar()?.isDigit() == true) {
            sb.append(currentChar()!!)
            advanceChar() // TODO better structure
        }
        if (currentChar() == 'L') {
            sb.append(currentChar()!!)
            advanceChar()
        }
        return sb.toString()
    }

    internal fun readIdentifier(): String {
        val sb = StringBuilder()
        while (currentChar()?.isIdentifierChar() == true) {
            sb.append(currentChar()!!)
            advanceChar()
        }
        if (sb.toString() == "as" && currentChar() == '?') {
            sb.append(currentChar()!!)
            advanceChar()
        }
        backward()
        return sb.toString()
    }

    internal fun readFieldIdentifier(): String {
        val sb = StringBuilder()
        while (currentChar()?.isFieldIdentifierChar() == true) {
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
        sb.append("*/")
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
            if (!hasDot || !nextChar()!!.isDigit()) { // is an integral number
                if (number.removeSuffix("L").length > 19) throw RuntimeException("Number `$number` is too big.")
                if (number.endsWith("L") || compareString(number, "2147483647") > 0) { // FIXME -2147483648 should not be casted to Long
                    val value = number.removeSuffix("L").toLongOrNull() ?: throw RuntimeException("Long `$number` is invalid.")
                    return Token(TokenType.Long, value, position, makeSourcePosition())
                } else {
                    val value = number.toIntOrNull() ?: throw RuntimeException("Integer `$number` is invalid.")
                    return Token(TokenType.Integer, value, position, makeSourcePosition())
                }
            }
            advanceChar() // eat the '.'
            val decimal = readInteger()
            val value = "$number.$decimal".toDoubleOrNull() ?: throw RuntimeException("Double `$number` is invalid.")
            return Token(TokenType.Double, value, position, makeSourcePosition())
        } finally {
            backward()
        }
    }

    fun readChar(isDecodeSurrogatePair: Boolean): String {
        val read = if (currentChar() == '\\') {
            advanceChar()
            when (currentChar()) {
                't' -> '\t'
                'b' -> '\b'
                'r' -> '\r'
                'n' -> '\n'
                '\'' -> '\''
                '"' -> '"'
                '\\' -> '\\'
                '$' -> '$'
                'u' -> {
                    val hex = (1..4).map { advanceChar() }.joinToString("")
                    var code = hex.toInt(16)
                    if (isDecodeSurrogatePair && code in (0xD800 .. 0xDFFF)) { // surrogates
                        if (advanceChar() != '\\' || advanceChar() != 'u') throw RuntimeException("Expect another \\u for a surrogate pair")
                        val lowSurrogateHex = (1..4).map { advanceChar() }.joinToString("")
                        val lowSurrogateCode = lowSurrogateHex.toInt(16)
                        // decode according to https://en.wikipedia.org/wiki/UTF-16#Examples
                        code = ((code - 0xD800) * 0x400) + (lowSurrogateCode - 0xDC00) + 0x10000
                    }
                    hexToUtf8String(code)
                }
                else -> throw RuntimeException("Unsupported escape \\${currentChar()}")
            }
        } else {
            currentChar()
        }
        return read.toString()
    }

    internal fun readStringContent(): Token {
        val sb = StringBuilder()
        val position = makeSourcePosition()
        while (currentChar() !in setOf('"', '$', null)) {
            sb.append(readChar(isDecodeSurrogatePair = true))
            advanceChar()
        }
        try {
            return Token(TokenType.StringLiteral, sb.toString(), position, makeSourcePosition())
        } finally {
            backward()
        }
    }

    internal fun readMultilineStringContent(): Token {
        val sb = StringBuilder()
        val position = makeSourcePosition()
        while (currentChar() !in setOf('"', '$', null)) {
            sb.append(currentChar())
            advanceChar()
        }
        try {
            return Token(TokenType.StringLiteral, sb.toString(), position, makeSourcePosition())
        } finally {
            backward()
        }
    }

    fun readToken(): Token {
        while (currentChar() != null) {
            val c = currentChar()!!
            try {
                when (mode.last()) {
                    Mode.Main -> when {
                        c in setOf('\n') -> return Token(TokenType.NewLine, c.toString(), makeSourcePosition(), makeNextCharSourcePosition())
                        c.isWhitespace() -> continue
                        c.isDigit() -> return readNumber()
                        c in setOf('(', ')', '[', ']') -> return Token(TokenType.Operator, c.toString(), makeSourcePosition(), makeNextCharSourcePosition())
                        c in setOf('+', '-', '*', '/', '%') -> {
                            val position = makeSourcePosition()
                            if (nextChar() == '=') {
                                advanceChar()
                                return Token(TokenType.Symbol, "$c=", position, makeNextCharSourcePosition())
                            }
                            when (val withNextChar = "$c${nextChar()}") {
                                "++", "--" -> {
                                    advanceChar()
                                    return Token(TokenType.Operator, withNextChar, position, makeNextCharSourcePosition())
                                }

                                "->" -> {
                                    advanceChar()
                                    return Token(TokenType.Symbol, withNextChar, position, makeNextCharSourcePosition())
                                }

                                "//" -> {
//                                    advanceChar()
                                    val s = readLine()
                                    if (isParseComment) {
                                        return Token(TokenType.Comment, s, position, makeNextCharSourcePosition())
                                    }
                                    continue // discard
                                }

                                "/*" -> {
//                                    advanceChar()
                                    val s = readBlockComment()
                                    if (isParseComment) {
                                        return Token(TokenType.Comment, s, position, makeNextCharSourcePosition())
                                    }
                                    continue // discard
                                }
                            }
                            return Token(TokenType.Operator, c.toString(), position, makeNextCharSourcePosition())
                        }

                        c in setOf('|', '&') -> {
                            val position = makeSourcePosition()
                            when (val withNextChar = "$c${nextChar()}") {
                                "||", "&&" -> {
                                    advanceChar()
                                    return Token(TokenType.Operator, withNextChar, position, makeNextCharSourcePosition())
                                }
                            }
//                        return Token(TokenType.Operator, c.toString(), position)
                            throw UnsupportedOperationException("Operator `$c` is not supported")
                        }

                        c in setOf('.', '?') -> {
                            val position = makeSourcePosition()
                            when (val withNextChar = "$c${nextChar()}") {
                                "?.", "?:" -> {
                                    advanceChar()
                                    return Token(TokenType.Operator, withNextChar, position, makeNextCharSourcePosition())
                                }
                                ".." -> {
                                    var op = withNextChar
                                    advanceChar()
                                    if (nextChar() == '<') {
                                        advanceChar()
                                        op += "<"
                                    }
                                    return Token(TokenType.Operator, op, position, makeNextCharSourcePosition())
                                }
                            }
                            if (c == '.') {
                                return Token(TokenType.Operator, c.toString(), position, makeNextCharSourcePosition())
                            }
                            return Token(TokenType.Symbol, c.toString(), position, makeNextCharSourcePosition())
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
                                return Token(TokenType.Symbol, token, position, makeNextCharSourcePosition())
                            } else {
                                return Token(TokenType.Operator, token, position, makeNextCharSourcePosition())
                            }
                        }

                        c == '\'' -> {
                            val position = makeSourcePosition()
                            advanceChar()
                            val char = readChar(isDecodeSurrogatePair = false).first()
                            if (advanceChar() != '\'') {
                                throw RuntimeException("Invalid char literal")
                            }
                            return Token(TokenType.Char, char, position, makeSourcePosition())
                        }

                        c == '"' -> {
                            val position = makeSourcePosition()
                            return if (nextChar() == '"' && nextChar(howMany = 2) == '"') {
                                advanceChar()
                                advanceChar()
                                Token(TokenType.Symbol, "\"\"\"", position, makeNextCharSourcePosition())
                            } else {
                                Token(TokenType.Symbol, "$c", position, makeNextCharSourcePosition())
                            }
                        }

                        c in setOf(':', ',', '{', '}', '@') -> return Token(
                            TokenType.Symbol,
                            c.toString(),
                            makeSourcePosition(),
                            makeNextCharSourcePosition(),
                        )

                        c in setOf(';') -> return Token(TokenType.Semicolon, c.toString(), makeSourcePosition(), makeNextCharSourcePosition())
                        c.isIdentifierChar() -> {
                            val position = makeSourcePosition()
                            val identifier = readIdentifier()
                            return Token(
                                TokenType.Identifier,
                                identifier,
                                position,
                                makeNextCharSourcePosition(),
                            )
                        }
                    }

                    Mode.QuotedString -> return when {
                        c == '$' && nextChar() == '{' -> {
                            val position = makeSourcePosition()
                            advanceChar()
                            Token(TokenType.Symbol, "\${", position, makeNextCharSourcePosition())
                        }
                        c == '$' -> {
                            val position = makeSourcePosition()
                            if (advanceChar() != '"') {
                                val identifier = readFieldIdentifier()
                                Token(
                                    TokenType.StringFieldIdentifier,
                                    identifier,
                                    position,
                                    makeNextCharSourcePosition(),
                                )
                            } else {
                                backward()
                                Token(TokenType.StringLiteral, "$", position, makeNextCharSourcePosition())
                            }
                        }
                        c == '"' -> Token(TokenType.Symbol, "$c", makeSourcePosition(), makeNextCharSourcePosition())
                        else -> readStringContent()
                    }

                    Mode.MultilineString -> return when {
                        c == '$' && nextChar() == '{' -> {
                            val position = makeSourcePosition()
                            advanceChar()
                            Token(TokenType.Symbol, "\${", position, makeNextCharSourcePosition())
                        }
                        c == '$' -> {
                            val position = makeSourcePosition()
                            if (advanceChar() != '"') {
                                val identifier = readFieldIdentifier()
                                Token(
                                    TokenType.StringFieldIdentifier,
                                    identifier,
                                    position,
                                    makeNextCharSourcePosition(),
                                )
                            } else {
                                backward()
                                Token(TokenType.StringLiteral, "$", position, makeNextCharSourcePosition())
                            }
                        }
                        c == '"' && nextChar() == '"' && nextChar(howMany = 2) == '"' ->
                            Token(TokenType.Symbol, "\"\"\"", makeSourcePosition(), makeSourcePosition()).let {
                                advanceChar()
                                advanceChar()
                                it.copy(endExclusive = makeNextCharSourcePosition())
                            }
                        c == '"' -> Token(TokenType.StringLiteral, "$c", makeSourcePosition(), makeNextCharSourcePosition())
                        else -> readMultilineStringContent()
                    }
                }
            } finally {
                advanceChar()
            }
        }
        return Token(TokenType.EOF, '\u0000', makeSourcePosition(), makeSourcePosition())
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

    enum class Mode {
        Main, QuotedString, MultilineString
    }
}

@OptIn(ExperimentalStdlibApi::class) // this opt-in is only for logging
fun hexToUtf8String(code: Int): String {
    log.v { "hexToUtf8String(${code.toHexString()})" }
    // convert to bytes according to https://en.wikipedia.org/wiki/UTF-8#Encoding
    val bytes = when {
        code <= 0x007F -> byteArrayOf(code.toByte())
        code <= 0x07FF -> byteArrayOf(
            ((code shr 6) or 0b110_00000).toByte(),
            ((code and 0b00_111111) or 0b10_000000).toByte()
        )
        code <= 0xFFFF -> byteArrayOf(
            ((code shr 12) or 0b1110_0000).toByte(),
            (((code shr 6) and 0b111111) or 0b10_000000).toByte(),
            ((code and 0b111111) or 0b10_000000).toByte(),
        )
        code <= 0x10FFFF -> byteArrayOf(
            ((code shr 18) or 0b11110_000).toByte(),
            (((code shr 12) and 0b111111) or 0b10_000000).toByte(),
            (((code shr 6) and 0b111111) or 0b10_000000).toByte(),
            ((code and 0b111111) or 0b10_000000).toByte(),
        )
        else -> throw RuntimeException("Unsupported unicode character \\u${code.toHexString()}")
    }
    return bytes.decodeToString()
}
