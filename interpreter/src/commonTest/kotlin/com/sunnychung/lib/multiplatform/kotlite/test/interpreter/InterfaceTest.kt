package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class InterfaceTest {
    @Test
    fun invokeFunction() {
        val interpreter = interpreter("""
            interface I {
                fun f(x: Int): Int
            }
            class A : I {
                override fun f(x: Int) = 2 * x
            }
            class B : I {
                override fun f(x: Int) = 3 * x
            }
            fun getInstance(x: Int): I {
                return if (x > 0) {
                    A()
                } else {
                    B()
                }
            }
            val a = getInstance(1).f(12)
            val b = getInstance(-1).f(12)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(24, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(36, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
