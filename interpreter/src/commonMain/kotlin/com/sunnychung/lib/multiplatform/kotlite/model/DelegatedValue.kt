package com.sunnychung.lib.multiplatform.kotlite.model

open class DelegatedValue<T : Any>(override val value: T, fullClassName: String, clazz: ClassDefinition? = null, typeArguments: List<DataType> = emptyList(), symbolTable: SymbolTable) :
    ClassInstance(symbolTable, fullClassName = fullClassName, clazz = symbolTable.findClass(fullClassName)?.first, typeArguments = typeArguments), KotlinValueHolder<T> {
    constructor(value: T, clazz: ClassDefinition, typeArguments: List<DataType> = emptyList(), symbolTable: SymbolTable) : this(
        value = value,
        fullClassName = clazz.fullQualifiedName,
        clazz = clazz,
        typeArguments = typeArguments,
        symbolTable = symbolTable
    )
}
