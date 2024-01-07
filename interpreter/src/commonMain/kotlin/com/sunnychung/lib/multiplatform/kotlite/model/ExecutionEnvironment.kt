package com.sunnychung.lib.multiplatform.kotlite.model

/**
 * ExecutionEnvironment is stateful. Need to pass the same ExecutionEnvironment instance into both
 * SemanticAnalyzer and Interpreter.
 */
class ExecutionEnvironment(
//    private val registrationFilter: BuiltinFunctionRegistrationFilter = BuiltinFunctionRegistrationFilter { _ -> true }
    private val functionRegistrationFilter: (CustomFunctionDefinition) -> Boolean = { true },
    private val classRegistrationFilter: (String) -> Boolean = { true },
) {
    internal val builtinFunctions: MutableList<CustomFunctionDeclarationNode> = mutableListOf()

    fun registerFunction(function: CustomFunctionDefinition) {
        if (functionRegistrationFilter(function)) {
            builtinFunctions += function.let {
                CustomFunctionDeclarationNode(it)
            }
        }
    }

    internal fun getBuiltinFunctions(topmostSymbolTable: SymbolTable): List<CustomFunctionDeclarationNode> {
        return builtinFunctions.toList()
    }
}
