package com.sunnychung.lib.multiplatform.kotlite.model

abstract class DelegatedValue<T>(val value: T, fullClassName: String, clazz: ClassDefinition? = null) :
    ClassInstance(fullClassName = fullClassName, clazz = clazz) {
    constructor(value: T, clazz: ClassDefinition) : this(value, clazz.fullQualifiedName, clazz)
}
