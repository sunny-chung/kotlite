package com.sunnychung.lib.multiplatform.kotlite.model

abstract class PrimitiveValue(symbolTable: SymbolTable) : RuntimeValue {
    internal val rootSymbolTable = symbolTable.findScope(0)

    val clazz: ClassDefinition = type().clazz

    final override fun type(): ObjectType {
        return primitiveType(rootSymbolTable)
    }

    abstract fun primitiveType(rootSymbolTable: SymbolTable): ObjectType
}

class PrimitiveType(val typeName: PrimitiveTypeName, isNullable: Boolean, val nonNullableClass: ClassDefinition, val nullableClass: ClassDefinition, superTypes: List<ObjectType>) : ObjectType(
    clazz = if (isNullable) {
        nullableClass
    } else {
        nonNullableClass
    },
    arguments = emptyList(),
    isNullable = isNullable,
    superTypes = superTypes,
) {
    init {
        if (clazz.fullQualifiedName != "${typeName.name}${if (isNullable) "?" else ""}") {
            throw RuntimeException("PrimitiveType class is not consistent -- ${typeName.name}${if (isNullable) "?" else ""} VS ${clazz.fullQualifiedName}")
        }
    }

    fun copyPrimitive(
        isNullable: Boolean = this.isNullable,
        nonNullableClass: ClassDefinition = this.nonNullableClass,
        nullableClass: ClassDefinition = this.nullableClass,
        superTypes: List<ObjectType> = this.superTypes,
    ): PrimitiveType = PrimitiveType(
        typeName = typeName,
        isNullable = isNullable,
        nonNullableClass = nonNullableClass,
        nullableClass = nullableClass,
        superTypes = superTypes,
    )
    override fun copyOf(isNullable: Boolean) = if (this.isNullable == isNullable) this else copyPrimitive(isNullable = isNullable)
}

enum class PrimitiveTypeName {
    Int, Long, Double, Boolean, String, Char, Byte
}
