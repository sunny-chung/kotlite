package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class CompanionObjectTest {
    @Test
    fun companionExtensionFunctions() {
        val interpreter = interpreter("""
            class A
            fun A.Companion.f(): Int {
                return 20
            }
            fun A.Companion.g(): Int {
                return 2
            }
            val a = A.f()
            val b = A.g()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
