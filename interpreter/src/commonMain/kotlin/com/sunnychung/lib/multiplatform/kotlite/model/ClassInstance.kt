package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter

open class ClassInstance(protected val fullClassName: String, clazz: ClassDefinition? = null, val memberPropertyValues: MutableMap<String, RuntimeValueAccessor> = mutableMapOf()) : RuntimeValue {
    internal var clazz: ClassDefinition? = null
    internal var hasInitialized: Boolean = false

    init {
        if (clazz != null) {
            attach(clazz)
        }
    }

    final override fun type(): DataType = ObjectType(clazz ?: throw RuntimeException("This object has not been initialized"))

    internal fun attach(clazz: ClassDefinition) {
        if (clazz.fullQualifiedName != fullClassName) throw RuntimeException("The class to attach does not match with class name")
        if (this.clazz != null) throw RuntimeException("This object has already been initialized")
        this.clazz = clazz

        clazz.memberProperties.forEach {
            memberPropertyValues[it.key] = RuntimeValueHolder(it.value.type, it.value.isMutable, null)
        }

        clazz.memberPropertyCustomAccessors.forEach {
            memberPropertyValues[it.key] = RuntimeValueDelegate(
                type = clazz.memberPropertyTypes[it.key]!!.type,
                reader = { interpreter ->
                    with(interpreter!!) {
                        val function = it.value.getter!!
                        FunctionCallNode(
                            function,
                            emptyList(),
                            SourcePosition(1, 1)
                        ).evalClassMemberAnyFunctionCall(this@ClassInstance, function)
                    }
                },
                writer = { interpreter, value ->
                    with(interpreter!!) {
                        val function = it.value.setter!!
                        FunctionCallNode(
                            function,
                            listOf(FunctionCallArgumentNode(index = 0, value = ValueNode(value))),
                            SourcePosition(1, 1)
                        ).evalClassMemberAnyFunctionCall(this@ClassInstance, function)
                    }
                }
            )
        }

        hasInitialized = true
    }

    fun assign(interpreter: Interpreter? = null, name: String, value: RuntimeValue): FunctionDeclarationNode? {
        val name = clazz!!.memberTransformedNameToPropertyName[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz!!.name}")

        // TODO remove
        val customAccessor = clazz!!.memberPropertyCustomAccessors[name]
        customAccessor?.setter?.let {
//            return it
            memberPropertyValues[name]!!.assign(interpreter, value)
            return null
        }

        val propertyDefinition = clazz!!.memberProperties[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz!!.name}")
//        if (!propertyDefinition.isMutable && memberPropertyValues.containsKey(name)) {
//            throw RuntimeException("val cannot be reassigned")
//        }
        if (!propertyDefinition.type.isAssignableFrom(value.type())) {
            throw RuntimeException("Type ${value.type().name} cannot be casted to ${propertyDefinition.type.name}")
        }

        memberPropertyValues[name]!!.assign(interpreter, value)
        return null
    }

    /**
     * Return value must be either FunctionDeclarationNode (if custom getter is defined) or RuntimeValue
     */
    fun read(interpreter: Interpreter? = null, name: String): Any {
        val name = clazz!!.memberTransformedNameToPropertyName[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz!!.name}")

        // TODO remove
        val customAccessor = clazz!!.memberPropertyCustomAccessors[name]
        customAccessor?.getter?.let {
//            return it
            return memberPropertyValues[name]!!.read(interpreter)
        }

        val propertyDefinition = clazz!!.memberProperties[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz!!.name}")

        return memberPropertyValues[name]!!.read(interpreter)
    }

    fun getPropertyHolder(name: String): RuntimeValueAccessor? {
        val name = clazz!!.memberTransformedNameToPropertyName[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz!!.name}")

        return memberPropertyValues[name]
    }

    fun findPropertyByDeclaredName(declaredName: String, interpreter: Interpreter? = null): RuntimeValue {
        return memberPropertyValues[declaredName]!!.read(interpreter)
    }

    override fun convertToString(): String = "${clazz!!.fullQualifiedName}()" // TODO

    override fun toString(): String = "${clazz!!.fullQualifiedName}($memberPropertyValues)"
}
