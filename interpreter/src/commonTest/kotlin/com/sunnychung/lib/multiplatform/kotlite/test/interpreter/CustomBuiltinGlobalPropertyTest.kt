package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.GlobalProperty
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomBuiltinGlobalPropertyTest {

    @Test
    fun readOnlyProperty() {
        val env = ExecutionEnvironment().apply {
            registerGlobalProperty(GlobalProperty(
                position = SourcePosition("<Test>", 1, 1),
                declaredName = "myPi",
                type = "Double",
                isMutable = false,
                getter = { interpreter -> DoubleValue(3.14, interpreter.symbolTable()) },
            ))
        }
        val interpreter = interpreter("""
            val a: Double = myPi
            val b: Double = myPi * 2
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        compareNumber(3.14, symbolTable.findPropertyByDeclaredName("myPi") as DoubleValue)
        compareNumber(3.14, symbolTable.findPropertyByDeclaredName("a") as DoubleValue)
        compareNumber(6.28, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
    }

    @Test
    fun dynamicProperty() {
        var counter = 0
        val env = ExecutionEnvironment().apply {
            registerGlobalProperty(GlobalProperty(
                position = SourcePosition("<Test>", 1, 1),
                declaredName = "counter",
                type = "Int",
                isMutable = false,
                getter = { interpreter -> IntValue(++counter, interpreter.symbolTable()) },
            ))
        }
        val interpreter = interpreter("""
            val a: Int = counter
            val b: Int = counter
            val c: Int = counter
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun writableExternalProperty() {
        var property = 10
        val env = ExecutionEnvironment().apply {
            registerGlobalProperty(GlobalProperty(
                position = SourcePosition("<Test>", 1, 1),
                declaredName = "x",
                type = "Int",
                isMutable = true,
                getter = { interpreter -> IntValue(property, interpreter.symbolTable()) },
                setter = { interpreter, value -> property = (value as IntValue).value },
            ))
        }
        val interpreter = interpreter("""
            val a: Int = x
            x += 5
            val b: Int = x
            x = 28
            ++x
            val c: Int = x
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }
}
