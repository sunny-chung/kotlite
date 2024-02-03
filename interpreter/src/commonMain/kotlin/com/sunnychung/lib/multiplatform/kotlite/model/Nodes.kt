package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.annotation.ModifyByAnalyzer
import com.sunnychung.lib.multiplatform.kotlite.error.CannotInferTypeException
import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.extension.emptyToNull
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterType
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

data class LongNode(val value: Long) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Long ${value}\"]\n"
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

data class BinaryOpNode(val node1: ASTNode, val node2: ASTNode, val operator: String, @ModifyByAnalyzer var type: TypeNode? = null,) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Binary Op ${operator}\"]"
        return "$self-->${node1.toMermaid()}\n$self-->${node2.toMermaid()}\n"
    }
}

data class UnaryOpNode(var node: ASTNode?, val operator: String, @ModifyByAnalyzer var type: TypeNode? = null,) : ASTNode {
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

open class TypeNode(val name: String, val arguments: List<TypeNode>?, val isNullable: Boolean, @ModifyByAnalyzer var transformedRefName: String? = null) : ASTNode {
    init {
        if (arguments?.isEmpty() == true) throw IllegalArgumentException("empty argument")
        if (name == "Function" && this !is FunctionTypeNode) throw IllegalArgumentException("function type node should be a FunctionTypeNode instance")
    }

    open fun descriptiveName(): String = "$name${arguments?.let { "<${it.joinToString(", ") { it.descriptiveName() }}>" } ?: ""}${if (isNullable) "?" else ""}"

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Type $name${if (isNullable) " ?" else ""}\"]"
        return "$self\n" + (arguments?.joinToString("") { "$self-->${it.toMermaid()}\n" } ?: "")
    }

    open fun copy(isNullable: Boolean) = TypeNode(
        name = name,
        arguments = arguments,
        isNullable = isNullable,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeNode) return false

        if (name != other.name) return false
        if (arguments != other.arguments) return false
        if (isNullable != other.isNullable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (arguments?.hashCode() ?: 0)
        result = 31 * result + isNullable.hashCode()
        return result
    }
}

data class PropertyDeclarationNode(
    val name: String,
    val typeParameters: List<TypeParameterNode>,
    val receiver: TypeNode?,
    val declaredType: TypeNode?,
    val isMutable: Boolean,
    val initialValue: ASTNode?,
    val accessors: PropertyAccessorsNode? = null,
    @ModifyByAnalyzer var transformedRefName: String? = null,
    @ModifyByAnalyzer var inferredType: TypeNode? = null,
) : ASTNode {
    val type: TypeNode
        get() = declaredType ?: inferredType ?: throw SemanticException("Could not infer type for property `$name`")
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Property Node `$name`\"]"
        return "$self\n" +
                (receiver?.let { "$self-- receiver type -->${it.toMermaid()}\n" } ?: "") +
                (declaredType?.let { "$self-- declared type -->${it.toMermaid()}\n" } ?: "") +
                (inferredType?.let { "$self-- inferred type -->${it.toMermaid()}\n" } ?: "") +
                (initialValue?.let { "$self-- initial value -->${it.toMermaid()}\n" } ?: "")
    }
}

data class AssignmentNode(val subject: ASTNode, val operator: String, val value: ASTNode, @ModifyByAnalyzer @Deprecated("To be removed") var transformedRefName: String? = null) : ASTNode {
    @ModifyByAnalyzer var functionCall: FunctionCallNode? = null

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Assignment Node `$operator`\"]"
        return "$self-- subject -->${subject.toMermaid()}\n" +
                "$self-- value -->${value.toMermaid()}\n"
    }
}

open class VariableReferenceNode(val variableName: String, @ModifyByAnalyzer var transformedRefName: String? = null, @ModifyByAnalyzer var ownerRef: PropertyOwnerInfo? = null, @ModifyByAnalyzer var type: TypeNode? = null) : ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"Variable Reference Node `$variableName`\"]"
}

/**
 * Member names are the exact identifiers in Kotlin code
 */
enum class FunctionModifier {
    operator
}

enum class FunctionValueParameterModifier {
    vararg
}

data class FunctionValueParameterNode(val name: String, val declaredType: TypeNode?, val defaultValue: ASTNode?, val modifiers: Set<FunctionValueParameterModifier>, @ModifyByAnalyzer var transformedRefName: String? = null) : ASTNode {
    @ModifyByAnalyzer var inferredType: TypeNode? = null
    val type: TypeNode get() = declaredType ?: inferredType
        ?: throw CannotInferTypeException("function value parameter type $name")

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Value Parameter Node `$name`\"]"
        return "$self\n" + (if (declaredType != null) "$self-- declared type -->${declaredType.toMermaid()}\n" else "") +
                if (defaultValue != null) "$self-->${defaultValue.toMermaid()}\n" else ""
    }
}

data class BlockNode(
    val statements: List<ASTNode>,
    val position: SourcePosition,
    val type: ScopeType,
    val format: FunctionBodyFormat,
    @ModifyByAnalyzer var returnType: TypeNode? = null,
    @ModifyByAnalyzer var returnTypeUpperBound: TypeNode? = null,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Block Node\"]"
        return statements.joinToString("") { "$self-->${it.toMermaid()}\n" }
    }
}

interface CallableNode {
    val valueParameters: List<FunctionValueParameterNode>
    val typeParameters: List<TypeParameterNode>
    val returnType: TypeNode
    val name: String?

    fun execute(interpreter: Interpreter, receiver: RuntimeValue?, arguments: List<RuntimeValue>, typeArguments: Map<String, DataType>): RuntimeValue
}

open class FunctionDeclarationNode(
    override val name: String,
    val receiver: TypeNode? = null,
    val declaredReturnType: TypeNode?,
    override val valueParameters: List<FunctionValueParameterNode>,
    val body: BlockNode,
    override val typeParameters: List<TypeParameterNode> = emptyList(),
    val modifiers: Set<FunctionModifier> = emptySet(),
    @ModifyByAnalyzer var transformedRefName: String? = null,
    @ModifyByAnalyzer var inferredReturnType: TypeNode? = null,
    @ModifyByAnalyzer var isVararg: Boolean = false
) : ASTNode, CallableNode {
    override val returnType: TypeNode
        get() = declaredReturnType ?: inferredReturnType ?: throw CannotInferTypeException("return type of function $name")

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Node `$name`\"]"
        return (declaredReturnType?.let { "$self-- type -->${it.toMermaid()}\n" } ?: "") +
                (receiver?.let { "$self-- receiver -->${it.toMermaid()}\n" } ?: "") +
                "$self-->${body.toMermaid()}\n"
    }

    fun resolveGenericParameterType(parameter: FunctionValueParameterNode): TypeNode {
        return parameter.type.resolveGenericParameterType(typeParameters)
    }

    override fun execute(interpreter: Interpreter, receiver: RuntimeValue?, arguments: List<RuntimeValue>, typeArguments: Map<String, DataType>): RuntimeValue {
        with (interpreter) {
            return body.eval()
        }
    }

    open fun copy(
        name: String = this.name,
        receiver: TypeNode? = this.receiver,
        declaredReturnType: TypeNode? = this.declaredReturnType,
        typeParameters: List<TypeParameterNode> = this.typeParameters,
        valueParameters: List<FunctionValueParameterNode> = this.valueParameters,
        modifiers: Set<FunctionModifier> = this.modifiers,
        body: BlockNode = this.body,
        transformedRefName: String? = this.transformedRefName,
        inferredReturnType: TypeNode? = this.inferredReturnType,
    ): FunctionDeclarationNode {
        if (this::class != FunctionDeclarationNode::class) {
            throw UnsupportedOperationException("Copying subclasses is not supported")
        }
        return FunctionDeclarationNode(
            name = name,
            receiver = receiver,
            declaredReturnType = declaredReturnType,
            typeParameters = typeParameters,
            valueParameters = valueParameters,
            modifiers = modifiers,
            body = body,
            transformedRefName = transformedRefName,
            inferredReturnType = inferredReturnType,
        )
    }


}

data class FunctionCallArgumentNode(val index: Int, val name: String? = null, val value: ASTNode) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Argument Node #$index `$name`\"]"
        return "$self-->${value.toMermaid()}\n"
    }
}

data class FunctionCallNode(
    val function: ASTNode,
    val arguments: List<FunctionCallArgumentNode>,
    val declaredTypeArguments: List<TypeNode>,
    val position: SourcePosition,
    @ModifyByAnalyzer var returnType: TypeNode? = null,
    @ModifyByAnalyzer var functionRefName: String? = null,
    @ModifyByAnalyzer var callableType: CallableType? = null,
    @ModifyByAnalyzer var inferredTypeArguments: List<TypeNode>? = null,
    @ModifyByAnalyzer var modifierFilter: SearchFunctionModifier? = null,
) : ASTNode {
    val typeArguments: List<TypeNode>
        get() = declaredTypeArguments.emptyToNull() ?: inferredTypeArguments ?: emptyList()
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

data class IfNode(val condition: ASTNode, val trueBlock: BlockNode?, val falseBlock: BlockNode?, @ModifyByAnalyzer var type: TypeNode? = null,) : ASTNode {
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
    val typeParameters: List<TypeParameterNode>,
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
    @ModifyByAnalyzer var type: TypeNode? = null,
    @ModifyByAnalyzer var transformedRefName: String? = null, // for extension property use
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Navigation Node\"]"
        return "$self-- subject -->${subject.toMermaid()}\n" +
                "$self-- access -->${member.toMermaid()}\n"
    }
}

class PropertyAccessorsNode(
    val type: TypeNode,
    val getter: FunctionDeclarationNode?,
    val setter: FunctionDeclarationNode?,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Navigation Node\"]"
        return "$self\n${getter?.let {"$self-- getter -->${it.toMermaid()}\n"} ?: ""}\n" +
                (setter?.let {"$self-- setter -->${it.toMermaid()}\n"} ?: "")
    }
}

class StringLiteralNode(val content: String) : ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"String Node `$content`\"]"
}

class StringFieldIdentifierNode(fieldIdentifier: String) : VariableReferenceNode(fieldIdentifier) {
    override fun toMermaid(): String = "${generateId()}[\"String Field Identifier Node `$variableName`\"]"
}

class StringNode(
    val nodes: List<ASTNode>
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"String Node\"]"
        return "$self\n${nodes.joinToString("") {"$self-->${it.toMermaid()}\n"}}"
    }
}

data class LambdaLiteralNode(
    val declaredValueParameters: List<FunctionValueParameterNode>,
    val body: BlockNode,
    @ModifyByAnalyzer var type: FunctionTypeNode? = null,
    @ModifyByAnalyzer var accessedRefs: SymbolReferenceSet? = null,
    @ModifyByAnalyzer var parameterTypesUpperBound: List<TypeNode>? = null,
    @ModifyByAnalyzer var returnTypeUpperBound: TypeNode? = null,
) : ASTNode, CallableNode {
    @ModifyByAnalyzer var valueParameterIt: FunctionValueParameterNode? = null

    override val typeParameters: List<TypeParameterNode> = emptyList()
    override val valueParameters: List<FunctionValueParameterNode>
        get() = if (declaredValueParameters.isEmpty() && parameterTypesUpperBound?.size == 1) {
            if (valueParameterIt == null) {
                valueParameterIt = FunctionValueParameterNode(
                    name = "it",
                    declaredType = parameterTypesUpperBound!!.first(),
                    defaultValue = null,
                    modifiers = emptySet()
                )
            }
            listOf(valueParameterIt!!)
        } else {
            declaredValueParameters
        }

    override val returnType: TypeNode
        get() = type!!.returnType!!

    override val name: String?
        get() = null

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Lambda Node\"]"
        return valueParameters.joinToString("") { "$self-- parameter -->${it.toMermaid() }\n" } +
                "$self-->${body.toMermaid()}\n"
    }

    override fun execute(interpreter: Interpreter, receiver: RuntimeValue?, arguments: List<RuntimeValue>, typeArguments: Map<String, DataType>): RuntimeValue {
        with (interpreter) {
            return body.eval()
        }
    }
}

class FunctionTypeNode(val receiverType: TypeNode? = null, val parameterTypes: List<TypeNode>?, val returnType: TypeNode?, isNullable: Boolean)
    : TypeNode("Function", parameterTypes?.let { p -> returnType?.let { r -> p + r } }, isNullable) {

    override fun descriptiveName(): String {
        var s = "(${parameterTypes!!.joinToString(", ") { it.descriptiveName() }}) -> ${returnType!!.descriptiveName()}"
        if (isNullable) s = "($s)?"
        return s
    }

    override fun copy(isNullable: Boolean): FunctionTypeNode {
        return FunctionTypeNode(
            receiverType = receiverType,
            parameterTypes = parameterTypes,
            returnType = returnType,
            isNullable = isNullable,
        )
    }

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Type $name${if (isNullable) " ?" else ""}\"]"
        return "$self\n" +
                (receiverType?.let { "$self- -receiver -->${it.toMermaid()}" } ?: "") +
                parameterTypes!!.joinToString("") { "$self-- parameter -->${it.toMermaid()}\n" } +
                "$self-- return -->${returnType!!.toMermaid()}"
    }
}

class ClassTypeNode(val clazz: TypeNode) : TypeNode("Class", listOf(clazz), false)

class CharNode(val value: Char) : ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"Char Node `$value` (${value.code})\"]"
}

class AsOpNode(val isNullable: Boolean, val expression: ASTNode, val type: TypeNode) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"As${if (isNullable) "?" else ""} Node\"]"
        return "$self-- expr -->${expression.toMermaid()}\n" +
                "$self-- type -->${type.toMermaid()}"
    }
}

class TypeParameterNode(val name: String, val typeUpperBound: TypeNode?): ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"Type Parameter Node `$name`\"]" +
        (typeUpperBound?.let { "--type upper bound -->${it.toMermaid()}" } ?: "")
}
fun TypeParameterNode.typeUpperBoundOrAny() = typeUpperBound ?: TypeNode("Any", null, true)

class IndexOpNode(val subject: ASTNode, val arguments: List<ASTNode>): ASTNode {
    @ModifyByAnalyzer var hasFunctionCall: Boolean? = null
    @ModifyByAnalyzer var call: FunctionCallNode? = null
    @ModifyByAnalyzer var type: TypeNode? = null

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Index Op Node\"]"
        return "$self-- subject -->${subject.toMermaid()}\n" +
                arguments.mapIndexed { index, it ->
                    "$self-- argument[$index] -->${it.toMermaid()}"
                }.joinToString("\n")
    }
}
