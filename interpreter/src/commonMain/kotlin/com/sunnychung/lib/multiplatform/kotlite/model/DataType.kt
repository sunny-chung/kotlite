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
        if (other is NothingType && isNullable) return true
        if (other::class == this::class) {
            if (other.isNullable && !this.isNullable) {
                return false
            }
            return true
        }
        return false
    }

    fun isSubTypeOf(other: DataType): Boolean = false

    fun isAssignableTo(other: DataType): Boolean {
        return other.isAssignableFrom(this)
    }

    // It is similar to `isAssignableFrom()`, except would return true for assignable type arguments
    // e.g. List<Any>.isAssignableFrom(List<Int>) = false, but
    // List<Any>.isConvertibleFrom(List<Int>) = true
    fun isConvertibleFrom(other: DataType): Boolean {
        return this.isAssignableFrom(other)
    }

    fun isConvertibleTo(other:DataType): Boolean {
        return other.isConvertibleFrom(this)
    }

    fun copyOf(isNullable: Boolean): DataType

    fun toTypeNode() = TypeNode(SourcePosition.NONE, name, null, isNullable)
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
data class NothingType(override val isNullable: Boolean) : DataType {
    override val name: String = "Nothing"

    override fun copyOf(isNullable: Boolean) = this

    override fun isAssignableFrom(other: DataType): Boolean {
        return if (isNullable) {
            super.isAssignableFrom(other)
        } else {
            other is NothingType && !other.isNullable
        }
    }
}
data class AnyType(override val isNullable: Boolean = false) : DataType {
    override val name: String = "Any"

    override fun copyOf(isNullable: Boolean): DataType = if (this.isNullable == isNullable) this else AnyType(isNullable)

    override fun isAssignableFrom(other: DataType): Boolean {
        return isNullable || !other.isNullable
    }
}
data object StarType : DataType {
    override val name: String = "*"
    override val isNullable: Boolean = true

    override fun copyOf(isNullable: Boolean): DataType = this

    override fun isAssignableFrom(other: DataType): Boolean {
        return true
    }
}

/**
 * @param superTypes It should contain a flattened and non-repetitive list of all super classes and interfaces
 */
data class ObjectType(val clazz: ClassDefinition, val arguments: List<DataType>, override val isNullable: Boolean = false, val superTypes: List<ObjectType>) : DataType {
    init {
        if (superTypes.distinctBy { it.name }.size != superTypes.size) {
            throw RuntimeException("superTypes of type ${clazz.fullQualifiedName} have repeated types")
        }
    }

    override val name: String = clazz.fullQualifiedName
    override val descriptiveName: String = "${name}${
        arguments.emptyToNull()?.let { "<${it.joinToString(", ") { it.descriptiveName }}>" } ?: ""
    }${if (isNullable) "?" else ""}"

    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)

    override fun isAssignableFrom(other: DataType): Boolean {
        if (other is NothingType && isNullable) return true
        if (other !is ObjectType) return false
        if (other.isNullable && !isNullable) return false
//        var otherClazz = other.clazz
//        var upwardOffset = 0
//        while (otherClazz.fullQualifiedName != clazz.fullQualifiedName && otherClazz.superClass != null) {
//            otherClazz = otherClazz.superClass!!
//            upwardOffset += 1
//        }
//        if (otherClazz.fullQualifiedName != clazz.fullQualifiedName) return false
//        if (upwardOffset == 0) { // no re-resolution is needed
//            return other.arguments == arguments
//        }
//
//        // FIXME resolving super classes with same name as type parameters using `clazz.currentScope` is incorrect
//        val genericResolverOfOther = ClassMemberResolver(other.clazz, arguments.map { it.toTypeNode() })
//        val resolutions = genericResolverOfOther.genericResolutions.let { it[it.lastIndex - upwardOffset] }
//        if (resolutions.first.fullQualifiedName != clazz.fullQualifiedName) {
//            throw RuntimeException("something wrong 1")
//        }
//        if (resolutions.second.size != arguments.size || arguments.size != clazz.typeParameters.size) {
//            throw RuntimeException("something wrong 2")
//        }
//        if (arguments.isEmpty()) return resolutions.second.isEmpty()
//
//        val scope = clazz.currentScope ?: throw RuntimeException("Scope is needed to resolve type ${other.descriptiveName}")
//        return arguments.withIndex().all {
//            val tp = clazz.typeParameters[it.index]
//            it.value == scope.assertToDataType(resolutions.second[tp.name]!!)
//        }
        var otherType: ObjectType = other
//        while (otherType.clazz.fullQualifiedName != clazz.fullQualifiedName && otherType.superType != null) {
//            otherType = otherType.superType!!
//        }
        if (otherType.clazz.fullQualifiedName != clazz.fullQualifiedName) {
            otherType = otherType.findSuperType(clazz.fullQualifiedName) ?: return false
        }
        if (otherType.clazz.fullQualifiedName != clazz.fullQualifiedName) return false
        if (otherType.arguments.size != arguments.size) throw RuntimeException("runtime type argument mismatch")
        return arguments.withIndex().all {
            it.value == StarType || it.value == otherType.arguments[it.index]
        }
//        return other is ObjectType &&
//                other.clazz.fullQualifiedName == clazz.fullQualifiedName &&
//                other.arguments == arguments &&
//                (isNullable || !other.isNullable)
    }

    // e.g. open class A; class B : A()
    // A.isConvertibleFrom(B) = true
    // B.isConvertibleFrom(A) = false
    override fun isConvertibleFrom(other: DataType): Boolean {
        if (other is NothingType && isNullable) return true
        if (other !is ObjectType) return false
        if (other.isNullable && !isNullable) return false
//        var otherClazz = other.clazz
//        var upwardOffset = 0
//        while (otherClazz.fullQualifiedName != clazz.fullQualifiedName && otherClazz.superClass != null) {
//            otherClazz = otherClazz.superClass!!
//            upwardOffset += 1
//        }
//        if (otherClazz.fullQualifiedName != clazz.fullQualifiedName) return false
//        val genericResolverOfOther = ClassMemberResolver(other.clazz, arguments.map { it.toTypeNode() })
//        val resolutions = genericResolverOfOther.genericResolutions.let { it[it.lastIndex - upwardOffset] }
//        if (resolutions.first.fullQualifiedName != clazz.fullQualifiedName) {
//            throw RuntimeException("something wrong 1")
//        }
//        if (resolutions.second.size != arguments.size || arguments.size != clazz.typeParameters.size) {
//            throw RuntimeException("something wrong 2")
//        }
//        if (arguments.isEmpty()) return resolutions.second.isEmpty()
//
//        val scope = clazz.currentScope ?: throw RuntimeException("Scope is needed to resolve type ${other.descriptiveName}")
//        return arguments.withIndex().all {
//            val tp = clazz.typeParameters[it.index]
//            it.value.isConvertibleFrom(scope.assertToDataType(resolutions.second[tp.name]!!))
//        }
        var otherType: ObjectType = other
//        while (otherType.clazz.fullQualifiedName != clazz.fullQualifiedName && otherType.superType != null) {
//            otherType = otherType.superType!!
//        }
        if (otherType.clazz.fullQualifiedName != clazz.fullQualifiedName) {
            otherType = otherType.findSuperType(clazz.fullQualifiedName) ?: return false
        }
        if (otherType.clazz.fullQualifiedName != clazz.fullQualifiedName) return false
        if (otherType.arguments.size != arguments.size) throw RuntimeException("runtime type argument mismatch")
        return arguments.withIndex().all {
            it.value.isConvertibleFrom(otherType.arguments[it.index])
        }
    }


    // e.g. open class A; class B : A()
    // A.isSubTypeOf(B) = false
    // B.isSubTypeOf(A) = true
    override fun isSubTypeOf(other: DataType): Boolean {
        if (other is AnyType && (other.isNullable || !isNullable)) return true
        if ((other !is ObjectType && other !is TypeParameterType) || other == this) return false
        return other.isConvertibleFrom(this)
    }

    fun findSuperType(typeName: String): ObjectType? {
        return superTypes.firstOrNull { it.name == typeName }
    }

    override fun toTypeNode(): TypeNode {
        return TypeNode(SourcePosition.NONE, name, arguments.map { it.toTypeNode() }.emptyToNull(), isNullable)
    }

    fun asTypeWithErasedTypeParameters(symbolTable: SymbolTable): ObjectType {
        return copy(arguments = arguments.mapIndexed { index, t ->
            val tp = clazz.typeParameters[index]
            TypeParameterType(
                name = tp.name,
                isNullable = t.isNullable,
                upperBound = symbolTable.assertToDataType(tp.typeUpperBoundOrAny())
            )
        })
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
        if (isAssignableFrom(other)) return true
        return upperBound.isAssignableFrom(other)
    }

    override fun isAssignableFrom(other: DataType): Boolean {
        if (other is NothingType && isNullable) return true
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
        if (other is NothingType && isNullable) return true
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

    override fun isConvertibleFrom(other: DataType): Boolean {
        if (other is NothingType && isNullable) return true
        if (other !is FunctionType) return false
        if (other.isNullable && !isNullable) return false

        if (returnType is UnresolvedType || other.returnType is UnresolvedType) return true

        if (!returnType.isConvertibleFrom(other.returnType)) return false
        if (arguments.size != other.arguments.size) return false
        arguments.forEachIndexed { i, _ ->
            if (!arguments[i].isConvertibleFrom(other.arguments[i])) {
                return false
            }
        }

        return true
    }

    override fun toTypeNode(): TypeNode {
        return FunctionTypeNode(
            position = SourcePosition.NONE,
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
    "Nothing" -> NothingType(isNullable = isNullable)
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
        SourcePosition.NONE,
        name,
        arguments.map { it.toTypeNode() }.emptyToNull(),
        isNullable
    )
    else -> TypeNode(
        SourcePosition.NONE,
        name,
        null,
        isNullable
    )
}

fun TypeNode.isPrimitive() =
    name in setOf("Int", "Double", "Long", "Boolean", "String", "Char", "Unit", "Nothing", "Any")
