package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionResolutionTest {

    @Test
    fun functionsHaveHigherPriorityThanPropertiesWithinSameScope() {
        val interpreter = interpreter("""
            fun f(): Int = 2
            val f = { 3 }
            val a = f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun propertiesInThisScopeHaveHigherPriorityThanFunctionsInParentScope() {
        val interpreter = interpreter("""
            fun f(): Int = 2
            fun g(f: () -> Int): Int {
                return f()
            }
            val a = g { 3 }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun classFunctionsAlwaysHaveHigherPriorityThanExtensionFunctions1() {
        val interpreter = interpreter("""
            class A {
                fun f(): Int = 2
            }
            fun A.f(): Int = 3
            val a = A().f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun classFunctionsAlwaysHaveHigherPriorityThanExtensionFunctions2() {
        val interpreter = interpreter("""
            class A {
                fun f(): Int = 2
            }
            fun g(): Int {
                fun A.f(): Int = 3
                return A().f()
            }
            val a = g()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun classFunctionsHaveHigherPriorityThanClassProperty1() {
        val interpreter = interpreter("""
            class A {
                val f: () -> Int = { 3 }
                fun f(): Int = 2
                
                fun g(): Int = f()
            }
            val a = A().f()
            val b = A().g()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun classFunctionsHaveHigherPriorityThanClassProperty2() {
        val interpreter = interpreter("""
            class A {
                fun f(): Int = 2
                val f: () -> Int = { 3 }
                
                fun g(): Int = f()
            }
            val a = A().f()
            val b = A().g()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun functionOverload1() {
        val interpreter = interpreter("""
            fun f(x: Int, y: String): Int = 2
            fun f(x: String, y: Int): Int = 3
            val a = f("a", 1)
            val b = f(1, "a")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun functionOverload2() {
        val interpreter = interpreter("""
            fun f(x: Int, y: String): Int = 2
            fun f(x: Int): Int = 3
            val a = f(1)
            val b = f(1, "a")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionOverload1() {
        val interpreter = interpreter("""
            class A
            fun A.f(x: Int, y: String): Int = 2
            fun A.f(x: String, y: Int): Int = 3
            val a = A().f("a", 1)
            val b = A().f(1, "a")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionOverload2() {
        val interpreter = interpreter("""
            class A
            fun A.f(x: Int, y: String): Int = 2
            fun A.f(x: Int): Int = 3
            val a = A().f(1)
            val b = A().f(1, "a")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun classMemberFunctionOverload1() {
        val interpreter = interpreter("""
            class A {
                fun f(x: Int, y: String): Int = 2
                fun f(x: String, y: Int): Int = 3
            }
            val a = A().f("a", 1)
            val b = A().f(1, "a")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun classMemberFunctionOverload2() {
        val interpreter = interpreter("""
            class A {
                fun f(x: Int, y: String): Int = 2
                fun f(x: Int): Int = 3
            }
            val a = A().f(1)
            val b = A().f(1, "a")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun classMemberFunctionOverload3() {
        val interpreter = interpreter("""
            class A {
                fun f(x: Int, y: String): Int = 2
                fun f(x: String, y: Int): Int = 3
                fun g(): Int = this.f("a", 1)
                fun h(): Int = this.f(1, "a")
            }
            val a = A().g()
            val b = A().h()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun classMemberFunctionOverload4() {
        val interpreter = interpreter("""
            class A {
                fun f(x: Int, y: String): Int = 2
                fun f(x: Int): Int = 3
                fun g(): Int = this.f(1)
                fun h(): Int = this.f(1, "a")
            }
            val a = A().g()
            val b = A().h()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun functionTypeHasTypeParameter() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            fun sum(p: MyPair<Int, Int>): Int = p.first + p.second
            val p1 = MyPair<Int, Int>(10, 12)
            val p2 = MyPair<Int, Int>(31, 32)
            val a = sum(p1)
            val b = sum(p2)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(63, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun vararg() {
        val interpreter = interpreter("""
            var a: Int = 0
            fun f(vararg args: Int) {
                ++a
            }
            f()
            f(1)
            f(1, 2, 3)
            f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }
}
