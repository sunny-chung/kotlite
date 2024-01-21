package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AsOpNode
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
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.LambdaLiteralNode
import com.sunnychung.lib.multiplatform.kotlite.model.LongNode
import com.sunnychung.lib.multiplatform.kotlite.model.NavigationNode
import com.sunnychung.lib.multiplatform.kotlite.model.NullNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyAccessorsNode
import com.sunnychung.lib.multiplatform.kotlite.model.PropertyDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ReturnNode
import com.sunnychung.lib.multiplatform.kotlite.model.ScriptNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringFieldIdentifierNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringLiteralNode
import com.sunnychung.lib.multiplatform.kotlite.model.StringNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.ValueNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhileNode

open class CodeGenerator(protected val node: ASTNode, val isPrintDebugInfo: Boolean) {
    var indentLevel = 0

    fun generateCode(): String {
        return node.generate()
    }

    fun debug(s: String): String {
        return if (isPrintDebugInfo) s else ""
    }

    protected fun indent() = " ".repeat(indentLevel * 4)

    protected fun ASTNode.generate(): String
        = when (this) {
            is StringFieldIdentifierNode -> this.generate()
            is AssignmentNode -> this.generate()
            is BinaryOpNode -> this.generate()
            is BlockNode -> this.generate()
            is BooleanNode -> this.generate()
            is CharNode -> this.generate()
            is BreakNode -> this.generate()
            is ClassDeclarationNode -> this.generate()
            is ClassInstanceInitializerNode -> this.generate()
            is ClassMemberReferenceNode -> this.generate()
            is ClassParameterNode -> this.generate()
            is ClassPrimaryConstructorNode -> this.generate()
            is ContinueNode -> this.generate()
            is DoubleNode -> this.generate()
            is FunctionCallArgumentNode -> this.generate()
            is FunctionCallNode -> this.generate()
            is FunctionDeclarationNode -> this.generate()
            is FunctionValueParameterNode -> this.generate()
            is IfNode -> this.generate()
            is IntegerNode -> this.generate()
            is LongNode -> this.generate()
            is NavigationNode -> this.generate()
            is NullNode -> this.generate()
            is PropertyDeclarationNode -> this.generate()
            is ReturnNode -> this.generate()
            is ScriptNode -> this.generate()
            is FunctionTypeNode -> this.generate()
            is TypeNode -> this.generate()
            is TypeParameterNode -> this.generate()
            is UnaryOpNode -> this.generate()
            is VariableReferenceNode -> this.generate()
            is WhileNode -> this.generate()
            is PropertyAccessorsNode -> TODO()
            is ValueNode -> TODO()
            is StringLiteralNode -> this.generate()
            is StringNode -> this.generate()
            is LambdaLiteralNode -> this.generate()
            is AsOpNode -> this.generate()
        }

    protected fun AssignmentNode.generate()
        = "${subject.generate()} $operator ${value.generate()}"

    protected fun BinaryOpNode.generate()
        = "(${node1.generate()} $operator ${node2.generate()})"

    protected fun BlockNode.generate()
        = run {
            ++indentLevel
            val s = "{\n${statements.joinToString("") { "${indent()}${it.generate()}\n" }}"
            --indentLevel
            "$s${indent()}}"
        }

    protected fun BooleanNode.generate() = "$value"

    protected fun CharNode.generate() = "'${if (value in setOf('\\', '\'')) "\\$value" else "$value" }'"

    protected fun BreakNode.generate() = "break"

    protected fun ClassDeclarationNode.generate()
        = "class $name " +
            (primaryConstructor?.let { "${it.generate()} " } ?: "") + "{\n" + run {
                ++indentLevel
                val s = declarations.joinToString("") { "${indent()}${it.generate()}\n" }
                --indentLevel
                "$s${indent()}}"
            }

    protected fun ClassInstanceInitializerNode.generate()
        = "init ${block.generate()}"

    protected fun ClassMemberReferenceNode.generate() = name

    protected fun ClassParameterNode.generate() = (if (isProperty) {
        if (isMutable) "var " else "val "
    } else "") + parameter.generate()

    protected fun ClassPrimaryConstructorNode.generate()
        = "constructor(" + parameters.joinToString(", ") { it.generate() } + ")"

    protected fun ContinueNode.generate() = "continue"

    protected fun DoubleNode.generate() = "$value"

    protected fun FunctionCallArgumentNode.generate()
        = (name?.let { "$name = " } ?: "") + value.generate()

    protected fun FunctionCallNode.generate()
        = "${function.generate()}${debug("<f:$functionRefName>")}(${arguments.joinToString(", ") { it.generate() }})"

    protected fun FunctionDeclarationNode.generate()
        = "fun ${if (typeParameters.isNotEmpty()) "<${typeParameters.joinToString(", ") {it.generate()}}> " else ""}${transformedRefName ?: name}(${valueParameters.joinToString(", ") { it.generate() }}): ${returnType.generate()} ${body.generate()}"

    protected fun FunctionValueParameterNode.generate()
        = "$name<$transformedRefName>: ${type.generate()}${defaultValue?.let { " = ${it.generate()}" } ?: ""}"

    protected fun IfNode.generate()
        = "if (${condition.generate()}) ${trueBlock?.let { it.generate() } ?: ";"}${falseBlock?.let { " else ${it.generate()}" } ?: ""}"

    protected fun IntegerNode.generate() = "$value"
    protected fun LongNode.generate() = "${value}L"

    protected fun NavigationNode.generate() = "${subject.generate()}$operator${member.generate()}"

    protected fun NullNode.generate() = "null"

    protected fun PropertyDeclarationNode.generate()
        = "${if (isMutable) "var" else "val"} $name<$transformedRefName>: ${(type as ASTNode).generate()}${initialValue?.let { " = ${it.generate()}" } ?: ""}" +
            (accessors?.getter?.let { "\nget() ${it.generate()}" } ?: "" )+
            (accessors?.setter?.let { "\nset() ${it.generate()}" } ?: "")

    protected fun ReturnNode.generate()
        = "return${if (returnToLabel.isNotEmpty()) "@$returnToLabel" else ""}${value?.let { " ${it.generate()}" } ?: ""}"

    protected fun ScriptNode.generate()
        = nodes.joinToString("") { "${it.generate()}\n" }

    protected fun FunctionTypeNode.generate(): String
        = "(${parameterTypes!!.joinToString(", ") {(it as ASTNode).generate()}}) -> ${(returnType as ASTNode).generate()}"

    protected fun TypeNode.generate(): String
        = "$name${arguments?.let { "<${it.joinToString(", ") { (it as ASTNode).generate() }}>" } ?: ""}${if (isNullable) "?" else ""}"

    protected fun TypeParameterNode.generate(): String
        = "$name${typeUpperBound?.let { " : ${it.generate()}" } ?: ""}"

    protected fun UnaryOpNode.generate()
        = "$operator(${node?.let { it.generate() } ?: " "})"

    protected fun VariableReferenceNode.generate()
        = "${ownerRef?.let { "$ownerRef." } ?: ""}$variableName${debug("<$transformedRefName>")}"

    protected fun WhileNode.generate()
        = "while (${condition.generate()})${body?.let { " ${it.generate()}" } ?: ";"}"

    protected fun StringNode.generate()
        = "\"${nodes.joinToString("") {
            if (it is StringLiteralNode) {
                it.generate()
            } else {
                "\${${it.generate()}}"
            }
        }}\""

    protected fun StringLiteralNode.generate() = content

    protected fun StringFieldIdentifierNode.generate(): String = (this as VariableReferenceNode).generate()

    protected fun LambdaLiteralNode.generate()
        = debug("<p=${accessedRefs!!.properties}; f=${accessedRefs!!.functions}; c=${accessedRefs!!.classes}>") +
            "{${valueParameters.joinToString(", ") {it.generate()}}${if (valueParameters.isNotEmpty()) " ->" else ""}\n" +
            run {
                ++indentLevel
                val s = body.statements.joinToString("") { "${indent()}${it.generate()}\n" }
                --indentLevel
                s
            } +
            "${indent()}}"

    protected fun AsOpNode.generate() = "(${expression.generate()} as${if (isNullable) "?" else ""} ${type.generate()})"

}
