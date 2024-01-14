package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.ExpectTokenMismatchException
import com.sunnychung.lib.multiplatform.kotlite.error.ParseException
import com.sunnychung.lib.multiplatform.kotlite.error.UnexpectedTokenException
import com.sunnychung.lib.multiplatform.kotlite.extension.removeAfterIndex
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.CharNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstanceInitializerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassMemberReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassPrimaryConstructorNode
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaLiteralNode
import com.sunnychung.lib.multiplatform.kotlite.model.NavigationNode
import com.sunnychung.lib.multiplatform.kotlite.model.NullNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyAccessorsNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.StringFieldIdentifierNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringLiteralNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringNode
import com.sunnychung.lib.multiplatform.kotlite.model.Token
import com.sunnychung.lib.multiplatform.kotlite.model.TokenType
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode

/**
 * Reference grammar: https://kotlinlang.org/spec/syntax-and-grammar.html#grammar-rule-expression
 */
class Parser(protected val lexer: Lexer) {
    internal val allTokens = mutableListOf<Token>()
    internal val tokenCharIndexes = mutableListOf<Int>()

    internal var currentToken: Token

    /**
     * Current token index
     */
    internal var tokenIndex = 0

    init {
//        log.v { "Tokens = $allTokens" }
        tokenCharIndexes += lexer.currentCursor()
        currentToken = readToken()
        tokenIndex = 0
    }

    internal fun readToken(): Token {
        if (tokenIndex + 1 < allTokens.size) {
            return allTokens[tokenIndex + 1]
        } else if (allTokens.lastOrNull()?.type == TokenType.EOF) {
            throw IndexOutOfBoundsException()
        }
        return lexer.readToken().also {
            currentToken = it
            allTokens += it
            tokenCharIndexes += lexer.currentCursor()
            ++tokenIndex
        }
    }

    private fun peekNextToken(): Token {
        val originalTokenIndex = tokenIndex
        val t = readToken()
        resetTokenToIndex(originalTokenIndex)
        return t
    }

    private fun lastToken() = allTokens[tokenIndex - 1]

    internal fun resetTokenToIndex(index: Int) {
        tokenIndex = index
        currentToken = allTokens[index]
        lexer.resetCursorTo(tokenCharIndexes[index + 1]) // cursor pointing to start of next token
        // discard already read tokens, because they are invalid after changing mode
        allTokens.removeAfterIndex(index)
        tokenCharIndexes.removeAfterIndex(index + 1)
    }

    private fun switchMode(mode: Lexer.Mode) {
        lexer.switchToMode(mode)
    }

    private fun exitMode() {
        lexer.switchToPreviousMode()
    }

    fun eat(tokenType: TokenType): Token {
        if (currentToken.type != tokenType) throw ExpectTokenMismatchException("$tokenType", currentToken.position)
        log.v { "ate $tokenType" }
        val t = currentToken
        if (t.type != TokenType.EOF) {
            currentToken = readToken()
        }
        return t
    }

    fun eat(tokenType: TokenType, value: Any): Token {
        if (currentToken.type != tokenType || currentToken.value != value) throw ExpectTokenMismatchException("$tokenType `$value`", currentToken.position)
        log.v { "ate $tokenType $value" }
        val t = currentToken
        currentToken = readToken()
        return t
    }

    fun isCurrentToken(type: TokenType, value: Any) =
        currentToken.type == type && currentToken.value == value

    fun currentTokenExcludingNL(isResetIndex: Boolean = true): Token {
        val originalTokenIndex = tokenIndex
        var t: Token = currentToken
        while (t.type == TokenType.NewLine) {
            t = readToken()
        }
        if (isResetIndex) {
            resetTokenToIndex(originalTokenIndex)
        }
        return t
    }

    fun isCurrentTokenExcludingNL(type: TokenType, value: Any): Boolean {
        val t = currentTokenExcludingNL()
        return t.type == type && t.value == value
    }

    fun isLastToken(type: TokenType, value: Any): Boolean {
        val t = lastToken()
        return t.type == type && t.value == value
    }

    fun repeatedNL() {
        while (currentToken.type == TokenType.NewLine) {
            eat(TokenType.NewLine)
        }
    }

    fun semi() {
        if (currentToken.type == TokenType.Semicolon) {
            eat(TokenType.Semicolon)
        } else {
            eat(TokenType.NewLine)
        }
        repeatedNL()
    }

    /**
     * semis:
     *     ';' | NL {';' | NL}
     */
    fun semis() {
        if (currentToken.type == TokenType.Semicolon) {
            eat(TokenType.Semicolon)
        } else {
            eat(TokenType.NewLine)
            while (currentToken.type in setOf(TokenType.Semicolon, TokenType.NewLine)) {
                eat(currentToken.type)
            }
        }
    }

    fun isSemi(): Boolean {
        return currentToken.type in setOf(TokenType.Semicolon, TokenType.NewLine)
    }

    fun userDefinedIdentifier(): String {
        return eat(TokenType.Identifier).value as String // TODO validate not reserved words
    }

    fun parenthesizedExpression(): ASTNode {
        eat(TokenType.Operator, "(")
        val node = expression()
        eat(TokenType.Operator, ")")
        return node
    }

    /**
     * unaryPrefix:
     *     annotation
     *     | label
     *     | (prefixUnaryOperator {NL})
     */
    fun unaryPrefix(): UnaryOpNode? { // TODO complete
        if (currentToken.type == TokenType.Operator && currentToken.value in setOf("++", "--", "-", "+", "!")) {
            val t = eat(TokenType.Operator)
            return when (t.value) {
                "++", "--" -> UnaryOpNode(operator = "pre${t.value}", node = null)
                else -> UnaryOpNode(operator = t.value as String, node = null)
            }
        }
        return null
    }

    /**
     *
     * prefixUnaryExpression:
     *     {unaryPrefix} postfixUnaryExpression
     *
     */
    fun prefixUnaryExpression(): ASTNode {
        val nodes = mutableListOf<UnaryOpNode>()
        do {
            val curr = unaryPrefix()
            if (curr != null) {
                if (nodes.isNotEmpty()) {
                    nodes.last().node = curr
                }
                nodes += curr
            }
        } while (curr != null)
        val expr = postfixUnaryExpression()
        if (nodes.isNotEmpty()) {
            nodes.last().node = expr
            return nodes.first()
        } else {
            return expr
        }
    }

    /**
     * postfixUnaryExpression:
     *     primaryExpression {postfixUnarySuffix}
     */
    fun postfixUnaryExpression(): ASTNode {
        var result = primaryExpression() // TODO complete expression
        while (currentToken.type in setOf(TokenType.Operator, TokenType.Symbol)) {
            val newResult = postfixUnarySuffix(result)
            if (newResult == result) break
            result = newResult
        }
        return result
    }

    /**
     * navigationSuffix:
     *     memberAccessOperator {NL} (simpleIdentifier | parenthesizedExpression | 'class')
     *
     * memberAccessOperator:
     *     ({NL} '.')
     *     | ({NL} safeNav)
     *     | '::'
     *
     * safeNav:
     *     QUEST_NO_WS '.'
     *
     * QUEST_NO_WS:
     *     '?'
     *
     */
    fun navigationSuffix(subject: ASTNode): NavigationNode {
        repeatedNL()
        val operator = eat(TokenType.Operator)
        if (operator.value !in setOf(".", "?.")) {
            throw UnexpectedTokenException(operator)
        }
        repeatedNL()
        val memberExpression = if (isCurrentToken(TokenType.Operator, "(")) {
            /*
                Use case: https://discuss.kotlinlang.org/t/how-is-parser-able-to-process-function-calls-on-objects-like-a-foo/25579/7

                fun getMember() : Int.() -> Int = { this + 1 }

                fun main() {
                    println(42.(getMember())())
                }

                This will only be supported AFTER lambda is supported
             */
            throw UnsupportedOperationException("Lambda is not supported")
//            parenthesizedExpression()
        } else {
            ClassMemberReferenceNode(eat(TokenType.Identifier).value as String) // any better node?
        }
        return NavigationNode(subject, operator.value as String, memberExpression)
    }

    /**
     * postfixUnarySuffix:
     *     postfixUnaryOperator
     *     | typeArguments
     *     | callSuffix
     *     | indexingSuffix
     *     | navigationSuffix
     */
    fun postfixUnarySuffix(subject: ASTNode): ASTNode {
        when (val op = currentToken.value as? String) { // TODO complete
            "(", "{" -> return callSuffix(subject)
            "++", "--" -> { eat(TokenType.Operator, op); return UnaryOpNode(subject, "post$op") }
            "!" -> { // this rule prevents conflict with consecutive boolean "Not" operators
                val nextToken = peekNextToken()
                if (nextToken.type == TokenType.Operator && nextToken.value == "!") {
                    eat(TokenType.Operator, op)
                    eat(TokenType.Operator, op)
                    return UnaryOpNode(subject, "!!")
                }
            }
        }
        when (currentTokenExcludingNL().value) {
            ".", "?." -> return navigationSuffix(subject)
        }
        return subject
    }

    /**
     * callSuffix:
     *     [typeArguments] (([valueArguments] annotatedLambda) | valueArguments)
     */
    fun callSuffix(subject: ASTNode): FunctionCallNode {
        val position = currentToken.position
        val arguments = mutableListOf<FunctionCallArgumentNode>()
        if (isCurrentToken(TokenType.Operator, "(")) {
            arguments += valueArguments()
        }
        if (isCurrentToken(TokenType.Symbol, "{")) {
            val lambda = lambdaLiteral()
            arguments += FunctionCallArgumentNode(
                index = arguments.size,
                value = lambda
            )
        }
        return FunctionCallNode(
            function = subject,
            arguments = arguments,
            position = position
        )
    }

    /**
     * valueArguments:
     *     '(' {NL} [valueArgument {{NL} ',' {NL} valueArgument} [{NL} ','] {NL}] ')'
     *
     */
    fun valueArguments(): List<FunctionCallArgumentNode> {
        val arguments = mutableListOf<FunctionCallArgumentNode>()
        var isLastTokenComma = false
        eat(TokenType.Operator, "(")
        repeatedNL()
        while (!isCurrentToken(TokenType.Operator, ")")) {
            if (arguments.isNotEmpty()) {
                if (!isLastTokenComma) throw UnexpectedTokenException(currentToken)
            }
            arguments += valueArgument(index = arguments.size)
            repeatedNL()
            isLastTokenComma = false
            if (isCurrentToken(TokenType.Symbol, ",")) {
                eat(TokenType.Symbol, ",")
                repeatedNL()
                isLastTokenComma = true
            }
        }
        eat(TokenType.Operator, ")")
        return arguments
    }

    /**
     * valueArgument:
     *     [annotation]
     *     {NL}
     *     [simpleIdentifier {NL} '=' {NL}]
     *     ['*']
     *     {NL}
     *     expression
     */
    fun valueArgument(index: Int): FunctionCallArgumentNode {
        repeatedNL()
        val name = if (peekNextToken().type == TokenType.Symbol && peekNextToken().value == "=") {
            val name = userDefinedIdentifier()
            eat(TokenType.Symbol, "=")
            name
        } else null
        repeatedNL()
        val value = expression()
        return FunctionCallArgumentNode(index = index, name = name, value = value)
    }

    /**
     * assignableExpression:
     *     prefixUnaryExpression
     *     | parenthesizedAssignableExpression
     */
    fun assignableExpression(): ASTNode {
        val currentToken = currentToken
        when (currentToken.type) {
            TokenType.Operator -> {
                if (currentToken.value == "(") {
                    return parenthesizedExpression()
                }
            }
            else -> return prefixUnaryExpression()
        }
        throw UnexpectedTokenException(currentToken)
    }

    /**
     * ifExpression:
     *     'if'
     *     {NL}
     *     '('
     *     {NL}
     *     expression
     *     {NL}
     *     ')'
     *     {NL}
     *     (controlStructureBody | ([controlStructureBody] {NL} [';'] {NL} 'else' {NL} (controlStructureBody | ';')) | ';')
     *
     */
    fun ifExpression(): ASTNode {
        eat(TokenType.Identifier, "if")
        repeatedNL()
        eat(TokenType.Operator, "(")
        repeatedNL()
        val condition = expression()
        repeatedNL()
        eat(TokenType.Operator, ")")
        repeatedNL()
        val trueBlock = if (isCurrentToken(TokenType.Semicolon, ";") || isCurrentToken(TokenType.Identifier, "else")) {
            null
        } else {
            controlStructureBody(ScopeType.If)
        }
        var hasEatSemicolonAfterCondition = if (isCurrentToken(TokenType.Semicolon, ";")) {
            eat(TokenType.Semicolon, ";")
            true
        } else false
        val falseBlock = if (isCurrentTokenExcludingNL(TokenType.Identifier, "else")) {
            repeatedNL()
            eat(TokenType.Identifier, "else")
            repeatedNL()
            if (isCurrentToken(TokenType.Semicolon, ";")) {
                eat(TokenType.Semicolon, ";")
                null
            } else {
                controlStructureBody(ScopeType.If)
            }
        } else null
        if (trueBlock == null && falseBlock == null) {
            if (!hasEatSemicolonAfterCondition) throw ExpectTokenMismatchException(";", lastToken().position)
        }
        return IfNode(condition = condition, trueBlock = trueBlock, falseBlock = falseBlock)
    }

    fun stringContentOrExpression(addNode: (ASTNode) -> Unit) {
        when (currentToken.type) {
            TokenType.StringLiteral -> {
                val t = eat(TokenType.StringLiteral)
                addNode(StringLiteralNode(t.value as String))
            }
            TokenType.StringFieldIdentifier -> {
                val t = eat(TokenType.StringFieldIdentifier)
                addNode(StringFieldIdentifierNode(t.value as String))
            }
            TokenType.Symbol -> {
                if (currentToken.value == "\${") {
                    switchMode(Lexer.Mode.Main)
                    eat(TokenType.Symbol, "\${")
                    repeatedNL()
                    addNode(expression())
                    repeatedNL()
                    exitMode() // switch mode before reading next token
                    eat(TokenType.Symbol, "}")
                } else {
                    throw UnexpectedTokenException(currentToken)
                }
            }
            else -> throw UnexpectedTokenException(currentToken)
        }
    }

    /**
     *
     * lineStringLiteral:
     *     '"' {lineStringContent | lineStringExpression} '"'
     *
     * lineStringContent:
     *     LineStrText
     *     | LineStrEscapedChar
     *     | LineStrRef
     *
     * lineStringExpression:
     *     '${'
     *     {NL}
     *     expression
     *     {NL}
     *     '}'
     *
     */
    fun lineStringLiteral(): ASTNode {
        val nodes = mutableListOf<ASTNode>()
        switchMode(Lexer.Mode.QuotedString) // switch mode before reading next token
        eat(TokenType.Symbol, "\"")
        while (!isCurrentToken(TokenType.Symbol, "\"")) {
            stringContentOrExpression { nodes += it }
        }
        exitMode()
        eat(TokenType.Symbol, "\"")
        return StringNode(nodes)
    }

    /**
     * multiLineStringLiteral:
     *     '"""' {multiLineStringContent | multiLineStringExpression | '"'} TRIPLE_QUOTE_CLOSE
     *
     * multiLineStringContent:
     *     MultiLineStrText
     *     | '"'
     *     | MultiLineStrRef
     *
     * multiLineStringExpression:
     *     '${'
     *     {NL}
     *     expression
     *     {NL}
     *     '}'
     */
    fun multiLineStringLiteral(): ASTNode {
        val nodes = mutableListOf<ASTNode>()
        switchMode(Lexer.Mode.MultilineString) // switch mode before reading next token
        eat(TokenType.Symbol, "\"\"\"")
        while (!isCurrentToken(TokenType.Symbol, "\"\"\"")) {
            stringContentOrExpression { nodes += it }
        }
        exitMode()
        eat(TokenType.Symbol, "\"\"\"")
        return StringNode(nodes)
    }

    /**
     * stringLiteral:
     *     lineStringLiteral
     *     | multiLineStringLiteral
     *
     */
    fun stringLiteral(): ASTNode {
        if (currentToken.type != TokenType.Symbol) throw UnexpectedTokenException(currentToken)
        return when (currentToken.value) {
            "\"" -> lineStringLiteral()
            "\"\"\"" -> multiLineStringLiteral()
            else -> throw UnexpectedTokenException(currentToken)
        }
    }

    /**
     * lambdaLiteral:
     *     '{'
     *     {NL}
     *     [[lambdaParameters] {NL} '->' {NL}]
     *     statements
     *     {NL}
     *     '}'
     *
     * lambdaParameters:
     *     lambdaParameter {{NL} ',' {NL} lambdaParameter} [{NL} ',']
     *
     * lambdaParameter:
     *     variableDeclaration
     *     | (multiVariableDeclaration [{NL} ':' {NL} type])
     *
     */
    fun lambdaLiteral(): ASTNode {
        val parameters = mutableListOf<FunctionValueParameterNode>()

        val position = eat(TokenType.Symbol, "{").position
        repeatedNL()

        val originalIndex = tokenIndex
        try {
            var hasEatenComma = false
            while (!isCurrentTokenExcludingNL(TokenType.Symbol, "->")) {
                if (parameters.isNotEmpty() && !hasEatenComma) {
                    throw ExpectTokenMismatchException(",", currentToken.position)
                }
                val (name, type) = variableDeclaration()
                parameters += FunctionValueParameterNode(name = name, declaredType = type, defaultValue = null)
                if (isCurrentTokenExcludingNL(TokenType.Symbol, ",")) {
                    repeatedNL()
                    eat(TokenType.Symbol, ",")
                    repeatedNL()
                    hasEatenComma = true
                }
            }
            eat(TokenType.Symbol, "->")
            repeatedNL()
        } catch (_: ParseException) {
            resetTokenToIndex(originalIndex)
            parameters.clear()
        }

        val statements = statements()

        repeatedNL()
        eat(TokenType.Symbol, "}")

        return LambdaLiteralNode(parameters, BlockNode(statements, position, ScopeType.FunctionBlock))
    }

    /**
     *
     * functionLiteral:
     *     lambdaLiteral
     *     | anonymousFunction
     *
     */
    fun functionLiteral() = lambdaLiteral()

    /**
     * primaryExpression:
     *     parenthesizedExpression
     *     | simpleIdentifier
     *     | literalConstant
     *     | stringLiteral
     *     | callableReference
     *     | functionLiteral
     *     | objectLiteral
     *     | collectionLiteral
     *     | thisExpression
     *     | superExpression
     *     | ifExpression
     *     | whenExpression
     *     | tryExpression
     *     | jumpExpression
     */
    fun primaryExpression(): ASTNode {
        val currentToken = currentToken
        when (currentToken.type) {
            TokenType.Operator -> {
                if (currentToken.value == "(") {
                    return parenthesizedExpression()
                }
            }
            TokenType.Integer -> {
                eat(TokenType.Integer)
                return IntegerNode(currentToken.value as Int)
            }
            TokenType.Double -> {
                eat(TokenType.Double)
                return DoubleNode(currentToken.value as Double)
            }
            TokenType.Char -> {
                eat(TokenType.Char)
                return CharNode(currentToken.value as Char)
            }
            TokenType.Identifier -> {
                when (currentToken.value) {
                    "return", "break", "continue" -> return jumpExpression()
                    "if" -> return ifExpression()

                    // literal
                    "true" -> { eat(TokenType.Identifier); return BooleanNode(true) }
                    "false" -> { eat(TokenType.Identifier); return BooleanNode(false) }
                    "null" -> { eat(TokenType.Identifier); return NullNode }
                }

                val t = eat(TokenType.Identifier)
                return VariableReferenceNode(t.value as String)
            }
            TokenType.Symbol -> {
                when (currentToken.value) {
                    "\"", "\"\"\"" -> return stringLiteral()
                    "{" -> return functionLiteral()
                }
            }
            // TODO other token types
            else -> Unit
        }
        throw UnexpectedTokenException(currentToken)
    }

    /**
     * disjunction:
     *     conjunction {{NL} '||' {NL} conjunction}
     */
    fun disjunction(): ASTNode {
        var n = conjunction()
        while (isCurrentTokenExcludingNL(TokenType.Operator, "||")) {
            repeatedNL()
            eat(TokenType.Operator, "||")
            repeatedNL()
            val n2 = conjunction()
            n = BinaryOpNode(node1 = n, node2 = n2, operator = "||")
        }
        return n
    }

    /**
     * conjunction:
     *     equality {{NL} '&&' {NL} equality}
     */
    fun conjunction(): ASTNode {
        var n = equality()
        while (isCurrentTokenExcludingNL(TokenType.Operator, "&&")) {
            repeatedNL()
            eat(TokenType.Operator, "&&")
            repeatedNL()
            val n2 = equality()
            n = BinaryOpNode(node1 = n, node2 = n2, operator = "&&")
        }
        return n
    }

    /**
     * equality:
     *     comparison {equalityOperator {NL} comparison}
     */
    fun equality(): ASTNode {
        var n = comparison()
        while (currentToken.type == TokenType.Operator && currentToken.value in setOf("!=", "!==", "==", "===")) {
            val t = eat(TokenType.Operator)
            repeatedNL()
            val n2 = comparison()
            n = BinaryOpNode(node1 = n, node2 = n2, operator = t.value as String)
        }
        return n
    }

    /**
     * comparison:
     *     genericCallLikeComparison {comparisonOperator {NL} genericCallLikeComparison}
     *
     */
    fun comparison(): ASTNode {
        var n = additiveExpression()
        while (currentToken.type == TokenType.Operator && currentToken.value in setOf("<", ">", "<=", ">=")) {
            val t = eat(TokenType.Operator)
            repeatedNL()
            val n2 = additiveExpression()
            n = BinaryOpNode(node1 = n, node2 = n2, operator = t.value as String)
        }
        return n
    }

    /**
     *
     *
     * additiveExpression:
     *     multiplicativeExpression {additiveOperator {NL} multiplicativeExpression}
     *
     *
     */
    fun additiveExpression(): ASTNode {
        var node = multiplicativeExpression() // TODO fix order
        while (currentToken.type == TokenType.Operator && currentToken.value in setOf("+", "-")) {
            val t = currentToken
            eat(TokenType.Operator)
            node = BinaryOpNode(node, multiplicativeExpression(), t.value.toString())
        }
        return node
    }

    /**
     *
     *
     * multiplicativeExpression:
     *     asExpression {multiplicativeOperator {NL} asExpression}
     *
     *
     */
    fun multiplicativeExpression(): ASTNode {
        var node = asExpression() // TODO fix order
        while (currentToken.type == TokenType.Operator && currentToken.value in setOf("*", "/", "%")) {
            val t = currentToken
            eat(TokenType.Operator)
            node = BinaryOpNode(node, asExpression(), t.value.toString())
        }
        return node
    }

    /**
     *
     *
     * asExpression:
     *     prefixUnaryExpression {{NL} asOperator {NL} type}
     *
     *
     */
    fun asExpression(): ASTNode {
        return prefixUnaryExpression()
    }

    /**
     * expression:
     *     disjunction
     */
    fun expression(): ASTNode {
        return disjunction()
    }

    /**
     * jumpExpression:
     *     ('throw' {NL} expression)
     *     | (('return' | RETURN_AT) [expression])
     *     | 'continue'
     *     | CONTINUE_AT
     *     | 'break'
     *     | BREAK_AT
     */
    fun jumpExpression(): ASTNode {
        val t = eat(TokenType.Identifier)
        when (t.value) { // TODO support return@
            "return" -> {
                val expr = if (!isSemi()) {
                    expression()
                } else null
                return ReturnNode(value = expr, returnToLabel = "", returnToAddress = "")
            }
            "break" -> return BreakNode("", "")
            "continue" -> return ContinueNode("", "")
        }
        TODO(t.value.toString())
    }

    /**
     * block:
     *     '{'
     *     {NL}
     *     statements
     *     {NL}
     *     '}'
     */
    fun block(type: ScopeType): BlockNode {
        val position = currentToken.position
        eat(TokenType.Symbol, "{")
        repeatedNL()
        val statements = statements()
        repeatedNL()
        eat(TokenType.Symbol, "}")
        return BlockNode(statements, position, type)
    }

    /**
     * controlStructureBody
     * (used by forStatement, whileStatement, doWhileStatement, ifExpression, whenEntry)
     *   : block
     *   | statement
     *   ;
     */
    fun controlStructureBody(type: ScopeType): BlockNode {
        return if (isCurrentToken(TokenType.Symbol, "{")) {
            block(type)
        } else {
            val position = currentToken.position
            BlockNode(listOf(statement()), position, type)
        }
    }

    /**
     * nullableType:
     *     (typeReference | parenthesizedType) {NL} (quest {quest})
     *
     * quest:
     *     QUEST_NO_WS
     *     | QUEST_WS
     *
     * typeReference:
     *     userType
     *     | 'dynamic'
     *
     * userType:
     *     simpleUserType {{NL} '.' {NL} simpleUserType}
     *
     * simpleUserType:
     *     simpleIdentifier [{NL} typeArguments]
     *
     * typeArguments:
     *     '<'
     *     {NL}
     *     typeProjection
     *     {{NL} ',' {NL} typeProjection}
     *     [{NL} ',']
     *     {NL}
     *     '>'
     *
     * typeProjection:
     *     ([typeProjectionModifiers] type)
     *     | '*'
     */
    fun typeReference(): TypeNode {
        val name = eat(TokenType.Identifier).value as String
        val argument = if (currentToken.type == TokenType.Symbol && currentToken.value == "<") {
            eat(TokenType.Symbol, "<")
            repeatedNL()
            // only deal with 1-depth argument atm
            val argument = type()
            repeatedNL()
            eat(TokenType.Symbol, ">")
            listOf(argument)
        } else null
        val isNullable = if (isCurrentTokenExcludingNL(TokenType.Symbol, "?")) {
            repeatedNL()
            eat(TokenType.Symbol, "?")
            true
        } else false
        return TypeNode(name, argument, isNullable)
    }

    /**
     * functionType:
     *     [receiverType {NL} '.' {NL}]
     *     functionTypeParameters
     *     {NL}
     *     '->'
     *     {NL}
     *     type
     *
     * functionTypeParameters:
     *     '('
     *     {NL}
     *     [parameter | type]
     *     {{NL} ',' {NL} (parameter | type)}
     *     [{NL} ',']
     *     {NL}
     *     ')'
     *
     */
    fun functionType(): FunctionTypeNode {
        val typeParameters = mutableListOf<TypeNode>()
        eat(TokenType.Operator, "(")
        repeatedNL()
        var hasEatenComma = false
        while (!isCurrentTokenExcludingNL(TokenType.Operator, ")")) {
            if (typeParameters.isNotEmpty() && !hasEatenComma) {
                throw ExpectTokenMismatchException(",", currentToken.position)
            }
            typeParameters += type()
            repeatedNL()
            if (isCurrentToken(TokenType.Symbol, ",")) {
                eat(TokenType.Symbol, ",")
                repeatedNL()
                hasEatenComma = true
            }
        }
        eat(TokenType.Operator, ")")

        repeatedNL()
        eat(TokenType.Symbol, "->")
        repeatedNL()
        val returnType = type()

        return FunctionTypeNode(parameterTypes = typeParameters, returnType = returnType, isNullable = false)
    }

    /**
     * parenthesizedType:
     *     '('
     *     {NL}
     *     type
     *     {NL}
     *     ')'
     *
     * nullableType:
     *     (typeReference | parenthesizedType) {NL} (quest {quest})
     *
     */
    fun nullableParenthesizedType(): TypeNode {
        eat(TokenType.Operator, "(")
        repeatedNL()
        val type = type(isTryParenthesizedType = false)
        repeatedNL()
        eat(TokenType.Operator, ")")
        val isNullable = if (isCurrentTokenExcludingNL(TokenType.Symbol, "?")) {
            repeatedNL()
            eat(TokenType.Symbol, "?")
            true
        } else false
        return type.copy(isNullable)
    }

    /**
     * type:
     *     [typeModifiers] (functionType | parenthesizedType | nullableType | typeReference | definitelyNonNullableType)
     *
     */
    fun type(isTryParenthesizedType: Boolean = true): TypeNode {
        val originalTokenIndex = tokenIndex
        return when {
            isCurrentToken(TokenType.Operator, "(") -> try {
                functionType()
            } catch (e: ParseException) {
                if (isTryParenthesizedType) {
                    resetTokenToIndex(originalTokenIndex)
                    nullableParenthesizedType()
                } else {
                    throw e
                }
            }
            else -> typeReference()
        }
    }

    /**
     * variableDeclaration:
     *     {annotation} {NL} simpleIdentifier [{NL} ':' {NL} type]
     *
     */
    fun variableDeclaration(): Pair<String, TypeNode?> {
        val name = userDefinedIdentifier()
        val type = if (isCurrentTokenExcludingNL(TokenType.Symbol, ":")) {
            repeatedNL()
            eat(TokenType.Symbol, ":")
            repeatedNL()
            type()
        } else null
        return name to type
    }

    /**
     * propertyDeclaration:
     *     [modifiers]
     *     ('val' | 'var')
     *     [{NL} typeParameters]
     *     [{NL} receiverType {NL} '.']
     *     ({NL} (multiVariableDeclaration | variableDeclaration))
     *     [{NL} typeConstraints]
     *     [{NL} (('=' {NL} expression) | propertyDelegate)]
     *     [{NL} ';']
     *     {NL}
     *     (([getter] [{NL} [semi] setter]) | ([setter] [{NL} [semi] getter]))
     *
     *
     */
    fun propertyDeclaration(isProcessBody: Boolean = true): PropertyDeclarationNode {
        val isMutable = eat(TokenType.Identifier).let {
            when (it.value) {
                "val" -> false
                "var" -> true
                else -> throw UnexpectedTokenException(it)
            }
        }
        repeatedNL()
        val (receiver, name) = receiverTypeAndIdentifier()
        val type = if (isCurrentTokenExcludingNL(TokenType.Symbol, ":")) {
            repeatedNL()
            eat(TokenType.Symbol, ":")
            repeatedNL()
            type()
        } else null

        val initialValue = if (currentToken.type == TokenType.Symbol && currentToken.value == "=") {
            eat(TokenType.Symbol, "=")
            expression()
        } else {
            null
        }
//        repeatedNL() // this would cause the NL before the next statement not recognized

        fun nextNonNLSemiToken(): Token {
            val originalTokenIndex = tokenIndex
            var token = currentTokenExcludingNL(isResetIndex = false)
            if (token.type == TokenType.Semicolon) {
                token = readToken()
            }
            resetTokenToIndex(originalTokenIndex)
            return token
        }

        val nextToken = currentTokenExcludingNL()
        val accessors = when (nextToken.value.takeIf { nextToken.type == TokenType.Identifier }) {
            "get" -> {
                repeatedNL()
                if (type == null) { // TODO make type infer possible
                    throw RuntimeException("Type is needed if there is custom accessor")
                }
                val getter = getter(type, isProcessBody)
                val next = nextNonNLSemiToken()
                val setter = if (next.type == TokenType.Identifier && next.value == "set") {
                    repeatedNL()
                    if (isSemi()) semi()
                    setter(type, isProcessBody)
                } else null
                PropertyAccessorsNode(type, getter, setter)
            }
            "set" -> {
                repeatedNL()
                if (type == null) { // TODO make type infer possible
                    throw RuntimeException("Type is needed if there is custom accessor")
                }
                val setter = setter(type, isProcessBody)
                val next = nextNonNLSemiToken()
                val getter = if (next.type == TokenType.Identifier && next.value == "get") {
                    repeatedNL()
                    if (isSemi()) semi()
                    getter(type, isProcessBody)
                } else null
                PropertyAccessorsNode(type, getter, setter)
            }
            else -> null
        }
        return PropertyDeclarationNode(name = name, receiver = receiver, declaredType = type, isMutable = isMutable, initialValue = initialValue, accessors = accessors)
    }

    /**
     * getter:
     *     [modifiers] 'get' [{NL} '(' {NL} ')' [{NL} ':' {NL} type] {NL} functionBody]
     *
     */
    fun getter(type: TypeNode, isProcessBody: Boolean = true): FunctionDeclarationNode {
        eat(TokenType.Identifier, "get")
        repeatedNL()
        eat(TokenType.Operator, "(")
        repeatedNL()
        eat(TokenType.Operator, ")")
        repeatedNL()
        val body = if (isProcessBody) functionBody() else dummyBlockNode()
        return FunctionDeclarationNode(name = "get", returnType = type, valueParameters = emptyList(), body = body)
    }

    /**
     * setter:
     *     [modifiers] 'set' [{NL} '(' {NL} functionValueParameterWithOptionalType [{NL} ','] {NL} ')' [{NL} ':' {NL} type] {NL} functionBody]
     *
     * functionValueParameterWithOptionalType:
     *     [parameterModifiers] parameterWithOptionalType [{NL} '=' {NL} expression]
     *
     * parameterWithOptionalType:
     *     simpleIdentifier {NL} [':' {NL} type]
     *
     */
    fun setter(type: TypeNode, isProcessBody: Boolean = true): FunctionDeclarationNode {
        eat(TokenType.Identifier, "set")
        repeatedNL()
        eat(TokenType.Operator, "(")
        repeatedNL()
        val parameterName = userDefinedIdentifier()
        repeatedNL()
        eat(TokenType.Operator, ")")
        repeatedNL()
        val returnType = if (false /* Kotlin only permits Unit as return type. No point to support this syntax */
                && isCurrentToken(TokenType.Symbol, ":")) {
            eat(TokenType.Symbol, ":")
            repeatedNL()
            type().also { repeatedNL() }
        } else {
            TypeNode("Unit", null, false)
        }
        val body = if (isProcessBody) functionBody() else dummyBlockNode()
        return FunctionDeclarationNode(
            name = "set",
            returnType = returnType,
            valueParameters = listOf(
                FunctionValueParameterNode(parameterName, type, null)
            ),
            body = body
        )
    }

    /**
     * functionValueParameters:
     *     '('
     *     {NL}
     *     [functionValueParameter {{NL} ',' {NL} functionValueParameter} [{NL} ',']]
     *     {NL}
     *     ')'
     *
     *
     */
    fun functionValueParameters(): List<FunctionValueParameterNode> {
        val parameters = mutableListOf<FunctionValueParameterNode>()
        var isLastTokenComma = false
        eat(TokenType.Operator, "(")
        repeatedNL()
        while (currentToken.type == TokenType.Identifier) {
            if (parameters.isNotEmpty()) {
                if (!isLastTokenComma) {
                    throw UnexpectedTokenException(currentToken)
                }
            }
            parameters += functionValueParameter()
            isLastTokenComma = false
            repeatedNL()
            if (currentToken.type == TokenType.Symbol && currentToken.value == ",") {
                eat(TokenType.Symbol, ",")
                repeatedNL()
                isLastTokenComma = true
            }
        }
        eat(TokenType.Operator, ")")
        return parameters
    }

    /**
     * parameter:
     *     simpleIdentifier
     *     {NL}
     *     ':'
     *     {NL}
     *     type
     *
     */
    fun parameter(): Pair<String, TypeNode> {
        val name = userDefinedIdentifier()
        repeatedNL()
        eat(TokenType.Symbol, ":")
        repeatedNL()
        val type = type()
        return name to type
    }

    /**
     * functionValueParameter:
     *     [parameterModifiers] parameter [{NL} '=' {NL} expression]
     *
     */
    fun functionValueParameter(): FunctionValueParameterNode {
        val (name, type) = parameter()
        repeatedNL()
        val defaultValue = if (currentToken.type == TokenType.Symbol && currentToken.value == "=") {
            eat(TokenType.Symbol, "=")
            repeatedNL()
            expression()
        } else null
        return FunctionValueParameterNode(name = name, declaredType = type, defaultValue = defaultValue)
    }

    /**
     * functionBody:
     *     block
     *     | ('=' {NL} expression)
     */
    fun functionBody(): BlockNode {
        val position = currentToken.position
        if (currentToken.type == TokenType.Symbol && currentToken.value == "=") {
            eat(TokenType.Symbol, "=")
            repeatedNL()
            return BlockNode(listOf(expression()), position, ScopeType.Function)
        } else {
            return block(ScopeType.Function)
        }
    }

    /**
     * receiverType:
     *     [typeModifiers] (parenthesizedType | nullableType | typeReference)
     *
     * nullableType:
     *     (typeReference | parenthesizedType) {NL} (quest {quest})
     *
     * typeReference:
     *     userType
     *     | 'dynamic'
     */
    fun receiverTypeAndIdentifier(): Pair<String?, String> {
        val identifiers = mutableListOf<String>()
        var hasEatenQuestionMark = false
        var numOfDotsAfterQuestionMark = 0
        do {
            if (hasEatenQuestionMark) {
                ++numOfDotsAfterQuestionMark
                if (numOfDotsAfterQuestionMark > 1) {
                    throw ExpectTokenMismatchException("(", currentToken.position)
                }
            }
            if (identifiers.isNotEmpty()) {
                if (isCurrentToken(TokenType.Operator, "?.")) {
                    eat(TokenType.Operator, "?.")
                } else {
                    eat(TokenType.Operator, ".")
                }
            }
            var identifier = userDefinedIdentifier()
            repeatedNL()
            if (isCurrentToken(TokenType.Operator, "?.")) {
                identifier += "?"
                hasEatenQuestionMark = true
            }
            identifiers += identifier
        } while (isCurrentToken(TokenType.Operator, ".") || isCurrentToken(TokenType.Operator, "?."))
        val name = identifiers.removeLast()
        val receiver = identifiers.takeIf { it.isNotEmpty() }?.joinToString(".")
        return receiver to name
    }

    /**
     *
     * functionDeclaration:
     *     [modifiers]
     *     'fun'
     *     [{NL} typeParameters]
     *     [{NL} receiverType {NL} '.']
     *     {NL}
     *     simpleIdentifier
     *     {NL}
     *     functionValueParameters
     *     [{NL} ':' {NL} type]
     *     [{NL} typeConstraints]
     *     [{NL} functionBody]
     *
     */
    fun functionDeclaration(isProcessBody: Boolean = true): FunctionDeclarationNode {
        eat(TokenType.Identifier).also {
            if (it.value !in setOf("fun")) throw UnexpectedTokenException(it)
        }
        repeatedNL()
        val (receiver, name) = receiverTypeAndIdentifier()
        val valueParameters = functionValueParameters()
        repeatedNL()
        val type = if (isCurrentToken(type = TokenType.Symbol, value = ":")) {
            eat(TokenType.Symbol, ":")
            repeatedNL()
            val type = type()
            repeatedNL()
            type
        } else {
            TypeNode("Unit", null, false)
        }
        // TODO make functionBody optional for interfaces
        if (!isProcessBody) {
            return FunctionDeclarationNode(
                name = name,
                receiver = receiver,
                returnType = type,
                valueParameters = valueParameters,
                body = dummyBlockNode()
            )
        }
        val body = functionBody()
        return FunctionDeclarationNode(name = name, receiver = receiver, returnType = type, valueParameters = valueParameters, body = body)
    }

    fun dummyBlockNode() = BlockNode(emptyList(), SourcePosition(1, 1), ScopeType.Function)

    /**
     * classParameter:
     *     [modifiers]
     *     ['val' | 'var']
     *     {NL}
     *     simpleIdentifier
     *     ':'
     *     {NL}
     *     type
     *     [{NL} '=' {NL} expression]
     */
    fun classParameter(): ClassParameterNode {
        val isMutable = if (currentToken.type == TokenType.Identifier && currentToken.value in setOf("val", "var")) {
            (currentToken.value == "var").also { eat(TokenType.Identifier) }
        } else null
        repeatedNL()
        val name = userDefinedIdentifier()
        eat(TokenType.Symbol, ":")
        repeatedNL()
        val type = type()
        val defaultValue = if (isCurrentTokenExcludingNL(TokenType.Symbol, "=")) {
            repeatedNL()
            eat(TokenType.Symbol, "=")
            repeatedNL()
            expression()
        } else null
        return ClassParameterNode(
            isProperty = isMutable != null,
            isMutable = isMutable == true,
            parameter = FunctionValueParameterNode(
                name = name,
                declaredType = type,
                defaultValue = defaultValue
            )
        )
    }

    /**
     * primaryConstructor:
     *     [[modifiers] 'constructor' {NL}] classParameters
     *
     * classParameters:
     *     '('
     *     {NL}
     *     [classParameter {{NL} ',' {NL} classParameter} [{NL} ',']]
     *     {NL}
     *     ')'
     */
    fun primaryConstructor() : ClassPrimaryConstructorNode {
        val parameters = mutableListOf<ClassParameterNode>()
        if (isCurrentToken(TokenType.Identifier, "constructor")) {
            eat(TokenType.Identifier, "constructor")
            repeatedNL()
        }
        eat(TokenType.Operator, "(")
        repeatedNL()
        var hasEatenComma = false
        while (!isCurrentTokenExcludingNL(TokenType.Operator, ")")) {
            if (parameters.isNotEmpty()) {
                if (!hasEatenComma) {
                    throw ExpectTokenMismatchException(",", currentToken.position)
                }
            }
            parameters += classParameter()
            repeatedNL()
            hasEatenComma = false
            if (isCurrentToken(TokenType.Symbol, ",")) {
                eat(TokenType.Symbol, ",")
                repeatedNL()
                hasEatenComma = true
            }
        }
        repeatedNL()
        eat(TokenType.Operator, ")")
        return ClassPrimaryConstructorNode(parameters)
    }

    /**
     * classMemberDeclarations:
     *     {classMemberDeclaration [semis]}
     *
     * classMemberDeclaration:
     *     declaration
     *     | companionObject
     *     | anonymousInitializer
     *     | secondaryConstructor
     *
     * anonymousInitializer:
     *     'init' {NL} block
     */
    fun classMemberDeclarations(): List<ASTNode> {
        val declarations = mutableListOf<ASTNode>()
        while (!isCurrentTokenExcludingNL(TokenType.Symbol, "}")) {
            declarations += if (isCurrentToken(TokenType.Identifier, "init")) {
                eat(TokenType.Identifier, "init")
                repeatedNL()
                val block = block(ScopeType.Initializer)
                ClassInstanceInitializerNode(block)
            } else {
                declaration()
            }

            if (isSemi()) {
                semis()
            }
        }
        return declarations
    }

    /**
     * classBody:
     *     '{'
     *     {NL}
     *     classMemberDeclarations
     *     {NL}
     *     '}'
     */
    fun classBody(): List<ASTNode> {
        eat(TokenType.Symbol, "{")
        repeatedNL()
        val declarations = classMemberDeclarations()
        repeatedNL()
        eat(TokenType.Symbol, "}")
        return declarations
    }

    /**
     * classDeclaration:
     *     [modifiers]
     *     ('class' | (['fun' {NL}] 'interface'))
     *     {NL}
     *     simpleIdentifier
     *     [{NL} typeParameters]
     *     [{NL} primaryConstructor]
     *     [{NL} ':' {NL} delegationSpecifiers]
     *     [{NL} typeConstraints]
     *     [({NL} classBody) | ({NL} enumClassBody)]
     */
    fun classDeclaration(): ClassDeclarationNode {
        eat(TokenType.Identifier, "class")
        repeatedNL()
        val name = userDefinedIdentifier()
        var token = currentTokenExcludingNL()
        val primaryConstructor = if (
            (token.type == TokenType.Identifier && token.value == "constructor")
            || (token.type == TokenType.Operator && token.value == "(")
        ) {
            repeatedNL()
            primaryConstructor().also { token = currentTokenExcludingNL() }
        } else null
        val declarations = if (token.type == TokenType.Symbol && token.value == "{") {
            repeatedNL()
            classBody()
        } else listOf()
        return ClassDeclarationNode(name = name, primaryConstructor = primaryConstructor, declarations = declarations)
    }

    /**
     * declaration:
     *     classDeclaration
     *     | objectDeclaration
     *     | functionDeclaration
     *     | propertyDeclaration
     *     | typeAlias
     */
    fun declaration(): ASTNode {
        if (currentToken.type != TokenType.Identifier) {
//            throw ParseException("Expected an identifier but missing")
            throw UnexpectedTokenException(currentToken)
        }
        when (currentToken.value as String) {
            "val", "var" -> return propertyDeclaration()
            "fun" -> return functionDeclaration()
            "class" -> return classDeclaration()
        }
        throw UnexpectedTokenException(currentToken)
    }

    /**
     * assignment:
     *     ((directlyAssignableExpression '=') | (assignableExpression assignmentAndOperator)) {NL} expression
     *
     * assignmentAndOperator
     * (used by assignment)
     *   : '+='
     *   | '-='
     *   | '*='
     *   | '/='
     *   | '%='
     *   ;
     *
     */
    fun assignment(): ASTNode {
//        val name = userDefinedIdentifier()
        val subject = assignableExpression()
        val operator = eat(TokenType.Symbol).also {
            if (it.value !in setOf("=", "+=", "-=", "*=", "/=", "%=")) {
                throw UnexpectedTokenException(it)
            }
        }.value as String
        repeatedNL()
        val expr = expression()
        return AssignmentNode(subject = subject, operator = operator, value = expr)
    }

    /**
     * whileStatement:
     *     'while'
     *     {NL}
     *     '('
     *     expression
     *     ')'
     *     {NL}
     *     (controlStructureBody | ';')
     */
    fun whileStatement(): ASTNode {
        eat(TokenType.Identifier, "while")
        repeatedNL()
        eat(TokenType.Operator, "(")
        repeatedNL()
        val condition = expression()
        repeatedNL()
        eat(TokenType.Operator, ")")
        repeatedNL()
        val loopBody = if (isCurrentToken(TokenType.Semicolon, ";")) {
            eat(TokenType.Semicolon, ";")
            null
        } else {
            controlStructureBody(ScopeType.While)
        }
        return WhileNode(condition = condition, body = loopBody)
    }

    /**
     * loopStatement:
     *     forStatement
     *     | whileStatement
     *     | doWhileStatement
     */
    fun loopStatement(): ASTNode {
        if (currentToken.type != TokenType.Identifier) throw UnexpectedTokenException(currentToken)
        return when (currentToken.value) {
            "while" -> whileStatement()
            else -> TODO()
        }
    }

    /**
     * statement:
     *     {label | annotation} (declaration | assignment | loopStatement | expression)
     */
    fun statement(): ASTNode { // TODO complete
        if (currentToken.type == TokenType.Identifier) {
            when (currentToken.value) {
                "val", "var", "fun", "class" -> return declaration()
                "for", "while", "do" -> return loopStatement()
            }
        }
        val tokenPos = tokenIndex
        return try {
            assignment()
        } catch (_: ParseException) {
            resetTokenToIndex(tokenPos)
            expression()
        }
    }

    /**
     * statements:
     *     [statement {semis statement}] [semis]
     *
     */
    fun statements(): List<ASTNode> {
        val result = mutableListOf<ASTNode>()
        var isLastTokenSemi = false
        while (!isCurrentToken(type = TokenType.Symbol, value = "}")) {
            if (result.isNotEmpty()) {
                if (!isLastTokenSemi) {
                    throw UnexpectedTokenException(currentToken)
                }
            }
            result += statement()
            isLastTokenSemi = false
            while (currentToken.type in setOf(TokenType.Semicolon, TokenType.NewLine)) {
                semis()
                isLastTokenSemi = true
            }
        }
        return result
    }

    /**
     * script:
     *     [shebangLine]
     *     {NL}
     *     {fileAnnotation}
     *     packageHeader
     *     importList
     *     {statement semi}
     *     EOF
     */
    fun script(): ScriptNode { // TODO complete
        val nodes = mutableListOf<ASTNode>()
//        do {
//            val curr = statement()
//            if (curr != null) {
//                nodes += curr
//                semi()
//            }
//        } while (curr != null)
        while (isSemi()) {
            semis()
        }
        while (currentToken.type != TokenType.EOF) {
            nodes += statement()
            if (currentToken.type in setOf(TokenType.Semicolon, TokenType.NewLine)) {
                semi()
            }
        }
        eat(TokenType.EOF)
        return ScriptNode(nodes)
    }

    /**
     * @return list of FunctionDeclarationNode and PropertyDeclarationNode
     */
    fun libHeaderFile(): List<ASTNode> {
        val result = mutableListOf<ASTNode>()
        while (currentTokenExcludingNL().type != TokenType.EOF) {
            repeatedNL()
            if (isCurrentToken(TokenType.Identifier, "val") || isCurrentToken(TokenType.Identifier, "var")) {
                result += propertyDeclaration(isProcessBody = false)
            } else {
                result += functionDeclaration(isProcessBody = false)
            }
        }
        eat(TokenType.EOF)
        return result
    }


}
