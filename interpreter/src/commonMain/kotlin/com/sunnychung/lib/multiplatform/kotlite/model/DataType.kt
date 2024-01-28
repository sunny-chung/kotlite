package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.extension.emptyToNull

sealed interface DataType {

    val name: String // fully qualified name
    val isNullable: Boolean

    val nameWithNullable get() = "$name${if (isNullable) "?" else ""}"

    val descriptiveName get() = nameWithNullable

    // Let class B extends A
    // A.isAssignableFrom(B) = true
    // A.isAssignableTo(B) = false
    // B.isAssignableFrom(A) = false
    // B.isAssignableTo(A) = true
    fun isAssignableFrom(other: DataType): Boolean {
//        val other = if (other is TypeParameterType) {
//            other.upperBound
//        } else other
        if (other is NullType && isNullable) return true
        if (other::class == this::class) {
            if (other.isNullable && !this.isNullable) {
                return false
            }
            return true
        }
        return false
    }

    fun isAssignableTo(other: DataType): Boolean {
        return other.isAssignableFrom(this)
    }

    // It is similar to `isAssignableFrom()`, except would return true for assignable type arguments
    // e.g. List<Any>.isAssignableFrom(List<Int>) = false, but
    // List<Any>.isConvertibleFrom(List<Int>) = true
    fun isConvertibleFrom(other: DataType): Boolean {
        return this.isAssignableFrom(other)
    }

    fun copyOf(isNullable: Boolean): DataType

    fun toTypeNode() = TypeNode(name, null, isNullable)
}

data class IntType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "Int"
    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)
}
data class LongType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "Long"
    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)
}
data class DoubleType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "Double"
    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)
}
data class BooleanType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "Boolean"
    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)
}
data class StringType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "String"
    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)
}
data class CharType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "Char"
    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)
}
data class ByteType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "Byte"
    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)
}
data class UnitType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "Unit"
    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)
}
data object NullType : DataType {
    override val name: String = "Nothing"
    override val nameWithNullable: String = "Nothing"
    override val isNullable: Boolean = true
    override fun copyOf(isNullable: Boolean) = this
}
data class AnyType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "Any"

    override fun copyOf(isNullable: Boolean): DataType = if (this.isNullable == isNullable) this else AnyType(isNullable)

    override fun isAssignableFrom(other: DataType): Boolean {
        return isNullable || !other.isNullable
    }
}
data class ObjectType(val clazz: ClassDefinition, val arguments: List<DataType>, override val isNullable: Boolean = false) : DataType {
    override val name: String = clazz.fullQualifiedName
    override val descriptiveName: String = "${name}${
        arguments.emptyToNull()?.let { "<${it.joinToString(", ") { it.descriptiveName }}>" } ?: ""
    }${if (isNullable) "?" else ""}"

    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)

    override fun isAssignableFrom(other: DataType): Boolean {
        if (other is NullType && isNullable) return true
        return other is ObjectType &&
                other.clazz.fullQualifiedName == clazz.fullQualifiedName &&
                other.arguments == arguments &&
                (isNullable || !other.isNullable)
    }

    override fun isConvertibleFrom(other: DataType): Boolean {
        if (other is NullType && isNullable) return true
        return other is ObjectType &&
                other.clazz.fullQualifiedName == clazz.fullQualifiedName &&
                other.arguments.withIndex().all {
                    arguments[it.index].isConvertibleFrom(it.value)
                } &&
                (isNullable || !other.isNullable)
    }

    override fun toTypeNode(): TypeNode {
        return TypeNode(name, arguments.map { it.toTypeNode() }.emptyToNull(), isNullable)
    }
}

/**
 * Only use in semantic analyzer
 */
data object UnresolvedType : DataType {
    override val name: String = "Unresolved"
    override val isNullable: Boolean = false
    override fun copyOf(isNullable: Boolean) = this
}

data class TypeParameterType(
    override val name: String,
    override val isNullable: Boolean = false,
    val upperBound: DataType
) : DataType {
    override val nameWithNullable: String
        get() = "<$name>${if (isNullable) "?" else ""}"

    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)

    override fun isConvertibleFrom(other: DataType): Boolean {
        return upperBound.isAssignableFrom(other)
    }

    override fun isAssignableFrom(other: DataType): Boolean {
        if (other is NullType && isNullable) return true
        return other is TypeParameterType &&
                other.name == name &&
                (isNullable || !other.isNullable) &&
                other.upperBound == upperBound
    }
}

data class FunctionType(val arguments: List<DataType>, val returnType: DataType, override val isNullable: Boolean) : DataType {
    override val name: String = "Function"

    override val nameWithNullable: String
        get() = "Function<${(arguments + returnType).joinToString(", ") { it.descriptiveName }}>".let {
            if (isNullable) "($it)?" else it
        }

    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)

    override fun isAssignableFrom(other: DataType): Boolean {
        if (other is NullType && isNullable) return true
        if (!super.isAssignableFrom(other) || other !is FunctionType) return false

        if (returnType is UnresolvedType || other.returnType is UnresolvedType) return true

        // val f: (Int) -> Any = (g as (Int) -> Int)
        if (!returnType.isAssignableFrom(other.returnType)) return false
        if (arguments.size != other.arguments.size) return false
        arguments.forEachIndexed { i, _ ->
            // val f: (Any) -> Unit = (g as (Int) -> Unit)
            if (!arguments[i].isAssignableFrom(other.arguments[i])) {
                return false
            }
        }
        return true
    }

    override fun toTypeNode(): TypeNode {
        return FunctionTypeNode(
            parameterTypes = arguments.map { it.toTypeNode() },
            returnType = returnType.toTypeNode(),
            isNullable = isNullable,
        )
    }
}

fun TypeNode.toPrimitiveDataType() = when(this.name) {
    "Int" -> IntType(isNullable = isNullable)
    "Long" -> LongType(isNullable = isNullable)
    "Double" -> DoubleType(isNullable = isNullable)
    "Boolean" -> BooleanType(isNullable = isNullable)
    "String" -> StringType(isNullable = isNullable)
    "Char" -> CharType(isNullable = isNullable)
    "Byte" -> ByteType(isNullable = isNullable)
    "Unit" -> UnitType(isNullable = isNullable)
    "Any" -> AnyType(isNullable = isNullable)
    "Nothing" -> NullType
    else -> null //ObjectType(clazz = clazz!!, isNullable = isNullable)
}

fun DataType.isNonNullNumberType() = when (this) {
    is DoubleType -> !isNullable
    is IntType -> !isNullable
    is LongType -> !isNullable
    else -> false
}

fun DataType.isNonNullIntegralType() = when (this) {
    is IntType -> !isNullable
    is LongType -> !isNullable
    else -> false
}

fun DataType.toTypeNode(): TypeNode = when (this) {
    is TypeParameterType -> upperBound.toTypeNode()
    is ObjectType -> TypeNode(
        name,
        arguments.map { it.toTypeNode() }.emptyToNull(),
        isNullable
    )
    else -> TypeNode(
        name,
        null,
        isNullable
    )
}
