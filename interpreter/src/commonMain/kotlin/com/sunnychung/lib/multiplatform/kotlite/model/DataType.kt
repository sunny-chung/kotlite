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
    fun isConvertibleFrom(other: DataType, isResolveTypeArguments: Boolean = true): Boolean {
        return this.isAssignableFrom(other)
    }

    fun isConvertibleTo(other: DataType): Boolean {
        return other.isConvertibleFrom(this)
    }

    fun copyOf(isNullable: Boolean): DataType

    fun toTypeNode(isResolveTypeArguments: Boolean = true) = TypeNode(SourcePosition.NONE, name, null, isNullable)

    fun `is`(type: PrimitiveTypeName, isNullable: Boolean): Boolean {
        return this is PrimitiveType && this.isNullable == isNullable && this.name == type.name
    }

    infix fun isPrimitiveTypeOf(type: PrimitiveTypeName): Boolean {
        return this is PrimitiveType && this.name == type.name
    }
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
//data class AnyType(override val isNullable: Boolean = false) : DataType {
//    override val name: String = "Any"
//
//    override fun copyOf(isNullable: Boolean): DataType = if (this.isNullable == isNullable) this else AnyType(isNullable)
//
//    override fun isAssignableFrom(other: DataType): Boolean {
//        return isNullable || !other.isNullable
//    }
//}

/**
 * TODO: Dynamic runtime class instead of constant `AnyClass.clazz` should be passed in.
 * Otherwise some states may be lost.
 */
class AnyType(isNullable: Boolean = false) : ObjectType(AnyClass.clazz, emptyList(), isNullable, emptyList()) {
    override val name: String = "Any"
    override val descriptiveName: String = "Any${if (isNullable) "?" else ""}" // if this line is absent, a strange Kotlin bug evaluates this field to be a String literal of "null"

    override fun copyOf(isNullable: Boolean): ObjectType = if (this.isNullable == isNullable) this else AnyType(isNullable)

    override fun isAssignableFrom(other: DataType): Boolean {
        return isNullable || !other.isNullable
    }

    override fun isConvertibleFrom(other: DataType, isResolveTypeArguments: Boolean): Boolean {
        return isAssignableFrom(other)
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
open class ObjectType(val clazz: ClassDefinition, val arguments: List<DataType>, override val isNullable: Boolean = false, val superTypes: List<ObjectType>) : DataType {
    init {
        if (superTypes.distinctBy { it.name }.size != superTypes.size) {
            throw RuntimeException("superTypes of type ${clazz.fullQualifiedName} have repeated types")
        }
    }

    override val name: String = clazz.fullQualifiedName.removeSuffix("?")
    override val descriptiveName: String = "${name}${
        arguments.emptyToNull()?.let { "<${it.joinToString(", ") { it.descriptiveName }}>" } ?: ""
    }${if (isNullable) "?" else ""}"

    val superTypeNames = superTypes.map { it.descriptiveName }.toSet()

    fun copy(
        clazz: ClassDefinition = this.clazz,
        arguments: List<DataType> = this.arguments,
        isNullable: Boolean = this.isNullable,
        superTypes: List<ObjectType> = this.superTypes,
    ): ObjectType {
        return ObjectType(clazz = clazz, arguments = arguments, isNullable = isNullable, superTypes = superTypes)
    }



    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)

    override fun isAssignableFrom(other: DataType): Boolean {
        if (other is NothingType && isNullable) return true
        if (other is RepeatedType) return other.actualType == null || this.isAssignableFrom(other.actualType!!)
        if (other is TypeParameterType) return this.isAssignableFrom(other.upperBound)
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
        if (otherType.clazz.fullQualifiedName.removeSuffix("?") != clazz.fullQualifiedName.removeSuffix("?")) {
            otherType = otherType.findSuperType(clazz.fullQualifiedName.removeSuffix("?")) ?: return false
        }
        if (otherType.clazz.fullQualifiedName.removeSuffix("?") != clazz.fullQualifiedName.removeSuffix("?")) return false
        if (otherType.arguments.size != arguments.size) throw RuntimeException("runtime type argument mismatch")
        return arguments.withIndex().all {
            it.value == StarType || it.value == otherType.arguments[it.index]
                || (otherType.arguments[it.index] is RepeatedType && it.value.isAssignableFrom(otherType.arguments[it.index]))
                || (it.value is TypeParameterType && (it.value as TypeParameterType).upperBound.isAssignableFrom(otherType.arguments[it.index]))
        }
//        return other is ObjectType &&
//                other.clazz.fullQualifiedName == clazz.fullQualifiedName &&
//                other.arguments == arguments &&
//                (isNullable || !other.isNullable)
    }

    // e.g. open class A; class B : A()
    // A.isConvertibleFrom(B) = true
    // B.isConvertibleFrom(A) = false
    override fun isConvertibleFrom(other: DataType, isResolveTypeArguments: Boolean): Boolean {
        if (other is NothingType && isNullable) return true
        if (other is RepeatedType) return other.actualType == null || this.isConvertibleFrom(other.actualType!!, isResolveTypeArguments = false) // this is the key
        if (other is TypeParameterType) return this.isConvertibleFrom(other.upperBound)
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
        if (otherType.clazz.fullQualifiedName.removeSuffix("?") != clazz.fullQualifiedName.removeSuffix("?")) {
            otherType = otherType.findSuperType(clazz.fullQualifiedName.removeSuffix("?")) ?: return false
        }
        if (otherType.clazz.fullQualifiedName.removeSuffix("?") != clazz.fullQualifiedName.removeSuffix("?")) return false
        if (otherType.arguments.size != arguments.size) throw RuntimeException("runtime type argument mismatch")
        if (!isResolveTypeArguments) {
            return true
        }
        return arguments.withIndex().all {
            it.value.isConvertibleFrom(otherType.arguments[it.index])
        }
    }


    // e.g. open class A; class B : A()
    // A.isSubTypeOf(B) = false
    // B.isSubTypeOf(A) = true
    override fun isSubTypeOf(other: DataType): Boolean {
        if (other is AnyType && (other.isNullable || !isNullable)) return name != "Any"
        if ((other !is ObjectType && other !is TypeParameterType) || other == this) return false
        return other.isConvertibleFrom(this)
    }

    fun findSuperType(typeName: String): ObjectType? {
        return superTypes.firstOrNull { it.name == typeName }
    }

    override fun toTypeNode(isResolveTypeArguments: Boolean): TypeNode {
        return TypeNode(
            position = SourcePosition.NONE,
            name = name,
            arguments = arguments.map {
                if (isResolveTypeArguments) {
                    it.toTypeNode()
                } else if (it is RepeatedType) {
                    TypeNode.createRepeatedTypeNode(it.realTypeDescriptiveName)
                } else {
                    TypeNode.createRepeatedTypeNode(it.nameWithNullable)
                }
            }.emptyToNull(),
            isNullable = isNullable
        )
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is RepeatedType) {
            return if (other.actualType != null) {
                equals(other.actualType)
            } else {
                other.realTypeDescriptiveName == descriptiveName
            }
        }
        if (other !is ObjectType) return false

        if (clazz != other.clazz) return false
        if (arguments != other.arguments) return false
        if (isNullable != other.isNullable) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazz.hashCode()
        result = 31 * result + arguments.hashCode()
        result = 31 * result + isNullable.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String {
        return "ObjectType($descriptiveName)"
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

data class RepeatedType(val realTypeDescriptiveName: String, override val isNullable: Boolean, var actualType: DataType? = null) : DataType {
    override val name: String = actualType?.name ?: "<Repeated<$realTypeDescriptiveName>>"
    override fun copyOf(isNullable: Boolean) = copy(isNullable = isNullable)

    override fun isAssignableFrom(other: DataType): Boolean {
        if (actualType == null) {
            return realTypeDescriptiveName == other.descriptiveName
        }
        val other = if (other is RepeatedType) other.actualTypeOrAny() else other
        return actualType!!.isAssignableFrom(other)
    }

    fun actualTypeOrAny(): DataType = actualType ?: AnyType(isNullable = isNullable)

    override fun isConvertibleFrom(other: DataType, isResolveTypeArguments: Boolean): Boolean {
        if (actualType == null) return true
        val other = if (other is RepeatedType) {
            other.actualTypeOrAny()
        } else {
            other
        }
        return actualType!!.isConvertibleFrom(other, isResolveTypeArguments = false)

//        if (other is RepeatedType) {
//            return realTypeDescriptiveName == other.realTypeDescriptiveName
//        }
//        return other.descriptiveName == realTypeDescriptiveName ||
//                (other is ObjectType && name in other.superTypeNames)
    }

    override fun toTypeNode(isResolveTypeArguments: Boolean): TypeNode {
        actualType?.toTypeNode(isResolveTypeArguments = false)?.let { return it }
        return TypeNode(SourcePosition.NONE, "<Repeated>", listOf(TypeNode(SourcePosition.NONE, realTypeDescriptiveName, null, false)), isNullable)
    }

    // override to avoid infinite loop for cases like `T : Comparable<T>`
    override fun equals(other: Any?): Boolean {
        if (other is RepeatedType) {
            return true
        }
        return if (actualType != null) {
            actualType == other
        } else if (other is DataType) {
            realTypeDescriptiveName == other.descriptiveName
        } else {
            false
        }
    }

    // override to avoid infinite loop for cases like `T : Comparable<T>`
    override fun hashCode(): Int {
        var result = realTypeDescriptiveName.hashCode()
        result = 31 * result + isNullable.hashCode()
        return result
    }
}

data class TypeParameterType(
    override val name: String,
    override val isNullable: Boolean = false,
    val upperBound: DataType
) : DataType {
    override val nameWithNullable: String
        get() = "<$name>${if (isNullable) "?" else ""}"

    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copy(isNullable = isNullable)

    override fun isConvertibleFrom(other: DataType, isResolveTypeArguments: Boolean): Boolean {
        if (isAssignableFrom(other)) return true
        return upperBound.isConvertibleFrom(other)
    }

    override fun isAssignableFrom(other: DataType): Boolean {
        if (other is NothingType && isNullable) return true
        if (other is RepeatedType) {
            return other.realTypeDescriptiveName == "$name${if (isNullable) "?" else ""}"
        }
        return other is TypeParameterType &&
                other.name == name &&
                (isNullable || !other.isNullable) &&
                other.upperBound == upperBound
    }
}

data class FunctionType(val arguments: List<DataType>, val returnType: DataType, override val isNullable: Boolean, val receiverType: DataType? = null) : DataType {
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

    override fun isConvertibleFrom(other: DataType, isResolveTypeArguments: Boolean): Boolean {
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

    override fun toTypeNode(isResolveTypeArguments: Boolean): TypeNode {
        return FunctionTypeNode(
            position = SourcePosition.NONE,
            parameterTypes = arguments.map {
                if (isResolveTypeArguments) {
                    it.toTypeNode()
                } else if (it is RepeatedType) {
                    TypeNode.createRepeatedTypeNode(it.realTypeDescriptiveName)
                } else {
                    TypeNode.createRepeatedTypeNode(it.nameWithNullable)
                }
            }, // parameterTypes should be non-null, as type inference does not take place here
            returnType = returnType.toTypeNode(),
            isNullable = isNullable,
        )
    }
}

fun TypeNode.toPrimitiveDataType(symbolTable: SymbolTable) = when(this.name) {
    "Int" -> if (isNullable) symbolTable.NullableIntType else symbolTable.IntType
    "Long" -> if (isNullable) symbolTable.NullableLongType else symbolTable.LongType
    "Double" -> if (isNullable) symbolTable.NullableDoubleType else symbolTable.DoubleType
    "Boolean" -> if (isNullable) symbolTable.NullableBooleanType else symbolTable.BooleanType
    "String" -> if (isNullable) symbolTable.NullableStringType else symbolTable.StringType
    "Char" -> if (isNullable) symbolTable.NullableCharType else symbolTable.CharType
    "Byte" -> if (isNullable) symbolTable.NullableByteType else symbolTable.ByteType
    "Unit" -> UnitType(isNullable = isNullable)
    "Any" -> AnyType(isNullable = isNullable)
    "Nothing" -> NothingType(isNullable = isNullable)
    else -> null //ObjectType(clazz = clazz!!, isNullable = isNullable)
}

fun DataType.isNonNullNumberType() = when ((this as? PrimitiveType)?.typeName) {
    PrimitiveTypeName.Double, PrimitiveTypeName.Int, PrimitiveTypeName.Long -> !isNullable
    else -> false
}

fun DataType.isNonNullIntegralType() = when ((this as? PrimitiveType)?.typeName) {
    PrimitiveTypeName.Int, PrimitiveTypeName.Long -> !isNullable
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

fun TypeNode.isPrimitiveWithValue() =
    name in setOf("Int", "Double", "Long", "Boolean", "String", "Char")

fun TypeNode.isPrimitive() =
    name in setOf("Int", "Double", "Long", "Boolean", "String", "Char", "Unit", "Nothing", "Any")
