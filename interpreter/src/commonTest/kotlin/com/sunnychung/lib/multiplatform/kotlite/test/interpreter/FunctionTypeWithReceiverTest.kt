package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionTypeWithReceiverTest {

    @Test
    fun primitiveAsReceiver() {
        val interpreter = interpreter("""
            fun f(operation: Int.() -> Int): Int {
                return 10.operation()
            }
            val x = f {
                this * 12
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(120, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun classAsReceiver() {
        val interpreter = interpreter("""
            class A(val x: Int)
            
            fun f(operation: A.() -> Int): Int {
                return A(10).operation()
            }
            val x = f {
                x * 12
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(120, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun callingFunctionWithReceiverInReturnedLambda() {
        val interpreter = interpreter("""
            class A(val x: Int)
            
            fun f(y: Int, operation: A.() -> Int): () -> Int {
                return { A(y).operation() * 5 }
            }
            val x = f(10) {
                x * 12
            }()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(600, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }
}
