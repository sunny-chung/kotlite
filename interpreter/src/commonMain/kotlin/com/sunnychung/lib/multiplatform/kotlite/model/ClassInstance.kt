package com.sunnychung.lib.multiplatform.kotlite.model

data class ClassInstance(val clazz: ClassDefinition, val memberPropertyValues: MutableMap<String, RuntimeValue> = mutableMapOf()) : RuntimeValue {

    override fun type(): DataType = ObjectType(clazz)

    fun assign(name: String, value: RuntimeValue): FunctionDeclarationNode? {
        val name = clazz.memberTransformedNameToPropertyName[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")

        val customAccessor = clazz.memberPropertyCustomAccessors[name]
        customAccessor?.setter?.let {
            return it
        }

        val propertyDefinition = clazz.memberProperties[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")
        if (!propertyDefinition.isMutable && memberPropertyValues.containsKey(name)) {
            throw RuntimeException("val cannot be reassigned")
        }
        if (!propertyDefinition.type.isAssignableFrom(value.type())) {
            throw RuntimeException("Type ${value.type().name} cannot be casted to ${propertyDefinition.type.name}")
        }

        memberPropertyValues[name] = value
        return null
    }

    /**
     * Return value must be either FunctionDeclarationNode (if custom getter is defined) or RuntimeValue
     */
    fun read(name: String): Any {
        val name = clazz.memberTransformedNameToPropertyName[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")

        val customAccessor = clazz.memberPropertyCustomAccessors[name]
        customAccessor?.getter?.let {
            return it
        }

        val propertyDefinition = clazz.memberProperties[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")

        return memberPropertyValues[name]!!
    }

    fun findPropertyByDeclaredName(declaredName: String): RuntimeValue {
        return memberPropertyValues[declaredName]!!
    }
}
