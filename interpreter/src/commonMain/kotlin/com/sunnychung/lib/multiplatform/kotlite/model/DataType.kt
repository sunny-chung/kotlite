package com.sunnychung.lib.multiplatform.kotlite.model

sealed interface DataType {

    val name: String // fully qualified name
    val isNullable: Boolean

    val nameWithNullable get() = "$name${if (isNullable) "?" else ""}"

    // Let class B extends A
    // A.isAssignableFrom(B) = true
    // A.isAssignableTo(B) = false
    // B.isAssignableFrom(A) = false
    // B.isAssignableTo(A) = true
    fun isAssignableFrom(other: DataType): Boolean {
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
}

data class IntType(override val isNullable: Boolean = false) : DataType { override val name: String = "Int" }
data class LongType(override val isNullable: Boolean = false) : DataType { override val name: String = "Long" }
data class DoubleType(override val isNullable: Boolean = false) : DataType { override val name: String = "Double" }
data class BooleanType(override val isNullable: Boolean = false) : DataType { override val name: String = "Boolean" }
data class StringType(override val isNullable: Boolean = false) : DataType { override val name: String = "String" }
data class CharType(override val isNullable: Boolean = false) : DataType { override val name: String = "Char" }
data class UnitType(override val isNullable: Boolean = false) : DataType { override val name: String = "Unit" }
data object NullType : DataType {
    override val name: String = "Nothing"
    override val nameWithNullable: String = "Nothing"
    override val isNullable: Boolean = true
}
data class AnyType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "Any"

    override fun isAssignableFrom(other: DataType): Boolean {
        return isNullable || !other.isNullable
    }
}
data class ObjectType(val clazz: ClassDefinition, override val isNullable: Boolean = false) : DataType {
    override val name: String = clazz.fullQualifiedName
}

/**
 * Only use in semantic analyzer
 */
data object UnresolvedType : DataType {
    override val name: String = "Unresolved"
    override val isNullable: Boolean = false
}

data class FunctionType(val arguments: List<DataType>, val returnType: DataType, override val isNullable: Boolean) : DataType {
    override val name: String = "Function"

    override val nameWithNullable: String
        get() = "Function<${(arguments + returnType).joinToString(", ") { it.nameWithNullable }}>".let {
            if (isNullable) "($it)?" else it
        }

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
}

fun TypeNode.toPrimitiveDataType() = when(this.name) {
    "Int" -> IntType(isNullable = isNullable)
    "Long" -> LongType(isNullable = isNullable)
    "Double" -> DoubleType(isNullable = isNullable)
    "Boolean" -> BooleanType(isNullable = isNullable)
    "String" -> StringType(isNullable = isNullable)
    "Char" -> CharType(isNullable = isNullable)
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
