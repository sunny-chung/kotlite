package com.sunnychung.lib.multiplatform.kotlite.model

data class ClassInstance(val clazz: ClassDefinition, val memberPropertyValues: MutableMap<String, RuntimeValue> = mutableMapOf()) : RuntimeValue {

    override fun type(): DataType = ObjectType(clazz)

    fun assign(name: String, value: RuntimeValue) {
        // TODO check type
        val propertyDefinition = clazz.memberPropertiesByTransformedName[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")
        if (!propertyDefinition.isMutable && memberPropertyValues.containsKey(name)) {
            throw RuntimeException("val cannot be reassigned")
        }
        if (!propertyDefinition.type.isAssignableFrom(value.type())) {
            throw RuntimeException("Type ${value.type().name} cannot be casted to ${propertyDefinition.type.name}")
        }

        memberPropertyValues[name] = value
    }

    fun read(name: String): RuntimeValue {
        val propertyDefinition = clazz.memberPropertiesByTransformedName[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")

        return memberPropertyValues[name]!!
    }

    fun findPropertyByDeclaredName(declaredName: String): RuntimeValue {
        return memberPropertyValues.keys.firstOrNull { it.substring(0 ..< it.lastIndexOf('/')) == declaredName }!!
            .let { transformedName -> memberPropertyValues[transformedName]!! }
    }
}
