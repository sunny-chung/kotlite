package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.annotation.ModifyByAnalyzer
import kotlin.random.Random

fun generateId() = Random.nextInt()

sealed interface ASTNode {
    fun toMermaid(): String
}

data class IntegerNode(val value: Int) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Integer ${value}\"]\n"
    }
}

data class DoubleNode(val value: Double) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Double ${value}\"]\n"
    }
}

data class BooleanNode(val value: Boolean) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Boolean ${value}\"]\n"
    }
}

data class ValueNode(val value: RuntimeValue) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Value ${value}\"]\n"
    }
}

data object NullNode : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Null\"]\n"
    }
}

data class BinaryOpNode(val node1: ASTNode, val node2: ASTNode, val operator: String) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Binary Op ${operator}\"]"
        return "$self-->${node1.toMermaid()}\n$self-->${node2.toMermaid()}\n"
    }
}

data class UnaryOpNode(var node: ASTNode?, val operator: String) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Unary Op ${operator}\"]"
        return "$self-->${node?.toMermaid()}\n"
    }
}

data class ScriptNode(val nodes: List<ASTNode>) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Script\"]"
        return nodes.map { "$self-->${it.toMermaid()}\n" }.joinToString("\n")
    }
}

data class TypeNode(val name: String, val argument: TypeNode?, val isNullable: Boolean) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Type $name${if (isNullable) " ?" else ""}\"]" + (if (argument != null) "-->${argument.toMermaid()}" else "") + "\n"
    }
}

data class PropertyDeclarationNode(
    val name: String,
    val type: TypeNode,
    val isMutable: Boolean,
    val initialValue: ASTNode?,
    val accessors: PropertyAccessorsNode? = null,
    @ModifyByAnalyzer var transformedRefName: String? = null
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Property Node `$name`\"]"
        return "$self-->${type.toMermaid()}\n$self-->${initialValue?.toMermaid()}\n"
    }
}

data class AssignmentNode(val subject: ASTNode, val operator: String, val value: ASTNode, @ModifyByAnalyzer @Deprecated("To be removed") var transformedRefName: String? = null) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Assignment Node `$operator`\"]"
        return "$self-- subject -->${subject.toMermaid()}\n" +
                "$self-- value -->${value.toMermaid()}\n"
    }
}

data class VariableReferenceNode(val variableName: String, @ModifyByAnalyzer var transformedRefName: String? = null, @ModifyByAnalyzer var ownerRef: String? = null) : ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"Variable Reference Node `$variableName`\"]"
}

data class FunctionValueParameterNode(val name: String, val type: TypeNode, val defaultValue: ASTNode?, @ModifyByAnalyzer var transformedRefName: String? = null) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Value Parameter Node `$name`\"]"
        return "$self-->${type.toMermaid()}\n" +
                if (defaultValue != null) "$self-->${defaultValue.toMermaid()}\n" else ""
    }
}

data class BlockNode(val statements: List<ASTNode>, val position: SourcePosition, val type: ScopeType) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Block Node\"]"
        return statements.joinToString("") { "$self-->${it.toMermaid()}\n" }
    }
}

data class FunctionDeclarationNode(val name: String, val type: TypeNode, val valueParameters: List<FunctionValueParameterNode>, val body: BlockNode) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Node `$name`\"]"
        return "$self-- type -->${type.toMermaid()}\n" +
                "$self-->${body.toMermaid()}\n"
    }
}

data class FunctionCallArgumentNode(val index: Int, val name: String? = null, val value: ASTNode) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Argument Node #$index `$name`\"]"
        return "$self-->${value.toMermaid()}\n"
    }
}

data class FunctionCallNode(val function: ASTNode, val arguments: List<FunctionCallArgumentNode>, val position: SourcePosition) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Call\"]"
        return "$self-- function -->${function.toMermaid()}\n" +
                arguments.joinToString("") { "$self-- argument -->${it.toMermaid()}\n" }
    }
}

data class ReturnNode(val value: ASTNode?, val returnToLabel: String, @ModifyByAnalyzer var returnToAddress: String) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Return Node `$returnToLabel`\"]"
        return "$self${if (value != null) "-->${value.toMermaid()}" else "" }\n"
    }
}

data class ContinueNode(val returnToLabel: String, @ModifyByAnalyzer var returnToAddress: String) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Continue Node `$returnToLabel`\"]"
        return self
    }
}

data class BreakNode(val returnToLabel: String, @ModifyByAnalyzer var returnToAddress: String) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Break Node `$returnToLabel`\"]"
        return self
    }
}

data class IfNode(val condition: ASTNode, val trueBlock: BlockNode?, val falseBlock: BlockNode?) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"If Node\"]"
        return "$self-- condition -->${condition.toMermaid()}\n" +
                (if (trueBlock != null) "$self-- true -->${trueBlock.toMermaid()}\n" else "") +
                (if (falseBlock != null) "$self-- false -->${falseBlock.toMermaid()}\n" else "")
    }
}

data class WhileNode(val condition: ASTNode, val body: BlockNode?) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"While Node\"]"
        return "$self-- condition -->${condition.toMermaid()}\n" +
                "$self-- loop -->${body?.toMermaid() ?: self}\n"
    }
}

data class ClassParameterNode(val isProperty: Boolean, val isMutable: Boolean, val parameter: FunctionValueParameterNode) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Class Primary Constructor Parameter Node isProperty=$isProperty isMutable=$isMutable\"]"
        return "$self-->${parameter.toMermaid()}\n"
    }
}

data class ClassPrimaryConstructorNode(val parameters: List<ClassParameterNode>) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Class Primary Constructor Node\"]"
        return "$self\n" +
                parameters.joinToString("") { "$self-->${it.toMermaid()}\n" }
    }
}

data class ClassInstanceInitializerNode(val block: BlockNode) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Class Init Node\"]-->${block.toMermaid()}\n"
    }
}

data class ClassDeclarationNode(
    val name: String,
    val primaryConstructor: ClassPrimaryConstructorNode?,
    val declarations: List<ASTNode>,
    @ModifyByAnalyzer var fullQualifiedName: String = name,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Class Declaration Node `$name`\"]"
        return "$self\n" +
                (primaryConstructor?.let { "$self-- primary constructor -->${it.toMermaid()}\n" } ?: "") +
                declarations.joinToString("") { "$self-->${it.toMermaid()}\n" }
    }
}

data class ClassMemberReferenceNode(val name: String, @ModifyByAnalyzer var transformedRefName: String? = null) : ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"Class Member Reference Node `$name`\"]"
}

data class NavigationNode(
    val subject: ASTNode,
    val operator: String,
    val member: ClassMemberReferenceNode,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Navigation Node\"]"
        return "$self-- subject -->${subject.toMermaid()}\n" +
                "$self-- access -->${member.toMermaid()}\n"
    }
}

class PropertyAccessorsNode(
    val getter: FunctionDeclarationNode?,
    val setter: FunctionDeclarationNode?,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Navigation Node\"]"
        return "$self\n${getter?.let {"$self-- getter -->${it.toMermaid()}\n"}}\n" +
                "${setter?.let {"$self-- setter -->${it.toMermaid()}\n"}}"
    }
}
