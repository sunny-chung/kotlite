package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter

class ClassInstance(interpreter: Interpreter, val clazz: ClassDefinition, val memberPropertyValues: MutableMap<String, RuntimeValueAccessor> = mutableMapOf()) : RuntimeValue {

    override fun type(): DataType = ObjectType(clazz)

    init {
        clazz.memberProperties.forEach {
            memberPropertyValues[it.key] = RuntimeValueHolder(it.value.type, it.value.isMutable, null)
        }

        val symbolTable = interpreter.callStack.currentSymbolTable()
        clazz.memberPropertyCustomAccessors.forEach {
            memberPropertyValues[it.key] = RuntimeValueDelegate(
                type = symbolTable.typeNodeToPropertyType(it.value.type, false)!!.type,
                reader = {
                    with(interpreter) {
                        val function = it.value.getter!!
                        FunctionCallNode(
                            function,
                            emptyList(),
                            SourcePosition(1, 1)
                        ).evalClassMemberAnyFunctionCall(this@ClassInstance, function)
                    }
                },
                writer = { value ->
                    with(interpreter) {
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
    }

    fun assign(name: String, value: RuntimeValue): FunctionDeclarationNode? {
        val name = clazz.memberTransformedNameToPropertyName[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")

        // TODO remove
        val customAccessor = clazz.memberPropertyCustomAccessors[name]
        customAccessor?.setter?.let {
//            return it
            memberPropertyValues[name]!!.assign(value)
            return null
        }

        val propertyDefinition = clazz.memberProperties[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")
//        if (!propertyDefinition.isMutable && memberPropertyValues.containsKey(name)) {
//            throw RuntimeException("val cannot be reassigned")
//        }
        if (!propertyDefinition.type.isAssignableFrom(value.type())) {
            throw RuntimeException("Type ${value.type().name} cannot be casted to ${propertyDefinition.type.name}")
        }

        memberPropertyValues[name]!!.assign(value)
        return null
    }

    /**
     * Return value must be either FunctionDeclarationNode (if custom getter is defined) or RuntimeValue
     */
    fun read(name: String): Any {
        val name = clazz.memberTransformedNameToPropertyName[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")

        // TODO remove
        val customAccessor = clazz.memberPropertyCustomAccessors[name]
        customAccessor?.getter?.let {
//            return it
            return memberPropertyValues[name]!!.read()
        }

        val propertyDefinition = clazz.memberProperties[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")

        return memberPropertyValues[name]!!.read()
    }

    fun getPropertyHolder(name: String): RuntimeValueAccessor? {
        val name = clazz.memberTransformedNameToPropertyName[name]
            ?: throw RuntimeException("Property $name is not defined in class ${clazz.name}")

        return memberPropertyValues[name]
    }

    fun findPropertyByDeclaredName(declaredName: String): RuntimeValue {
        return memberPropertyValues[declaredName]!!.read()
    }

    override fun convertToString(): String = "${clazz.fullQualifiedName}()" // TODO

    override fun toString(): String = "${clazz.fullQualifiedName}($memberPropertyValues)"
}
