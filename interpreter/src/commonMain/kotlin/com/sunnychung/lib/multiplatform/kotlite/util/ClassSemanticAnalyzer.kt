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

    private val topmostFunctionsBySignature: MutableMap<String, FunctionDeclarationNode> = mutableMapOf()
    private var hasAbstractMembers = false
    private val visitedTypes = mutableSetOf<String>()
    private val implementedFunctions = mutableSetOf<FunctionDeclarationNode>()

    private val classMemberResolver = ClassMemberResolver.create(symbolTable, classDefinition, null)!!

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
        if (clazz.fullQualifiedName in visitedTypes) return
        visitedTypes += clazz.fullQualifiedName

        clazz.getMemberFunctionsDeclaredInThisClass().forEach {
            val func = it.value

            if (func !in implementedFunctions) {
                // func is a topmost member that is not overridden

                if (FunctionModifier.abstract in func.modifiers || func.body == null) {
                    hasAbstractMembers = true
                }
            }

            if (FunctionModifier.override in func.modifiers) {
                val resolvedFunc = classMemberResolver.resolveTypes(func, clazz.fullQualifiedName)
                val superClassIdenticalFunctions = (listOfNotNull(clazz.superClass) + clazz.superInterfaces).flatMap { superType ->
                    classMemberResolver.findMemberFunctionsAndExactTypesByDeclaredName(func.name, superType).values
                }
                    .distinctBy { it.function }
                    .filter { FunctionModifier.open in it.function.modifiers }
                    .filter {
                        it.resolvedValueParameterTypes.withIndex().all {
                            it.value.type == resolvedFunc.function.valueParameters[it.index].type
                        }
                    }
                if (superClassIdenticalFunctions.isNotEmpty()) {
                    implementedFunctions += func
                    implementedFunctions += superClassIdenticalFunctions.map { it.function }
                }
            }


//            val signature = func.toSignature(symbolTable)
//            if (!topmostFunctionsBySignature.containsKey(signature)) {
//                // func is a topmost member that is not overridden
//                topmostFunctionsBySignature[signature] = func
//
//                if (FunctionModifier.abstract in func.modifiers || func.body == null) {
//                    hasAbstractMembers = true
//                }
//            } else {
//                if (!topmostFunctionsBySignature[signature]!!.modifiers.contains(FunctionModifier.override)) {
//                    throw SemanticException(position, "Abstract function `${func.name}` is repeatedly defined. It can be resolved by overriding the function.")
//                }
//            }
        }

        clazz.superClass?.let {
            visit(it)
        }

        clazz.superInterfaces.forEach {
            visit(it)
        }
    }
}
