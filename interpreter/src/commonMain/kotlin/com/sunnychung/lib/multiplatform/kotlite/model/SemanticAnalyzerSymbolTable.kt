package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.extension.resolveGenericParameterTypeToUpperBound
import com.sunnychung.lib.multiplatform.kotlite.log
import com.sunnychung.lib.multiplatform.kotlite.util.ClassMemberResolver

class SemanticAnalyzerSymbolTable(
    scopeLevel: Int,
    scopeName: String,
    scopeType: ScopeType,
    parentScope: SymbolTable?,
    returnType: DataType? = null,
) : SymbolTable(
    scopeLevel = scopeLevel,
    scopeName = scopeName,
    scopeType = scopeType,
    parentScope = parentScope,
    returnType = returnType
) {
    /**
     * For resolving type parameters in function declarations only
     */
    private val tempTypeAlias = mutableListOf<Map<String, DataType>>()

    fun TypeNode.toClass(): ClassDefinition {
        return (findClass(name) ?: throw RuntimeException("Could not find class `$name`"))
            .first
    }

    private fun declareTempTypeAlias(typeAliasAndUpperBounds: List<Pair<String, TypeNode>>) {
        val alias = mutableMapOf<String, DataType>()
        // update tempTypeAlias first, because other type parameters may depend on previous type parameters in the same event
        // e.g. `<T, L : List<T>>`
        tempTypeAlias += alias
        typeAliasAndUpperBounds.forEach {
            alias[it.first] = assertToDataType(it.second)
        }
    }

    private fun popTempTypeAlias() {
        tempTypeAlias.removeLast()
    }

    override fun findTypeAlias(name: String): Pair<DataType, SymbolTable>? {
        tempTypeAlias.indices.reversed().forEach {  index ->
            tempTypeAlias[index][name]?.let { return it to this }
        }
        return super.findTypeAlias(name)
    }

    override fun functionNameTransform(name: String, function: FunctionDeclarationNode) = function.toSignature(this)

    fun findFunctionsByOriginalName(originalName: String, isThisScopeOnly: Boolean = false): List<Pair<FunctionDeclarationNode, SymbolTable>> {
        return functionDeclarations.filter { it.value.name == originalName }
            .map { it.value to this } +
                (Unit.takeIf { !isThisScopeOnly }?.let {
                    (parentScope as? SemanticAnalyzerSymbolTable)?.findFunctionsByOriginalName(originalName, isThisScopeOnly)
                } ?: emptyList())
    }

    // only use in semantic analyzer
    private fun findAllMatchingCallables(currentSymbolTable: SymbolTable, originalName: String, receiverClass: ClassDefinition?, receiverType: DataType?, arguments: List<FunctionCallArgumentInfo>, modifierFilter: SearchFunctionModifier): List<FindCallableResult> {
        var thisScopeCandidates = mutableListOf<FindCallableResult>()
        if (receiverClass == null) {
            if (modifierFilter != SearchFunctionModifier.ConstructorOnly) findFunctionsByOriginalName(originalName, isThisScopeOnly = true).map {
                val owner = findFunctionOwner(functionNameTransform(originalName, it.first))
                FindCallableResult(
                    transformedName = it.first.transformedRefName!!,
                    owner = owner,
                    type = if (owner == null) CallableType.Function else CallableType.ClassMemberFunction,
                    isVararg = it.first.isVararg,
                    arguments = it.first.valueParameters,
                    typeParameters = it.first.typeParameters,
                    receiverType = null,
                    returnType = it.first.returnType,
                    signature = it.first.toSignature(this),
                    definition = it.first,
                    scope = this
                )
            }.let { thisScopeCandidates += it }
            findClass(originalName, isThisScopeOnly = true)?.let {
                thisScopeCandidates += FindCallableResult(
                    transformedName = it.first.fullQualifiedName,
                    owner = null,
                    type = CallableType.Constructor,
                    isVararg = false,
                    arguments = it.first.primaryConstructor?.parameters?.map { it.parameter } ?: emptyList(),
                    typeParameters = it.first.typeParameters,
                    receiverType = null,
                    returnType = TypeNode(SourcePosition.NONE, it.first.fullQualifiedName, null, false),
                    signature = it.first.fullQualifiedName,
                    definition = it.first,
                    scope = this
                )
            }
            if (modifierFilter != SearchFunctionModifier.ConstructorOnly) getPropertyTypeOrNull(originalName, isThisScopeOnly = true)?.let {
                if (it.first.type !is FunctionType || it.first.type.isNullable) {
                    return@let
                }
                val transformedName = "$originalName/${this.scopeLevel}"
                val owner = findPropertyOwner(transformedName)?.ownerRefName
                thisScopeCandidates += FindCallableResult(
                    transformedName = transformedName,
                    owner = owner,
                    type = CallableType.Property,
                    isVararg = false,
                    arguments = (it.first.type as FunctionType).arguments,
                    typeParameters = emptyList(),
                    receiverType = null,
                    returnType = (it.first.type as FunctionType).returnType.toTypeNode(),
                    signature = "$owner//$transformedName",
                    definition = it.first,
                    scope = this
                )
            }
        } else if (modifierFilter != SearchFunctionModifier.ConstructorOnly) {
//            receiverClass.findMemberFunctionsByDeclaredName(originalName).map {
//                val it = it.value
//                FindCallableResult(
//                    transformedName = it.transformedRefName!!,
//                    owner = null,
//                    type = CallableType.ClassMemberFunction,
//                    isVararg = it.isVararg,
//                    arguments = it.valueParameters,
//                    typeParameters = it.typeParameters,
//                    receiverType = it.receiver,
//                    returnType = it.returnType,
//                    definition = it,
//                    scope = this
//                )
//            }.let { thisScopeCandidates += it }
            ClassMemberResolver(receiverClass, null).findMemberFunctionsAndTypeUpperBoundsByDeclaredName(originalName).map { lookup ->
                val it = lookup.value.function
                // TODO this is slow, O(n^2). optimize this
                ClassMemberResolver(
                    receiverClass,
                    (receiverType as ObjectType).arguments.map { it.toTypeNode() }
                ).findMemberFunctionWithIndexByTransformedNameLinearSearch(it.transformedRefName!!).let { lookup2 ->
                    FindCallableResult(
                        transformedName = it.transformedRefName!!,
                        owner = null,
                        type = CallableType.ClassMemberFunction,
                        isVararg = it.isVararg,
                        arguments = lookup2!!.resolvedValueParameterTypes,
                        typeParameters = it.typeParameters,
                        receiverType = it.receiver ?: receiverType.toTypeNode(),
                        returnType = lookup2!!.resolvedReturnType,
                        signature = it.toSignature(this),
                        definition = it,
                        scope = this
                    )
                }
            }.let { thisScopeCandidates += it }

            findExtensionFunctionsIncludingSuperClasses(receiverType!!, originalName, isThisScopeOnly = true).map {
                FindCallableResult(
                    transformedName = it.function.transformedRefName!!,
                    owner = null,
                    type = CallableType.ExtensionFunction,
                    isVararg = it.function.isVararg,
                    arguments = it.function.valueParameters,
                    typeParameters = it.function.typeParameters,
                    receiverType = it.function.receiver, //it.resolvedReceiverType.toTypeNode(), //it.function.receiver,
                    returnType = it.function.returnType,
                    signature = it.function.toSignature(this),
                    definition = it.function,
                    scope = this
                )
            }.let { thisScopeCandidates += it }
        }
        thisScopeCandidates = thisScopeCandidates
            .filter { callable ->
                when (modifierFilter) {
                    SearchFunctionModifier.OperatorFunctionOnly -> {
                        if (callable.definition is FunctionDeclarationNode) {
                            callable.definition.modifiers.contains(FunctionModifier.operator)
                        } else {
                            false
                        }
                    }

                    SearchFunctionModifier.NoRestriction, SearchFunctionModifier.ConstructorOnly -> true
                }
            }
            .filter { callable ->
                declareTempTypeAlias(callable.typeParameters.map {
                    it.name to it.typeUpperBoundOrAny()
                })
                try {
                    if (callable.isVararg) {
                        val functionArgType = currentSymbolTable.typeNodeToDataType(
                            (callable.arguments.first() as FunctionValueParameterNode).type.resolveGenericParameterTypeToUpperBound(
                                callable.typeParameters + (receiverClass?.typeParameters ?: emptyList())
                            )
                        )!!
                        return@filter arguments.all { functionArgType.isConvertibleFrom(it.type) }
                    }

                    if (callable.arguments.isEmpty()) {
                        return@filter arguments.isEmpty()
                    }
                    when (callable.arguments.first()) {
                        is DataType /* is a lambda */ -> return@filter arguments.size == callable.arguments.size &&
                                arguments.all { it.name == null } &&
                                callable.arguments.foldIndexed(true) { i, acc, it -> acc && (it as DataType).isAssignableFrom(arguments[i].type) }
                        is FunctionValueParameterNode -> {
                            if (arguments.size > callable.arguments.size) return@filter false
                            val argumentsReordered = arrayOfNulls<FunctionCallArgumentInfo>(callable.arguments.size)
                            val typeParameterMapping = mutableMapOf<String, DataType>()
                            arguments.forEachIndexed { i, arg ->
                                val newIndex = if (arg.name == null) {
                                    if (i == arguments.lastIndex && arg.type is FunctionType) {
                                        callable.arguments.lastIndex
                                    } else {
                                        i
                                    }
                                } else {
                                    val findIndex = callable.arguments.indexOfFirst { (it as FunctionValueParameterNode).name == arg.name }
                                    if (findIndex < 0) {
                                        return@filter false
                                    }
                                    findIndex
                                }
                                argumentsReordered[newIndex] = arg
                            }
                            callable.arguments.foldIndexed(true) { i, acc, it ->
                                val functionArg = it as FunctionValueParameterNode
                                val callArg = argumentsReordered[i]
                                acc && if (callArg == null) {
                                    functionArg.defaultValue != null
                                } else {
                                    currentSymbolTable.assertToDataType(functionArg.type.resolveGenericParameterTypeToUpperBound(callable.typeParameters + (receiverClass?.typeParameters ?: emptyList()) )).isConvertibleFrom(callArg.type)
                                    // TODO filter whether same type parameter always map to same argument
                                }
                            }
                        }
                        else -> throw UnsupportedOperationException()
                    }
                } finally {
                    popTempTypeAlias()
                }
            }.toMutableList()
        return thisScopeCandidates + ((parentScope as? SemanticAnalyzerSymbolTable)?.findAllMatchingCallables(currentSymbolTable, originalName, receiverClass, receiverType, arguments, modifierFilter) ?: emptyList())
    }

    fun assertToDataTypeWithTypeParameters(type: TypeNode, typeParameters: List<TypeParameterNode>): DataType {
        declareTempTypeAlias(typeParameters.map {
            it.name to it.typeUpperBoundOrAny()
        })
        try {
            return assertToDataType(type)
        } finally {
            popTempTypeAlias()
        }
    }

    // only use in semantic analyzer
    // this operation is expensive
    fun findMatchingCallables(currentSymbolTable: SymbolTable, originalName: String, receiverType: DataType?, arguments: List<FunctionCallArgumentInfo>, modifierFilter: SearchFunctionModifier): List<FindCallableResult> {
        val receiverClass = receiverType?.let { (findClass(it.nameWithNullable) ?: throw RuntimeException("Class ${it.nameWithNullable} not found")).first }
        return findAllMatchingCallables(currentSymbolTable, originalName, receiverClass, receiverType, arguments, modifierFilter)
            .distinctBy { it.definition }
            .let { callables -> // subclass callables override superclass
                callables.filterNot { callable ->
                    if (callable.receiverType != null) {
                        val callableReceiverType = assertToDataTypeWithTypeParameters(callable.receiverType, callable.typeParameters)
                        callables.any { it.receiverType != null && assertToDataTypeWithTypeParameters(it.receiverType, it.typeParameters).isSubTypeOf(callableReceiverType) }
                    } else {
                        false
                    }
                }
            }
            .let { callables -> // class member functions override extension methods
                callables.filterNot { callable ->
                    if (callable.receiverType != null && callable.type == CallableType.ExtensionFunction) {
                        val callableReceiverType = assertToDataTypeWithTypeParameters(callable.receiverType, callable.typeParameters)
                        callables.any { it.receiverType != null && assertToDataTypeWithTypeParameters(it.receiverType, it.typeParameters) == callableReceiverType && it.type == CallableType.ClassMemberFunction }
                    } else {
                        false
                    }
                }
            }
            .also { log.v { "Matching functions:\n${it.joinToString("\n")}" } }
    }

    fun findExtensionFunctionsIncludingSuperClasses(receiverType: DataType, functionName: String, isThisScopeOnly: Boolean = false): List<ExtensionFunctionLookupResult> {
        val result = mutableListOf<ExtensionFunctionLookupResult>()

        var type: DataType? = receiverType
        val resolver = (type as? ObjectType)?.let { ClassMemberResolver(it.clazz, it.arguments.map { it.toTypeNode() }) }
        var classTreeIndex = (type as? ObjectType)?.clazz?.index ?: 0
        while (type != null) {
            findExtensionFunctions(type, functionName, isThisScopeOnly)
                .let { lookups ->
                    result.addAll(lookups.map {
                        ExtensionFunctionLookupResult(
                            function = it.first,
                            resolvedReceiverType = type!!,
                            symbolTable = it.second,
                        )
                    })
                }

//            type = (type as? ObjectType)?.clazz?.superClass?.let {
//                val typeResolutions = resolver!!.genericResolutions[it.index].second
//                ObjectType(it, it.typeParameters.map {
//                    assertToDataType(typeResolutions[it.name]!!)
//                })
//            }
            type = (type as? ObjectType)?.superType
        }

        return result
    }

    fun findExtensionFunctions(receiverType: DataType, functionName: String, isThisScopeOnly: Boolean = false): List<Pair<FunctionDeclarationNode, SymbolTable>> {
        return extensionFunctionDeclarations.values.filter { func ->
            (func.receiver?.let {
                if (
                    !(receiverType.name == "Nothing" && receiverType.isNullable && it.isNullable) &&
                    (it.name != receiverType.name || it.isNullable != receiverType.isNullable)
                ) return@let false
                // at here, class name and nullability matched, or receiver is null and nullability matched
                val objectTypeReceiver = receiverType as? ObjectType
                if (it.arguments.isNullOrEmpty() && (objectTypeReceiver == null || objectTypeReceiver.arguments.isEmpty())) {
                    return@let true
                }
                // at here, function receiver type has some type parameter, so receiverType can only be class instance
                if (objectTypeReceiver == null) {
                    throw RuntimeException("Receiver must be a class instance")
                }
                if (it.arguments!!.size != objectTypeReceiver.arguments.size) {
                    throw RuntimeException("Number of type arguments mismatch")
                }
                declareTempTypeAlias(func.typeParameters.map {
                    it.name to it.typeUpperBoundOrAny()
                })
                try {
                    it.arguments.forEachIndexed { index, argType ->
                        if (argType.name == "*") return@forEachIndexed
                        val typeParameter = func.typeParameters.firstOrNull { it.name == argType.name }
                        val functionTypeParameterType = if (typeParameter != null) {
                            typeParameter.typeUpperBound ?: TypeNode(SourcePosition.NONE, "Any", null, true)
                        } else {
                            argType
                        }.let { typeNodeToDataType(it) } // TODO generic type
                            ?: throw SemanticException(argType.position, "Unknown type ${argType.descriptiveName()}")
                        if (!functionTypeParameterType.isConvertibleFrom(objectTypeReceiver.arguments[index])) {
                            return@let false // type mismatch
                        }
                    }
                } finally {
                    popTempTypeAlias()
                }
                true
            } ?: false) && func.name == functionName
        }.map {
            it to this
        } + (Unit.takeIf { !isThisScopeOnly }?.let {
            (parentScope as? SemanticAnalyzerSymbolTable)
                ?.findExtensionFunctions(receiverType, functionName, isThisScopeOnly)
        } ?: emptyList())
    }

    fun findExtensionPropertyByDeclarationIncludingSuperClasses(resolvedReceiver: TypeNode, declaredName: String, isThisScopeOnly: Boolean = false): Pair<String, ExtensionProperty>? {
        var receiverType: DataType? = assertToDataType(resolvedReceiver)
        while (receiverType != null) {
            findExtensionPropertyByDeclaration(receiverType.toTypeNode(), declaredName)?.let {
                return it
            }
            receiverType = (receiverType as? ObjectType)?.superType
        }
        return null
    }

    fun DataType.toTypeNode(): TypeNode =
        if (this is NothingType) {
            TypeNode(SourcePosition.NONE, "Nothing", null, true)
        } else if (this !is ObjectType && this !is FunctionType) {
            TypeNode(SourcePosition.NONE, name, null, isNullable)
        } else if (this is FunctionType) {
            FunctionTypeNode(
                position = SourcePosition.NONE,
                parameterTypes = arguments.map { it.toTypeNode() },
                returnType = returnType.toTypeNode(),
                isNullable = isNullable,
            )
        } else {
            TypeNode(SourcePosition.NONE, name, null, isNullable)
        }
}

fun FunctionDeclarationNode.toSignature(symbolTable: SemanticAnalyzerSymbolTable): String {
    with (symbolTable) {
        val typeParameters = (symbolTable.listTypeAliasInAllScopes() + typeParameters /* typeParameters has higher precedence */)
            .associateBy { it.name }
        return (receiver?.let { "${it.descriptiveName()}/" } ?: "") + name + "//" + valueParameters.joinToString("/") {
            if (typeParameters.containsKey(it.type.name)) {
                val typeUpperBound = typeParameters[it.type.name]!!.typeUpperBound
                typeUpperBound?.toClass()?.fullQualifiedName ?: "Any?"
            } else {
                it.type
                    .let { findClass(it.name)?.first }
                    ?.fullQualifiedName
                    ?: it.type.descriptiveName() // TODO this is dirty. any way to get upper bound type name?
            }
        }
    }
}

enum class SearchFunctionModifier {
    OperatorFunctionOnly,
    ConstructorOnly,
    NoRestriction,
}

data class ExtensionFunctionLookupResult(
    val function: FunctionDeclarationNode,
    val resolvedReceiverType: DataType,
    val symbolTable: SymbolTable,
)
