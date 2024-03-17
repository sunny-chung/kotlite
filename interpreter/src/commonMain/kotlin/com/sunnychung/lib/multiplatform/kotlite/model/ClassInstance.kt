package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.Interpreter
import com.sunnychung.lib.multiplatform.kotlite.extension.merge

open class ClassInstance(
    currentScope: SymbolTable,
    protected val fullClassName: String,
    clazz: ClassDefinition? = null,
    val typeArguments: List<DataType>,
    private val memberPropertyValues: MutableMap<String, RuntimeValueAccessor> = mutableMapOf(),

    /**
     * The purpose of `parentInstance` is to support type parameters in superclass and private properties (not supported now)
     * without introducing big changes and complexities to existing codebase.
     *
     * It can be flattened in the future.
     */
    val parentInstance: ClassInstance? = null,
) : RuntimeValue, ComparableRuntimeValue<Comparable<Any>> {
    internal var clazz: ClassDefinition? = null
    internal var hasInitialized: Boolean = false
    internal var typeArgumentByName: Map<String, DataType> = emptyMap()
    internal var type: DataType? = null

    init {
        if (clazz != null) {
            attach(clazz, currentScope)
        }
    }

    final override fun type(): DataType = type ?: throw RuntimeException("This object has not been initialized")

    internal fun attach(clazz: ClassDefinition, currentScope: SymbolTable) {
        if (clazz.fullQualifiedName != fullClassName) throw RuntimeException("The class to attach does not match with class name")
        if (this.clazz != null) throw RuntimeException("This object has already been initialized")
        this.clazz = clazz

        typeArgumentByName = clazz.typeParameters.mapIndexed { index, tp ->
            tp.name to typeArguments[index]
        }.toMap()

        clazz.getDeclaredPropertiesInThisClass().forEach {
            memberPropertyValues[it.key] = RuntimeValueHolder(it.value.type.resolveTypeParameter(), it.value.isMutable, null)
        }

        clazz.getDeclaredPropertyAccessorsInThisClass().forEach {
            memberPropertyValues[it.key] = RuntimeValueDelegate(
                type = clazz.findMemberProperty(it.key)!!.type.resolveTypeParameter(),
                reader = { interpreter ->
                    with(interpreter!!) {
                        val function = it.value.getter!!
                        FunctionCallNode(
                            function,
                            emptyList(),
                            emptyList(),
                            SourcePosition("", 1, 1)
                        ).evalClassMemberAnyFunctionCall(this@ClassInstance, function)
                    }
                },
                writer = { interpreter, value ->
                    with(interpreter!!) {
                        val function = it.value.setter!!
                        FunctionCallNode(
                            function,
                            listOf(
                                FunctionCallArgumentNode(
                                    SourcePosition.NONE, index = 0, value = ValueNode(
                                        SourcePosition.NONE, value
                                    )
                                )
                            ),
                            emptyList(),
                            SourcePosition("", 1, 1)
                        ).evalClassMemberAnyFunctionCall(this@ClassInstance, function)
                    }
                }
            )
        }

        type = currentScope.resolveObjectType(clazz, typeArguments.map { it.toTypeNode() }, false)

        hasInitialized = true
    }

    fun assign(interpreter: Interpreter? = null, name: String, value: RuntimeValue): Pair<Boolean, FunctionDeclarationNode?> {
        val name = clazz!!.findMemberPropertyDeclaredName(name, inThisClassOnly = true)
            ?: return parentInstance?.assign(interpreter = interpreter, name = name, value = value)
            ?: throw RuntimeException("Property $name is not defined in class ${clazz!!.name}")

        // TODO remove
        val customAccessor = clazz!!.findMemberPropertyCustomAccessor(name, inThisClassOnly = true)
        customAccessor?.setter?.let {
//            return it
            memberPropertyValues[name]!!.assign(interpreter, value)
            return true to null
        }

        val propertyDefinition = clazz!!.findMemberPropertyWithoutAccessor(name, inThisClassOnly = true)
            ?: throw RuntimeException("Property $name is not defined in class ${clazz!!.name}")
//        if (!propertyDefinition.isMutable && memberPropertyValues.containsKey(name)) {
//            throw RuntimeException("val cannot be reassigned")
//        }
        if (!propertyDefinition.type.resolveTypeParameter().isAssignableFrom(value.type())) {
            throw RuntimeException("Type ${value.type().name} cannot be casted to ${propertyDefinition.type.name}")
        }

        memberPropertyValues[name]!!.assign(interpreter, value)
        return true to null
    }

    /**
     * Return value must be either FunctionDeclarationNode (if custom getter is defined) or RuntimeValue
     */
    fun read(interpreter: Interpreter? = null, name: String): Any {
        val name = clazz!!.findMemberPropertyDeclaredName(name, inThisClassOnly = true)
            ?: return parentInstance?.read(interpreter = interpreter, name = name)
            ?: throw RuntimeException("Property $name is not defined in class ${clazz!!.name}")

        // TODO remove
        val customAccessor = clazz!!.findMemberPropertyCustomAccessor(name, inThisClassOnly = true)
        customAccessor?.getter?.let {
//            return it
            return memberPropertyValues[name]!!.read(interpreter)
        }

        val propertyDefinition = clazz!!.findMemberPropertyWithoutAccessor(name, inThisClassOnly = true)
            ?: throw RuntimeException("Property $name is not defined in class ${clazz!!.name}")

        return memberPropertyValues[name]!!.read(interpreter)
    }

    fun getPropertyHolder(name: String): RuntimeValueAccessor? {
        val name = clazz!!.findMemberPropertyDeclaredName(name, inThisClassOnly = true)
            ?: return parentInstance?.getPropertyHolder(name)
            ?: throw RuntimeException("Property $name is not declared in class ${clazz!!.name}")

        return memberPropertyValues[name]
    }

    fun findPropertyByDeclaredName(declaredName: String, interpreter: Interpreter? = null): RuntimeValue {
        return memberPropertyValues[declaredName]?.read(interpreter)
            ?: parentInstance?.findPropertyByDeclaredName(declaredName, interpreter)
            ?: throw RuntimeException("Property $declaredName is not declared in class ${clazz!!.name}")
    }

    internal fun getAllMemberProperties(): Map<String, RuntimeValueAccessor> {
        return memberPropertyValues merge (parentInstance?.getAllMemberProperties() ?: emptyMap())
    }

    /**
     * This method should not make use of parentInstance to avoid logic errors, e.g. same type parameter name resolved
     * to an unexpected type.
     */
    private fun DataType.resolveTypeParameter(): DataType {
        if (this !is TypeParameterType) return this
        return typeArgumentByName[name]?.copyOf(isNullable = isNullable) ?: TODO()
    }

    override fun compareTo(other: ComparableRuntimeValue<Comparable<Any>>): Int {
        clazz?.compareToExec?.let { executable ->
            return executable(this, other)
        }
        throw RuntimeException("Class ${clazz!!.fullQualifiedName} is not comparable")
    }

    override fun convertToString(): String = "${clazz!!.fullQualifiedName}()" // TODO

    override fun toString(): String = "${clazz!!.fullQualifiedName}($memberPropertyValues)"
}
