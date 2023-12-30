package com.sunnychung.lib.multiplatform.kotlite.model

sealed interface DataType {

    val name: String
    val isNullable: Boolean

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
data class DoubleType(override val isNullable: Boolean = false) : DataType { override val name: String = "Double" }
data class BooleanType(override val isNullable: Boolean = false) : DataType { override val name: String = "Boolean" }
data class StringType(override val isNullable: Boolean = false) : DataType { override val name: String = "String" }
data class UnitType(override val isNullable: Boolean = false) : DataType { override val name: String = "Unit" }
data object NullType : DataType {
    override val name: String = "Null"
    override val isNullable: Boolean = true
}
data class ObjectType(val clazz: ClassDefinition, override val isNullable: Boolean = false) : DataType {
    override val name: String = clazz.name
}

fun TypeNode.toPrimitiveDataType() = when(this.name) {
    "Int" -> IntType(isNullable = isNullable)
    "Double" -> DoubleType(isNullable = isNullable)
    "Boolean" -> BooleanType(isNullable = isNullable)
    "String" -> StringType(isNullable = isNullable)
    "Unit" -> UnitType(isNullable = isNullable)
    else -> null //ObjectType(clazz = clazz!!, isNullable = isNullable)
}
