package com.sunnychung.lib.multiplatform.kotlite.model

abstract class DelegatedValue<T>(val value: T, fullClassName: String, clazz: ClassDefinition? = null, typeArguments: List<DataType> = emptyList()) :
    ClassInstance(fullClassName = fullClassName, clazz = clazz, typeArguments = typeArguments) {
    constructor(value: T, clazz: ClassDefinition, typeArguments: List<DataType> = emptyList()) : this(value, clazz.fullQualifiedName, clazz, typeArguments)
}
