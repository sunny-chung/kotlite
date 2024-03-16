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
    val position: SourcePosition
    fun toMermaid(): String
}

data class IntegerNode(override val position: SourcePosition, val value: Int) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Integer ${value}\"]\n"
    }
}

data class DoubleNode(override val position: SourcePosition, val value: Double) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Double ${value}\"]\n"
    }
}

data class LongNode(override val position: SourcePosition, val value: Long) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Long ${value}\"]\n"
    }
}

data class BooleanNode(override val position: SourcePosition, val value: Boolean) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Boolean ${value}\"]\n"
    }
}

data class ValueNode(override val position: SourcePosition, val value: RuntimeValue) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Value ${value}\"]\n"
    }
}

data object NullNode : ASTNode {
    override val position: SourcePosition
        get() = SourcePosition("", 1, 1)
    override fun toMermaid(): String {
        return "${generateId()}[\"Null\"]\n"
    }
}

data class BinaryOpNode(override val position: SourcePosition, val node1: ASTNode, val node2: ASTNode, val operator: String, @ModifyByAnalyzer var type: TypeNode? = null,) : ASTNode {
    @ModifyByAnalyzer var hasFunctionCall: Boolean? = null
    @ModifyByAnalyzer var call: FunctionCallNode? = null

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Binary Op ${operator}\"]"
        return "$self-->${node1.toMermaid()}\n$self-->${node2.toMermaid()}\n"
    }
}

data class UnaryOpNode(override val position: SourcePosition, var node: ASTNode?, val operator: String, @ModifyByAnalyzer var type: TypeNode? = null,) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Unary Op ${operator}\"]"
        return "$self-->${node?.toMermaid()}\n"
    }
}

data class ScriptNode(override val position: SourcePosition, val nodes: List<ASTNode>) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Script\"]"
        return nodes.map { "$self-->${it.toMermaid()}\n" }.joinToString("\n")
    }
}

/**
 *
 * @param position This value is only useful when the type is invalid and error detail is needed.
 * Most of the time it can be set to `SourcePosition.NONE`.
 */
open class TypeNode(override val position: SourcePosition, val name: String, val arguments: List<TypeNode>?, val isNullable: Boolean, @ModifyByAnalyzer var transformedRefName: String? = null) : ASTNode {
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
        position = position,
        name = name,
        arguments = arguments,
        isNullable = isNullable,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeNode) return false

//        if (position != other.position) return false
        if (name != other.name) return false
        if (arguments != other.arguments) return false
        if (isNullable != other.isNullable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0 // position.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (arguments?.hashCode() ?: 0)
        result = 31 * result + isNullable.hashCode()
        return result
    }

    override fun toString(): String = descriptiveName()
}

data class PropertyDeclarationNode(
    override val position: SourcePosition,
    val name: String,
    val declaredModifiers: Set<PropertyModifier>,
    val typeParameters: List<TypeParameterNode>,
    val receiver: TypeNode?,
    val declaredType: TypeNode?,
    val isMutable: Boolean,
    val initialValue: ASTNode?,
    val accessors: PropertyAccessorsNode? = null,
    @ModifyByAnalyzer var transformedRefName: String? = null,
    @ModifyByAnalyzer var inferredType: TypeNode? = null,
    @ModifyByAnalyzer val inferredModifiers: MutableSet<PropertyModifier> = mutableSetOf(),
) : ASTNode {
    val type: TypeNode
        get() = declaredType ?: inferredType ?: throw SemanticException(position, "Could not infer type for property `$name`")
    val modifiers: Set<PropertyModifier>
        get() = declaredModifiers + inferredModifiers
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
    /**
     * Used to replace the call of the operator, e.g. "+=".
     */
    @ModifyByAnalyzer var wholeFunctionCall: FunctionCallNode? = null

    /**
     * Only used when wholeFunctionCall is null. Used to replace the call of the operator excluding assignment, e.g. "+".
     */
    @ModifyByAnalyzer var preAssignFunctionCall: FunctionCallNode? = null

    /**
     * Only used when wholeFunctionCall is null. Used to replace the call of the assignment, e.g. "=".
     */
    @ModifyByAnalyzer var assignFunctionCall: FunctionCallNode? = null

    override val position: SourcePosition get() = subject.position

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Assignment Node `$operator`\"]"
        return "$self-- subject -->${subject.toMermaid()}\n" +
                "$self-- value -->${value.toMermaid()}\n"
    }
}

open class VariableReferenceNode(override val position: SourcePosition, val variableName: String, @ModifyByAnalyzer var transformedRefName: String? = null, @ModifyByAnalyzer var ownerRef: PropertyOwnerInfo? = null, @ModifyByAnalyzer var type: TypeNode? = null) : ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"Variable Reference Node `$variableName`\"]"
}

/**
 * Member names are the exact identifiers in Kotlin code
 */
enum class FunctionModifier {
    operator, open, override, abstract, infix
}

enum class FunctionValueParameterModifier {
    vararg
}

enum class ClassModifier {
    open, enum, abstract
}

enum class PropertyModifier {
    open, override
}

data class FunctionValueParameterNode(override val position: SourcePosition, val name: String, val declaredType: TypeNode?, val defaultValue: ASTNode?, val modifiers: Set<FunctionValueParameterModifier>, @ModifyByAnalyzer var transformedRefName: String? = null) : ASTNode {
    @ModifyByAnalyzer
    var inferredType: TypeNode? = null
    val type: TypeNode get() = declaredType ?: inferredType
        ?: throw CannotInferTypeException(position, "function value parameter type $name")

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Value Parameter Node `$name` modifiers=[${modifiers.joinToString(", ")}] \"]"
        return "$self\n" + (if (declaredType != null) "$self-- declared type -->${declaredType.toMermaid()}\n" else "") +
                if (defaultValue != null) "$self-->${defaultValue.toMermaid()}\n" else ""
    }
}

data class BlockNode(
    val statements: List<ASTNode>,
    override val position: SourcePosition,
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
    val position: SourcePosition
    val valueParameters: List<FunctionValueParameterNode>
    val typeParameters: List<TypeParameterNode>
    val returnType: TypeNode
    val name: String?
    val labelName: String?

    fun execute(interpreter: Interpreter, receiver: RuntimeValue?, arguments: List<RuntimeValue>, typeArguments: Map<String, DataType>): RuntimeValue
}

open class FunctionDeclarationNode(
    override val position: SourcePosition,
    override val name: String,
    val receiver: TypeNode? = null,
    val declaredReturnType: TypeNode?,
    override val valueParameters: List<FunctionValueParameterNode>,
    val body: BlockNode?,
    override val typeParameters: List<TypeParameterNode> = emptyList(),
    val declaredModifiers: Set<FunctionModifier> = emptySet(),
    @ModifyByAnalyzer var transformedRefName: String? = null,
    @ModifyByAnalyzer var inferredReturnType: TypeNode? = null,
    @ModifyByAnalyzer var isVararg: Boolean = false,
    @ModifyByAnalyzer val inferredModifiers: MutableSet<FunctionModifier> = mutableSetOf(),
) : ASTNode, CallableNode {
    override val returnType: TypeNode
        get() = declaredReturnType ?: inferredReturnType ?: throw CannotInferTypeException(position, "return type of function $name")
    val modifiers: Set<FunctionModifier>
        get() = declaredModifiers + inferredModifiers

    override val labelName: String?
        get() = null

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Node `$name` modifiers=[${modifiers.joinToString(", ")}]\"]"
        return (declaredReturnType?.let { "$self-- type -->${it.toMermaid()}\n" } ?: "") +
                (receiver?.let { "$self-- receiver -->${it.toMermaid()}\n" } ?: "") +
                (body?.let { "$self--body-->${it.toMermaid()}\n" } ?: "")
    }

    fun resolveGenericParameterType(parameter: FunctionValueParameterNode): TypeNode {
        return parameter.type.resolveGenericParameterType(typeParameters)
    }

    override fun execute(interpreter: Interpreter, receiver: RuntimeValue?, arguments: List<RuntimeValue>, typeArguments: Map<String, DataType>): RuntimeValue {
        with (interpreter) {
            return body?.eval() ?: throw RuntimeException("This function is not implemented")
        }
    }

    open fun copy(
        name: String = this.name,
        receiver: TypeNode? = this.receiver,
        declaredReturnType: TypeNode? = this.declaredReturnType,
        typeParameters: List<TypeParameterNode> = this.typeParameters,
        valueParameters: List<FunctionValueParameterNode> = this.valueParameters,
        modifiers: Set<FunctionModifier> = this.modifiers,
        body: BlockNode? = this.body,
        transformedRefName: String? = this.transformedRefName,
        inferredReturnType: TypeNode? = this.inferredReturnType,
    ): FunctionDeclarationNode {
        if (this::class != FunctionDeclarationNode::class) {
            throw UnsupportedOperationException("Copying subclasses is not supported")
        }
        return FunctionDeclarationNode(
            position = position,
            name = name,
            receiver = receiver,
            declaredReturnType = declaredReturnType,
            typeParameters = typeParameters,
            valueParameters = valueParameters,
            declaredModifiers = modifiers,
            body = body,
            transformedRefName = transformedRefName,
            inferredReturnType = inferredReturnType,
        )
    }


}

data class FunctionCallArgumentNode(override val position: SourcePosition, val index: Int, val name: String? = null, val value: ASTNode) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Argument Node #$index `$name`\"]"
        return "$self-->${value.toMermaid()}\n"
    }
}

data class FunctionCallNode(
    val function: ASTNode,
    val arguments: List<FunctionCallArgumentNode>,
    val declaredTypeArguments: List<TypeNode>,
    override val position: SourcePosition,
    val isSuperclassConstruction: Boolean = false,
    @ModifyByAnalyzer var returnType: TypeNode? = null,
    @ModifyByAnalyzer var functionRefName: String? = null,
    @ModifyByAnalyzer var callableType: CallableType? = null,
    @ModifyByAnalyzer var inferredTypeArguments: List<TypeNode?>? = null,
    @ModifyByAnalyzer var modifierFilter: SearchFunctionModifier? = null,
) : ASTNode {
    val typeArguments: List<TypeNode>
        get() = declaredTypeArguments.emptyToNull() ?: inferredTypeArguments?.let { args ->
            val nonNullArgs = args.filterNotNull()
            if (nonNullArgs.size != args.size) {
                null
            } else {
                nonNullArgs
            }
        } ?: emptyList()
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Function Call\"]"
        return "$self-- function -->${function.toMermaid()}\n" +
                arguments.joinToString("") { "$self-- argument -->${it.toMermaid()}\n" }
    }
}

data class ReturnNode(override val position: SourcePosition, val value: ASTNode?, val returnToLabel: String, @ModifyByAnalyzer var returnToAddress: String) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Return Node `$returnToLabel`\"]"
        return "$self${if (value != null) "-->${value.toMermaid()}" else "" }\n"
    }
}

data class ContinueNode(override val position: SourcePosition, val returnToLabel: String, @ModifyByAnalyzer var returnToAddress: String) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Continue Node `$returnToLabel`\"]"
        return self
    }
}

data class BreakNode(override val position: SourcePosition, val returnToLabel: String, @ModifyByAnalyzer var returnToAddress: String) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Break Node `$returnToLabel`\"]"
        return self
    }
}

data class IfNode(override val position: SourcePosition, val condition: ASTNode, val trueBlock: BlockNode?, val falseBlock: BlockNode?, @ModifyByAnalyzer var type: TypeNode? = null,) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"If Node\"]"
        return "$self-- condition -->${condition.toMermaid()}\n" +
                (if (trueBlock != null) "$self-- true -->${trueBlock.toMermaid()}\n" else "") +
                (if (falseBlock != null) "$self-- false -->${falseBlock.toMermaid()}\n" else "")
    }
}

data class WhileNode(override val position: SourcePosition, val condition: ASTNode, val body: BlockNode?) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"While Node\"]"
        return "$self-- condition -->${condition.toMermaid()}\n" +
                "$self-- loop -->${body?.toMermaid() ?: self}\n"
    }
}

data class ClassParameterNode(
    override val position: SourcePosition,
    val isProperty: Boolean,
    val isMutable: Boolean,
    val modifiers: Set<PropertyModifier>,
    val parameter: FunctionValueParameterNode,

    /**
     * Used when this is a non-property class constructor parameter, where this parameter will have
     * TWO transformedRefName:
     * 1. While resolving default values of other constructor parameters (the one inside FunctionValueParameterNode is used)
     * 2. While resolving initializers and default values of property declarations inside class body (this field is used)
     */
    @ModifyByAnalyzer var transformedRefNameInBody: String? = null,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Class Primary Constructor Parameter Node isProperty=$isProperty isMutable=$isMutable\"]"
        return "$self-->${parameter.toMermaid()}\n"
    }
}

data class ClassPrimaryConstructorNode(override val position: SourcePosition, val parameters: List<ClassParameterNode>) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Class Primary Constructor Node\"]"
        return "$self\n" +
                parameters.joinToString("") { "$self-->${it.toMermaid()}\n" }
    }
}

data class ClassInstanceInitializerNode(override val position: SourcePosition, val block: BlockNode) : ASTNode {
    override fun toMermaid(): String {
        return "${generateId()}[\"Class Init Node\"]-->${block.toMermaid()}\n"
    }
}

data class ClassDeclarationNode(
    override val position: SourcePosition,
    val name: String,
    val isInterface: Boolean, // TODO change to enum
    val declaredModifiers: Set<ClassModifier>,
    val typeParameters: List<TypeParameterNode>,
    val primaryConstructor: ClassPrimaryConstructorNode?,
    val superInvocations: List<ASTNode>?,
    val declarations: List<ASTNode>,
    val enumEntries: List<EnumEntryNode> = emptyList(),
    @ModifyByAnalyzer var fullQualifiedName: String = name,
) : ASTNode {
    @ModifyByAnalyzer val inferredModifiers: MutableSet<ClassModifier> = mutableSetOf()
    val modifiers: Set<ClassModifier>
        get() = declaredModifiers + inferredModifiers

    override fun toMermaid(): String {
        val self = "${generateId()}[\"${if (!isInterface) "Class" else "Interface"} Declaration Node `$name` modifiers=[${modifiers.joinToString(", ")}]\"]"
        return "$self\n" +
                (primaryConstructor?.let { "$self-- primary constructor -->${it.toMermaid()}\n" } ?: "") +
                (superInvocations?.joinToString("") { "$self--super-->${it.toMermaid()}\n" } ?: "") +
                declarations.joinToString("") { "$self-->${it.toMermaid()}\n" }
    }
}

data class ClassMemberReferenceNode(override val position: SourcePosition, val name: String, @ModifyByAnalyzer var transformedRefName: String? = null) : ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"Class Member Reference Node `$name`\"]"
}

data class NavigationNode(
    override val position: SourcePosition,
    val subject: ASTNode,
    val operator: String,
    val member: ClassMemberReferenceNode,
    @ModifyByAnalyzer var type: TypeNode? = null, // data type
    @ModifyByAnalyzer var memberType: MemberType? = null,
    @ModifyByAnalyzer var transformedRefName: String? = null, // for extension property use
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Navigation Node\"]"
        return "$self-- subject -->${subject.toMermaid()}\n" +
                "$self-- access -->${member.toMermaid()}\n"
    }

    enum class MemberType {
        Direct, Extension, Enum
    }
}

class PropertyAccessorsNode(
    override val position: SourcePosition,
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

class StringLiteralNode(override val position: SourcePosition, val content: String) : ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"String Node `$content`\"]"
}

class StringFieldIdentifierNode(override val position: SourcePosition, fieldIdentifier: String) : VariableReferenceNode(position, fieldIdentifier) {
    override fun toMermaid(): String = "${generateId()}[\"String Field Identifier Node `$variableName`\"]"
}

class StringNode(
    override val position: SourcePosition,
    val nodes: List<ASTNode>
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"String Node\"]"
        return "$self\n${nodes.joinToString("") {"$self-->${it.toMermaid()}\n"}}"
    }
}

data class LambdaLiteralNode(
    override val position: SourcePosition,
    val declaredValueParameters: List<FunctionValueParameterNode>,
    val body: BlockNode,
    val label: LabelNode?,
    @ModifyByAnalyzer var type: FunctionTypeNode? = null,
    @ModifyByAnalyzer var accessedRefs: SymbolReferenceSet? = null,
    @ModifyByAnalyzer var parameterTypesUpperBound: List<TypeNode>? = null,
    @ModifyByAnalyzer var returnTypeUpperBound: TypeNode? = null,
) : ASTNode, CallableNode {
    @ModifyByAnalyzer var valueParameterIt: FunctionValueParameterNode? = null

    override val labelName: String?
        get() = label?.label

    override val typeParameters: List<TypeParameterNode> = emptyList()
    override val valueParameters: List<FunctionValueParameterNode>
        get() = if (declaredValueParameters.isEmpty() && parameterTypesUpperBound?.size == 1) {
            if (valueParameterIt == null) {
                valueParameterIt = FunctionValueParameterNode(
                    position = position,
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

class FunctionTypeNode(override val position: SourcePosition, val receiverType: TypeNode? = null, val parameterTypes: List<TypeNode>?, val returnType: TypeNode?, isNullable: Boolean)
    : TypeNode(position, "Function", parameterTypes?.let { p -> returnType?.let { r -> p + r } }, isNullable) {

    override fun descriptiveName(): String {
        var s = "(${parameterTypes?.joinToString(", ") { it.descriptiveName() } ?: "?"}) -> ${returnType?.descriptiveName() ?: "?"}"
        if (isNullable) s = "($s)?"
        return s
    }

    override fun copy(isNullable: Boolean): FunctionTypeNode {
        return FunctionTypeNode(
            position = position,
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

class ClassTypeNode(val clazz: TypeNode) : TypeNode(clazz.position, "Class", listOf(clazz), false)

class CharNode(override val position: SourcePosition, val value: Char) : ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"Char Node `$value` (${value.code})\"]"
}

class AsOpNode(override val position: SourcePosition, val isNullable: Boolean, val expression: ASTNode, val type: TypeNode) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"As${if (isNullable) "?" else ""} Node\"]"
        return "$self-- expr -->${expression.toMermaid()}\n" +
                "$self-- type -->${type.toMermaid()}"
    }
}

class TypeParameterNode(override val position: SourcePosition, val name: String, val typeUpperBound: TypeNode?): ASTNode {
    override fun toMermaid(): String = "${generateId()}[\"Type Parameter Node `$name`\"]" +
        (typeUpperBound?.let { "--type upper bound -->${it.toMermaid()}" } ?: "")
}
fun TypeParameterNode.typeUpperBoundOrAny() = typeUpperBound ?: TypeNode(position, "Any", null, true)

class IndexOpNode(override val position: SourcePosition, val subject: ASTNode, val arguments: List<ASTNode>): ASTNode {
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

data class InfixFunctionCallNode(
    override val position: SourcePosition,
    val node1: ASTNode,
    val node2: ASTNode,
    val functionName: String,
    @ModifyByAnalyzer var type: TypeNode? = null,
) : ASTNode {
    @ModifyByAnalyzer var call: FunctionCallNode? = null
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Infix Function Call ${functionName}\"]"
        return "$self-->${node1.toMermaid()}\n$self-->${node2.toMermaid()}\n"
    }
}

data class ElvisOpNode(
    override val position: SourcePosition,
    val primaryNode: ASTNode,
    val fallbackNode: ASTNode,
    @ModifyByAnalyzer var type: TypeNode? = null,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Elvis Op\"]"
        return "$self--primary-->${primaryNode.toMermaid()}\n$self--fallback-->${fallbackNode.toMermaid()}\n"
    }
}

data class ThrowNode(override val position: SourcePosition, val value: ASTNode) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Throw\"]"
        return "$self-->${value.toMermaid()}"
    }
}

data class TryNode(
    override val position: SourcePosition,
    val mainBlock: BlockNode,
    val catchBlocks: List<CatchNode>,
    val finallyBlock: BlockNode?,
    @ModifyByAnalyzer var type: TypeNode? = null,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Try\"]"
        return "$self--exec-->${mainBlock.toMermaid()}" +
                catchBlocks.withIndex().joinToString { "\n$self--catch[${it.index}]-->${it.value.toMermaid()}" } +
                (finallyBlock?.let { "\n$self--finally-->${it.toMermaid()}" } ?: "")
    }
}

data class CatchNode(
    override val position: SourcePosition,
    val valueName: String,
    val catchType: TypeNode,
    val block: BlockNode,
    @ModifyByAnalyzer var type: TypeNode? = null,
) : ASTNode {
    @ModifyByAnalyzer var valueTransformedRefName: String? = null

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Catch (val `$valueName`)\"]"
        return "$self--type-->${catchType.toMermaid()}" +
                "$self--exec-->${block.toMermaid()}"
    }
}

data class WhenSubjectNode(
    override val position: SourcePosition,
    val valueName: String?,
    val declaredType: TypeNode?,
    val value: ASTNode,
    @ModifyByAnalyzer var valueTransformedRefName: String? = null,
    @ModifyByAnalyzer var type: TypeNode? = null,
) : ASTNode {
    fun hasValueDeclaration(): Boolean = valueName != null

    override fun toMermaid(): String {
        val self = "${generateId()}[\"When subject${valueName?.let { " (val `$valueName`)" } ?: ""}\"]"
        return "$self--expr-->${value.toMermaid()}" +
                (declaredType?.let { "\n$self--type-->${it.toMermaid()}" } ?: "")
    }
}

data class WhenConditionNode(
    override val position: SourcePosition,
    val testType: TestType,
    val expression: ASTNode,
    @ModifyByAnalyzer var type: TypeNode? = null,
) : ASTNode {
    enum class TestType {
        RangeTest, TypeTest, Regular
    }

    override fun toMermaid(): String {
        val self = "${generateId()}[\"When condition; type = $testType\"]"
        return "$self--expr-->${expression.toMermaid()}"
    }
}

data class WhenEntryNode(
    override val position: SourcePosition,
    val conditions: List<WhenConditionNode>,
    val body: BlockNode,
    @ModifyByAnalyzer var bodyType: TypeNode? = null,
) : ASTNode {
    fun isElseCondition(): Boolean = conditions.isEmpty()

    override fun toMermaid(): String {
        val self = "${generateId()}[\"When entry\"]"
        return "$self--expr-->${body.toMermaid()}" +
            conditions.joinToString { "\n$self-->${it.toMermaid()}" }
    }
}

data class WhenNode(
    override val position: SourcePosition,
    val subject: WhenSubjectNode?,
    val entries: List<WhenEntryNode>,
    @ModifyByAnalyzer var type: TypeNode? = null,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"When\"]"
        return entries.withIndex().joinToString("\n") { "$self--entry[${it.index}]-->${it.value.toMermaid()}" } +
            (subject?.let { "\n$self--subject-->${it.toMermaid()}" } ?: "")
    }
}

data class LabelNode(
    override val position: SourcePosition,
    val label: String,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Label '$label'\"]"
        return self
    }
}

data class EnumEntryNode(
    override val position: SourcePosition,
    val name: String,
    val arguments: List<FunctionCallArgumentNode>,
) : ASTNode {
    @ModifyByAnalyzer var call: FunctionCallNode? = null
    override fun toMermaid(): String {
        val self = "${generateId()}[\"Enum '$name'\"]"
        return self + arguments.withIndex().joinToString {
            "\n$self--arg[${it.index}]-->${it.value.toMermaid()}"
        }
    }
}

data class ValueParameterDeclarationNode(
    override val position: SourcePosition,
    val name: String,
    val declaredType: TypeNode?,
    @ModifyByAnalyzer var transformedRefName: String? = null,
) : ASTNode {
    @ModifyByAnalyzer
    var inferredType: TypeNode? = null
    val type: TypeNode
        get() = declaredType ?: inferredType
            ?: throw CannotInferTypeException(position, "value parameter type $name")

    override fun toMermaid(): String {
        val self = "${generateId()}[\"Value Parameter '$name'\"]"
        return self + declaredType?.let {
            "\n$self--type-->${it.toMermaid()}"
        }
    }
}

data class ForNode(
    override val position: SourcePosition,
    val variables: List<ValueParameterDeclarationNode>,
    val subject: ASTNode,
    val body: BlockNode,
) : ASTNode {
    override fun toMermaid(): String {
        val self = "${generateId()}[\"For\"]"
        return self +
            subject.let { "\n$self--subject-->${it.toMermaid()}" } +
            body.let { "\n$self--body-->${it.toMermaid()}" } +
            variables.withIndex().joinToString("") { "\n$self--var[${it.index}]-->${it.value.toMermaid()}" }
    }
}
