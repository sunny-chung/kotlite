package com.sunnychung.lib.multiplatform.kotlite

import com.sunnychung.lib.multiplatform.kotlite.extension.emptyToNull
import com.sunnychung.lib.multiplatform.kotlite.model.ASTNode
import com.sunnychung.lib.multiplatform.kotlite.model.AsOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.AssignmentNode
import com.sunnychung.lib.multiplatform.kotlite.model.BinaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.BlockNode
import com.sunnychung.lib.multiplatform.kotlite.model.BooleanNode
import com.sunnychung.lib.multiplatform.kotlite.model.BreakNode
import com.sunnychung.lib.multiplatform.kotlite.model.CatchNode
import com.sunnychung.lib.multiplatform.kotlite.model.CharNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstanceInitializerNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassMemberReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.ClassPrimaryConstructorNode
import com.sunnychung.lib.multiplatform.kotlite.model.ContinueNode
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleNode
import com.sunnychung.lib.multiplatform.kotlite.model.ElvisOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.EnumEntryNode
import com.sunnychung.lib.multiplatform.kotlite.model.ForNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallArgumentNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionTypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionValueParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.IfNode
import com.sunnychung.lib.multiplatform.kotlite.model.IndexOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.InfixFunctionCallNode
import com.sunnychung.lib.multiplatform.kotlite.model.IntegerNode
import com.sunnychung.lib.multiplatform.kotlite.model.LabelNode
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
import com.sunnychung.lib.multiplatform.kotlite.model.ThrowNode
import com.sunnychung.lib.multiplatform.kotlite.model.TryNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeNode
import com.sunnychung.lib.multiplatform.kotlite.model.TypeParameterNode
import com.sunnychung.lib.multiplatform.kotlite.model.UnaryOpNode
import com.sunnychung.lib.multiplatform.kotlite.model.ValueNode
import com.sunnychung.lib.multiplatform.kotlite.model.ValueParameterDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.VariableReferenceNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenConditionNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenEntryNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenNode
import com.sunnychung.lib.multiplatform.kotlite.model.WhenSubjectNode
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
            is IndexOpNode -> this.generate()
            is InfixFunctionCallNode -> this.generate()
            is ElvisOpNode -> this.generate()
            is ThrowNode -> this.generate()
            is CatchNode -> this.generate()
            is TryNode -> this.generate()
            is WhenConditionNode -> this.generate()
            is WhenEntryNode -> this.generate()
            is WhenNode -> this.generate()
            is WhenSubjectNode -> this.generate()
            is LabelNode -> this.generate()
            is EnumEntryNode -> this.generate()
            is ForNode -> this.generate()
            is ValueParameterDeclarationNode -> this.generate()
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
        = "${modifiers.joinToString("") { "$it " }}class $name" +
            (typeParameters.emptyToNull()?.let { parameters -> "<${parameters.joinToString(", ") {it.generate()}}> " } ?: " ") +
            (primaryConstructor?.let { "${it.generate()} " } ?: "") +
            (superClassInvocation?.let { ": ${it.generate()} " } ?: "") +
            "{\n" + buildString {
                ++indentLevel
                if (enumEntries.isNotEmpty()) {
                    append(indent())
                    append(enumEntries.joinToString(", ") { it.generate() })
                    append(";\n")
                }
                append(declarations.joinToString("") { "${indent()}${it.generate()}\n" })
                --indentLevel
                append(indent(), "}")
            }

    protected fun ClassInstanceInitializerNode.generate()
        = "init ${block.generate()}"

    protected fun ClassMemberReferenceNode.generate() = name

    protected fun ClassParameterNode.generate() = (if (isProperty) {
        if (isMutable) "var " else "val "
    } else "") + parameter.generate() + "<$transformedRefNameInBody>"

    protected fun ClassPrimaryConstructorNode.generate()
        = "constructor(" + parameters.joinToString(", ") { it.generate() } + ")"

    protected fun ContinueNode.generate() = "continue"

    protected fun DoubleNode.generate() = "$value"

    protected fun FunctionCallArgumentNode.generate()
        = (name?.let { "$name = " } ?: "") + value.generate()

    protected fun FunctionCallNode.generate()
        = "${function.generate()}${debug("<f:$functionRefName>")}${if (typeArguments.isNotEmpty()) "<${typeArguments.joinToString(", ") { it.descriptiveName() }}>" else ""}(${arguments.joinToString(", ") { it.generate() }})"

    protected fun FunctionDeclarationNode.generate()
        = "${modifiers.joinToString("") { "$it " }}fun ${if (typeParameters.isNotEmpty()) "<${typeParameters.joinToString(", ") {it.generate()}}> " else ""}${transformedRefName ?: name}(${valueParameters.joinToString(", ") { it.generate() }}): ${returnType.generate()} ${body.generate()}"

    protected fun FunctionValueParameterNode.generate()
        = "${modifiers.joinToString("") { "$it " }}$name<$transformedRefName>: ${type.generate()}${defaultValue?.let { " = ${it.generate()}" } ?: ""}"

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
        = "${ownerRef?.let { "${it.ownerRefName}." } ?: ""}$variableName${debug("<r:$transformedRefName>")}"

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
            "${label?.generate() ?: ""}{${valueParameters.joinToString(", ") {it.generate()}}${if (valueParameters.isNotEmpty()) " ->" else ""}\n" +
            run {
                ++indentLevel
                val s = body.statements.joinToString("") { "${indent()}${it.generate()}\n" }
                --indentLevel
                s
            } +
            "${indent()}}"

    protected fun AsOpNode.generate() = "(${expression.generate()} as${if (isNullable) "?" else ""} ${type.generate()})"

    protected fun IndexOpNode.generate() = "${subject.generate()}[${arguments.joinToString(", ") { it.generate() }}]"

    protected fun InfixFunctionCallNode.generate() = "${node1.generate()} $functionName ${node2.generate()}"

    protected fun ElvisOpNode.generate() = "${primaryNode.generate()} ?: ${fallbackNode.generate()}"

    protected fun ThrowNode.generate() = "throw ${value.generate()}"

    protected fun TryNode.generate() = "try ${
        mainBlock.generate()
    }${
        catchBlocks.joinToString(prefix = " ") { it.generate() }
    }${
        finallyBlock?.let { " finally ${it.generate()}" }
    }"

    protected fun CatchNode.generate() = "catch ${block.generate()}"

    protected fun WhenSubjectNode.generate() = buildString {
        append("(")
        if (hasValueDeclaration()) {
            append("val $valueName")
            if (declaredType != null) {
                append(": ")
                append(declaredType.generate())
            }
            if (isPrintDebugInfo) {
                append("<$valueTransformedRefName>")
            }
            append(" = ")
        }
        append(value.generate())
        append(")")
    }

    protected fun WhenConditionNode.generate() = buildString {
        if (testType == WhenConditionNode.TestType.TypeTest) {
            append("is ")
        }
        append(expression.generate())
    }

    protected fun WhenEntryNode.generate() = buildString {
        if (isElseCondition()) {
            append("else")
        } else {
            append(conditions.joinToString(", ") { it.generate() })
        }
        append(" -> ")
        append(body.generate())
    }

    protected fun WhenNode.generate() = buildString {
        append("when ")
        if (subject != null) {
            append(subject.generate())
            append(" ")
        }
        append("{\n")
        ++indentLevel

        entries.forEach {
            append(indent())
            append(it.generate())
            append("\n")
        }

        --indentLevel
        append(indent())
        append("}")
    }

    protected fun LabelNode.generate() = "$label@ "

    protected fun EnumEntryNode.generate() = buildString {
        append(name)
        if (arguments.isNotEmpty()) {
            append("(")
            append(arguments.joinToString(", ") { it.generate() })
            append(")")
        }
    }

    protected fun ForNode.generate() = buildString {
        append("for (")
        if (variables.size == 1) {
            append(variables.single().generate())
        } else {
            append("(")
            append(variables.joinToString(", ") { it.generate() })
            append(")")
        }
        append(" in ${subject.generate()}) ${body.generate()}")
    }

    protected fun ValueParameterDeclarationNode.generate() = buildString {
        append(name)
        if (declaredType != null) {
            append(": ${declaredType.generate()}")
        } else if (isPrintDebugInfo) {
            append("[: ${type.generate()}]")
        }
    }
}
