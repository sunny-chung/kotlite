package com.sunnychung.lib.multiplatform.kotlite.model

abstract class DelegatedValue<T>(override val value: T, fullClassName: String, clazz: ClassDefinition? = null, typeArguments: List<DataType> = emptyList(), symbolTable: SymbolTable) :
    ClassInstance(symbolTable, fullClassName = fullClassName, clazz = symbolTable.findClass(fullClassName)?.first, typeArguments = typeArguments), KotlinValueHolder<T> {
    constructor(value: T, clazz: ClassDefinition, typeArguments: List<DataType> = emptyList(), symbolTable: SymbolTable) : this(value, clazz.fullQualifiedName, clazz, typeArguments, symbolTable)
}
