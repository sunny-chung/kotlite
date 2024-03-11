package com.sunnychung.lib.multiplatform.kotlite.util

import com.sunnychung.lib.multiplatform.kotlite.error.SemanticException
import com.sunnychung.lib.multiplatform.kotlite.model.ClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.model.ClassModifier
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionDeclarationNode
import com.sunnychung.lib.multiplatform.kotlite.model.FunctionModifier
import com.sunnychung.lib.multiplatform.kotlite.model.SemanticAnalyzerSymbolTable
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import com.sunnychung.lib.multiplatform.kotlite.model.toSignature

class ClassSemanticAnalyzer(val symbolTable: SemanticAnalyzerSymbolTable, val position: SourcePosition, val classDefinition: ClassDefinition) {

    val topmostFunctionsBySignature: MutableMap<String, FunctionDeclarationNode> = mutableMapOf()
    var hasAbstractMembers = false

    /**
     * This function assumes all member FunctionDeclarationNode#visit() and
     * corresponding ClassDeclarationNode#visit() have been called
     */
    @Throws(SemanticException::class)
    fun check() {
        visit(classDefinition)

        if (hasAbstractMembers && ClassModifier.abstract !in classDefinition.modifiers) {
            throw SemanticException(position, "Class ${classDefinition.fullQualifiedName} must be marked as abstract, because it contains an abstract member")
        }
    }

    private fun visit(clazz: ClassDefinition) {
        clazz.getMemberFunctionsDeclaredInThisClass().forEach {
            val func = it.value
            val signature = func.toSignature(symbolTable)
            if (!topmostFunctionsBySignature.containsKey(signature)) {
                // func is a topmost member that is not overridden
                topmostFunctionsBySignature[signature] = func

                if (FunctionModifier.abstract in func.modifiers || func.body == null) {
                    hasAbstractMembers = true
                }
            } else {
                if (!topmostFunctionsBySignature[signature]!!.modifiers.contains(FunctionModifier.override)) {
                    throw SemanticException(position, "Abstract function `${func.name}` is repeatedly defined. It can be resolved by overriding the function.")
                }
            }
        }

        clazz.superClass?.let {
            visit(it)
        }

        clazz.superInterfaces.forEach {
            visit(it)
        }
    }
}
