package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.log

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

    fun TypeNode.toClass(): ClassDefinition {
        return (findClass(name) ?: throw RuntimeException("Could not find class `$name`"))
            .first
    }

    override fun functionNameTransform(name: String, function: FunctionDeclarationNode) = function.toSignature(this)

    fun findFunctionsByOriginalName(originalName: String, isThisScopeOnly: Boolean = false): List<Pair<FunctionDeclarationNode, SymbolTable>> {
        return functionDeclarations.filter { it.value.name == originalName }
            .map { it.value to this } +
                (Unit.takeIf { !isThisScopeOnly }?.let {
                    (parentScope as? SemanticAnalyzerSymbolTable)?.findFunctionsByOriginalName(originalName, isThisScopeOnly)
                } ?: emptyList())
    }

    fun findExtensionFunctionsByOriginalName(receiver: String, originalName: String, isThisScopeOnly: Boolean = false): List<Pair<FunctionDeclarationNode, SymbolTable>> {
        return extensionFunctionDeclarations.filter { it.value.receiver == receiver && it.value.name == originalName }
            .map { it.value to this } +
                (Unit.takeIf { !isThisScopeOnly }?.let {
                    (parentScope as? SemanticAnalyzerSymbolTable)?.findFunctionsByOriginalName(originalName, isThisScopeOnly)
                } ?: emptyList())
    }

    // only use in semantic analyzer
    private fun findAllMatchingCallables(currentSymbolTable: SymbolTable, originalName: String, receiverClass: ClassDefinition?, arguments: List<FunctionCallArgumentInfo>): List<FindCallableResult> {
        var thisScopeCandidates = mutableListOf<FindCallableResult>()
        if (receiverClass == null) {
            findFunctionsByOriginalName(originalName, isThisScopeOnly = true).map {
                val owner = findFunctionOwner(functionNameTransform(originalName, it.first))
                FindCallableResult(
                    transformedName = it.first.transformedRefName!!,
                    owner = owner,
                    type = if (owner == null) CallableType.Function else CallableType.ClassMemberFunction,
                    arguments = it.first.valueParameters,
                    returnType = it.first.returnType,
                    definition = it.first,
                    scope = this
                )
            }.let { thisScopeCandidates += it }
            findClass(originalName, isThisScopeOnly = true)?.let {
                thisScopeCandidates += FindCallableResult(
                    transformedName = it.first.fullQualifiedName,
                    owner = null,
                    type = CallableType.Constructor,
                    arguments = it.first.primaryConstructor?.parameters?.map { it.parameter } ?: emptyList(),
                    returnType = TypeNode(it.first.fullQualifiedName, null, false),
                    definition = it.first,
                    scope = this
                )
            }
            getPropertyTypeOrNull(originalName, isThisScopeOnly = true)?.let {
                if (it.first.type !is FunctionType) {
                    return@let
                }
                val transformedName = "$originalName/${this.scopeLevel}"
                thisScopeCandidates += FindCallableResult(
                    transformedName = transformedName,
                    owner = findPropertyOwner(transformedName),
                    type = CallableType.Property,
                    arguments = (it.first.type as FunctionType).arguments,
                    returnType = (it.first.type as FunctionType).returnType.toTypeNode(),
                    definition = it.first,
                    scope = this
                )
            }
        } else {
            receiverClass.memberFunctions.filter {
                it.value.name == originalName
            }.map {
                val it = it.value
                FindCallableResult(
                    transformedName = it.transformedRefName!!,
                    owner = null,
                    type = CallableType.ClassMemberFunction,
                    arguments = it.valueParameters,
                    returnType = it.returnType,
                    definition = it,
                    scope = this
                )
            }.let { thisScopeCandidates += it }
            findExtensionFunctionsByOriginalName(receiverClass.fullQualifiedName, originalName, isThisScopeOnly = true).map {
                FindCallableResult(
                    transformedName = it.first.transformedRefName!!,
                    owner = null,
                    type = CallableType.ExtensionFunction,
                    arguments = it.first.valueParameters,
                    returnType = it.first.returnType,
                    definition = it.first,
                    scope = this
                )
            }.let { thisScopeCandidates += it }
        }
        thisScopeCandidates = thisScopeCandidates.filter { callable ->
            if (callable.arguments.isEmpty()) {
                return@filter arguments.isEmpty()
            }
            when (callable.arguments.first()) {
                is DataType -> return@filter arguments.size == callable.arguments.size &&
                        arguments.all { it.name == null } &&
                        callable.arguments.foldIndexed(true) { i, acc, it -> acc && (it as DataType).isAssignableFrom(arguments[i].type) }
                is FunctionValueParameterNode -> {
                    if (arguments.size > callable.arguments.size) return@filter false
                    val argumentsReordered = arrayOfNulls<FunctionCallArgumentInfo>(callable.arguments.size)
                    arguments.forEachIndexed { i, arg ->
                        if (arg.name == null) {
                            argumentsReordered[i] = arg
                        } else {
                            val findIndex = callable.arguments.indexOfFirst { (it as FunctionValueParameterNode).name == arg.name }
                            if (findIndex < 0) {
                                return@filter false
                            }
                            argumentsReordered[findIndex] = arg
                        }
                    }
                    callable.arguments.foldIndexed(true) { i, acc, it ->
                        val functionArg = it as FunctionValueParameterNode
                        val callArg = argumentsReordered[i]
                        acc && if (callArg == null) {
                            functionArg.defaultValue != null
                        } else {
                            currentSymbolTable.typeNodeToDataType(functionArg.type)!!.isAssignableFrom(callArg.type)
                        }
                    }
                }
                else -> throw UnsupportedOperationException()
            }
        }.toMutableList()
        return thisScopeCandidates + ((parentScope as? SemanticAnalyzerSymbolTable)?.findAllMatchingCallables(currentSymbolTable, originalName, receiverClass, arguments) ?: emptyList())
    }

    // only use in semantic analyzer
    fun findMatchingCallables(currentSymbolTable: SymbolTable, originalName: String, receiverType: DataType?, arguments: List<FunctionCallArgumentInfo>): List<FindCallableResult> {
        val receiverClass = when (receiverType) {
            is ObjectType -> receiverType.clazz
            null -> null
            else -> findClass(receiverType.name)!!.first
        }
        return findAllMatchingCallables(currentSymbolTable, originalName, receiverClass, arguments)
            .also { log.v { "Matching functions:\n${it.joinToString("\n")}" } }
            .firstOrNull()?.let { listOf(it) }
            ?: emptyList()
    }

    fun DataType.toTypeNode(): TypeNode =
        if (this is NullType) {
            TypeNode("Nothing", null, true)
        } else if (this !is ObjectType && this !is FunctionType) {
            TypeNode(name, null, isNullable)
        } else if (this is FunctionType) {
            FunctionTypeNode(
                parameterTypes = arguments.map { it.toTypeNode() },
                returnType = returnType.toTypeNode(),
                isNullable = isNullable,
            )
        } else {
            TypeNode(name, null, isNullable)
        }
}

fun FunctionDeclarationNode.toSignature(symbolTable: SemanticAnalyzerSymbolTable): String {
    with (symbolTable) {
        return name + "//" + valueParameters.joinToString("/") {
            it.type.toClass().fullQualifiedName
        }
    }
}