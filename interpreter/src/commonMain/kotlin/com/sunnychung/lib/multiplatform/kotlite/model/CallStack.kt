package com.sunnychung.lib.multiplatform.kotlite.model

import com.sunnychung.lib.multiplatform.kotlite.lexer.BuiltinFilename

class CallStack {

    private val activationRecords = mutableListOf<ActivationRecord>()

    init {
        activationRecords += ActivationRecord(
            functionFullQualifiedName = ":builtin",
            callPosition = SourcePosition(BuiltinFilename.BUILTIN, 1, 1),
            scopeType = ScopeType.Script,
            isFunctionCall = false,
            parent = null,
            scopeLevel = 0
        )
        activationRecords += ActivationRecord(
            functionFullQualifiedName = ":global",
            callPosition = SourcePosition(BuiltinFilename.GLOBAL, 1, 1),
            scopeType = ScopeType.Script,
            isFunctionCall = false,
            parent = activationRecords.last(),
            scopeLevel = 1
        )
    }

    internal fun provideBuiltinClass(clazz: ClassDefinition) {
        activationRecords[0].symbolTable.declareClass(SourcePosition.BUILTIN, clazz)
    }

    internal fun provideBuiltinFunction(function: CustomFunctionDeclarationNode) {
        if (function.receiver == null) {
            activationRecords[0].symbolTable.declareFunction(SourcePosition.BUILTIN, function.transformedRefName!!, function)
        } else {
            activationRecords[0].symbolTable.declareExtensionFunction(SourcePosition.BUILTIN, function.transformedRefName!!, function)
        }
    }

    internal fun provideBuiltinExtensionProperty(property: ExtensionProperty) {
        activationRecords[0].symbolTable.declareExtensionProperty(SourcePosition.BUILTIN, property.transformedName!!, property)
    }

    fun getStacktrace(currentPosition: SourcePosition? = null): List<String> {
        // first two are built-in and global, which are not in user scope
        return let {
            if (currentPosition != null) {
                listOf("${currentPosition.filename}:${currentPosition.lineNum}:${currentPosition.col}")
            } else {
                emptyList()
            }
        } + activationRecords.subList(2, activationRecords.size)
            .filter { it.isFunctionCall }
            .asReversed()
            .map { "${it.functionFullQualifiedName ?: "<anonymous>"} (${it.callPosition.filename}:${it.callPosition.lineNum}:${it.callPosition.col})" }
    }

    fun push(functionFullQualifiedName: String?, scopeType: ScopeType, callPosition: SourcePosition, isFunctionCall: Boolean = false) {
        activationRecords += ActivationRecord(
            functionFullQualifiedName = functionFullQualifiedName,
            callPosition = callPosition,
            isFunctionCall = isFunctionCall,
            parent = activationRecords.last(),
            scopeLevel = activationRecords.size,
            scopeType = scopeType,
        )
    }

    fun pop(scopeType: ScopeType) {
        val ar = activationRecords.removeLast()
        if (ar.scopeType != scopeType) {
            throw IllegalStateException("A wrong scope is completed")
        }
    }

    fun currentSymbolTable() = activationRecords.last().symbolTable
}
