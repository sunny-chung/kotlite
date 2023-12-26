package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.error.ExpectTokenMismatchException
import com.sunnychung.lib.multiplatform.kotlite.error.UnexpectedTokenException
import com.sunnychung.lib.multiplatform.kotlite.lexer.Lexer
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.ScopeType
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.Token
import com.sunnychung.lib.multiplatform.kotlite.model.TokenType
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode

/**
 * Reference grammar: https://kotlinlang.org/spec/syntax-and-grammar.html#grammar-rule-expression
 */
class Parser(lexer: Lexer) {
    private val allTokens = lexer.readAllTokens()

    private var currentToken: Token = allTokens.first()
    private var tokenIndex = 0

    init {
        log.v { "Tokens = $allTokens" }
    }

    private fun readToken(): Token {
        if (tokenIndex + 1 < allTokens.size) {
            currentToken = allTokens[++tokenIndex]
            return currentToken
        } else {
            throw IndexOutOfBoundsException()
        }
    }

    private fun peekNextToken() = allTokens[tokenIndex + 1]

    private fun lastToken() = allTokens[tokenIndex - 1]

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

    fun isCurrentTokenExcludingNL(type: TokenType, value: Any): Boolean {
        // TODO optimize
        for (i in tokenIndex..< allTokens.size) {
            if (allTokens[i].type == TokenType.NewLine) continue
            return allTokens[i].type == type && allTokens[i].value == value
        }
        return false
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
     * postfixUnarySuffix:
     *     postfixUnaryOperator
     *     | typeArguments
     *     | callSuffix
     *     | indexingSuffix
     *     | navigationSuffix
     */
    fun postfixUnarySuffix(subject: ASTNode): ASTNode {
        when (currentToken.value) { // TODO complete
            "(" -> return callSuffix(subject)
            "++" -> { eat(TokenType.Operator, "++"); return UnaryOpNode(subject, "post++") }
            "--" -> { eat(TokenType.Operator, "--"); return UnaryOpNode(subject, "post--") }
        }
        return subject
    }

    /**
     * callSuffix:
     *     [typeArguments] (([valueArguments] annotatedLambda) | valueArguments)
     */
    fun callSuffix(subject: ASTNode): FunctionCallNode {
        val position = currentToken.position
        val arguments = valueArguments()
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
            TokenType.Identifier -> {
                when (currentToken.value) {
                    "return", "break", "continue" -> return jumpExpression()
                    "if" -> return ifExpression()

                    // literal
                    "true" -> { eat(TokenType.Identifier); return BooleanNode(true) }
                    "false" -> { eat(TokenType.Identifier); return BooleanNode(false) }
                }

                val t = eat(TokenType.Identifier)
                return VariableReferenceNode(t.value as String)
            }
            // TODO other token types
            else -> Unit
        }
        throw UnexpectedTokenException(currentToken)
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
        return equality() // FIXME
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
     * type:
     *     [typeModifiers] (functionType | parenthesizedType | nullableType | typeReference | definitelyNonNullableType)
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
    fun type(): TypeNode {
        val name = eat(TokenType.Identifier).value as String
        val argument = if (currentToken.type == TokenType.Symbol && currentToken.value == "<") {
            eat(TokenType.Symbol, "<")
            repeatedNL()
            // only deal with 1-depth argument atm
            val argument = type()
            repeatedNL()
            eat(TokenType.Symbol, ">")
            argument
        } else null
        return TypeNode(name, argument)
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
     * variableDeclaration:
     *     {annotation} {NL} simpleIdentifier [{NL} ':' {NL} type]
     */
    fun propertyDeclaration(): ASTNode {
        eat(TokenType.Identifier).also {
            if (it.value !in setOf("val", "var")) throw UnexpectedTokenException(it)
        }
        val name = userDefinedIdentifier()
        repeatedNL()
        eat(TokenType.Symbol, ":")
        repeatedNL()
        val type = type()
        val initialValue = if (currentToken.type == TokenType.Symbol && currentToken.value == "=") {
            eat(TokenType.Symbol, "=")
            expression()
        } else {
            null
        }
//        repeatedNL() // this would cause the NL before the next statement not recognized
        // TODO getter & setter
        return PropertyDeclarationNode(name = name, type = type, initialValue = initialValue)
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
     * functionValueParameter:
     *     [parameterModifiers] parameter [{NL} '=' {NL} expression]
     *
     * parameter:
     *     simpleIdentifier
     *     {NL}
     *     ':'
     *     {NL}
     *     type
     *
     */
    fun functionValueParameter(): FunctionValueParameterNode {
        val name = userDefinedIdentifier()
        repeatedNL()
        eat(TokenType.Symbol, ":")
        repeatedNL()
        val type = type()
        repeatedNL()
        val defaultValue = if (currentToken.type == TokenType.Symbol && currentToken.value == "=") {
            eat(TokenType.Symbol, "=")
            repeatedNL()
            expression()
        } else null
        return FunctionValueParameterNode(name = name, type = type, defaultValue = defaultValue)
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
     *
     *
     */
    fun functionDeclaration(): FunctionDeclarationNode {
        eat(TokenType.Identifier).also {
            if (it.value !in setOf("fun")) throw UnexpectedTokenException(it)
        }
        repeatedNL()
        val name = userDefinedIdentifier()
        repeatedNL()
        val valueParameters = functionValueParameters()
        repeatedNL()
        val type = if (isCurrentToken(type = TokenType.Symbol, value = ":")) {
            eat(TokenType.Symbol, ":")
            repeatedNL()
            val type = type()
            repeatedNL()
            type
        } else {
            TypeNode("Unit", null)
        }
        // TODO make functionBody optional for interfaces
        val body = functionBody()
        return FunctionDeclarationNode(name = name, type = type, valueParameters = valueParameters, body = body)
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
        val name = userDefinedIdentifier()
        val operator = eat(TokenType.Symbol).also {
            if (it.value !in setOf("=", "+=", "-=", "*=", "/=", "%=")) {
                throw UnexpectedTokenException(it)
            }
        }.value as String
        repeatedNL()
        val expr = expression()
        return AssignmentNode(variableName = name, operator = operator, value = expr)
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
                "val", "var", "fun" -> return declaration()
                "for", "while", "do" -> return loopStatement()
            }
            if (peekNextToken().type == TokenType.Symbol && peekNextToken().value in setOf("=", "+=", "-=", "*=", "/=", "%=")) {
                return assignment()
            }
        }

        return expression()
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


}