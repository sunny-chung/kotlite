package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class PropertyTypeTest {
    @Test
    fun assignToInt1() {
        val interpreter = interpreter("""
            val a: Int = 3.5
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun assignToInt2() {
        val interpreter = interpreter("""
            var a: Int = 3
            a = 3.5
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun assignToInt3() {
        val interpreter = interpreter("""
            var a: Int = 3
            a = true
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun assignToInt4() {
        val interpreter = interpreter("""
            var a: Int = 3
            a = null
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun assignToIntNullableSuccess() {
        val interpreter = interpreter("""
            var a: Int? = 3
            a = null
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("a") is NullValue)
    }

    @Test
    fun assignToIntNullableFail() {
        val interpreter = interpreter("""
            var a: Int? = 3
            a = 3.5
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun assignToDouble1() {
        val interpreter = interpreter("""
            val a: Double = 3
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun assignToDouble2() {
        val interpreter = interpreter("""
            var a: Double = 3.5
            a = 3
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun assignToDouble3() {
        val interpreter = interpreter("""
            var a: Double = 3.5
            a = true
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun assignToDouble4() {
        val interpreter = interpreter("""
            var a: Double = 3.5
            a = null
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun assignToDoubleNullableSuccess() {
        val interpreter = interpreter("""
            var a: Double? = 3.5
            a = null
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("a") is NullValue)
    }

    @Test
    fun blockFail1() {
        val interpreter = interpreter("""
            if (true) {
                var a: Int = 2
                a = 30.0
            }
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun blockFail2() {
        val interpreter = interpreter("""
            var a: Int = 2
            if (true) {
                a = 30.0
            }
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun assignToDoubleNullableFail() {
        val interpreter = interpreter("""
            var a: Double? = 3.5
            a = 3
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun functionArgumentFail() {
        val interpreter = interpreter("""
            fun f(a: Double = 20): Double {
                return a
            }
            f()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun functionModifyOutsideFail() {
        val interpreter = interpreter("""
            var a: Int = 2
            fun f() {
                a = 30.0
            }
            f()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun functionReturnTypeFail1() {
        val interpreter = interpreter("""
            fun f(a: Double): Double {
                return 20
            }
            f(1.0)
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun functionReturnTypeFail2() {
        val interpreter = interpreter("""
            fun f(a: Double): Int {
                return 20
            }
            var result: Double = f(1.0)
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun functionReturnTypeSuccess1() {
        val interpreter = interpreter("""
            fun f(a: Double): Double {
                return 20.0
            }
            f(1.0)
        """.trimIndent())
        interpreter.eval()
    }

    @Test
    fun functionReturnTypeSuccess2() {
        val interpreter = interpreter("""
            fun f(a: Double): Double {
                return 20.0
            }
            var result: Double = f(1.0)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        compareNumber(20.0, symbolTable.findPropertyByDeclaredName("result") as DoubleValue)
    }

    @Test
    fun functionReturnTypeUnitSuccess1() {
        val interpreter = interpreter("""
            fun f(a: Double): Unit {
                20.0
            }
            var result: Unit = f(1.0)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        assertTrue(symbolTable.findPropertyByDeclaredName("result") is UnitValue)
    }

    @Test
    fun functionReturnTypeUnitSuccess2() {
        val interpreter = interpreter("""
            fun f(a: Double): Unit {
                20.0
            }
            f(1.0)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(0, symbolTable.propertyValues.size)
    }

    @Test
    fun functionReturnTypeUnitFail() {
        val interpreter = interpreter("""
            fun f(a: Double): Unit {
                return 20.0
            }
            f(1.0)
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classPrimaryConstructorFail1() {
        val interpreter = interpreter("""
            class Cls(c: Int = 10, var a: Int = 60, var b: Double = c)
            Cls()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classPrimaryConstructorFail2() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60, var b: Double = 1)
            Cls()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classInitFail1() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                val c: Double = b
            }
            Cls()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classInitFail2() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                val c: Double = 12
            }
            Cls()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classInitFail3() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                var b: Int = 10
                init {
                    b = 20.0
                }
            }
            Cls()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classFunctionArgument() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                fun f(c: Double) {
                    val d: Int = c
                }
            }
            Cls().f(1)
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classFunction1() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                fun f(): Double {
                    return b
                }
            }
            Cls().f()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classFunction2() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                val b: Double = 10.0
                fun f(): Double {
                    return b
                }
            }
            val r: Int = Cls().f()
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classPropertyAccessorsFail1() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                var b: Double = 10.0
                var c: Double
                    get() = a
                    set(value) {
                        b = value
                    }
            }
            val o: Cls = Cls()
            val a: Double = o.c
            o.c = 20.0
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classPropertyAccessorsFail2() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                var b: Double = 10.0
                var c: Double
                    get() = b
                    set(value) {
                        a = value
                    }
            }
            val o: Cls = Cls()
            val a: Double = o.c
            o.c = 20.0
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classPropertyAccessorsFail3() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                var b: Double = 10.0
                var c: Double
                    get() {
                        return a
                    }
                    set(value) {
                        a = value
                    }
            }
            val o: Cls = Cls()
            val a: Double = o.c
            o.c = 20.0
        """.trimIndent())
        assertFails { interpreter.eval() }
    }

    @Test
    fun classPropertyAccessorsFail4() {
        val interpreter = interpreter("""
            class Cls(var a: Int = 60) {
                var b: Double = 10.0
                var c: Double
                    get() = b
                    set(value) {
                        b = value
                        return a
                    }
            }
            val o: Cls = Cls()
            val a: Double = o.c
            o.c = 20.0
        """.trimIndent())
        assertFails { interpreter.eval() }
    }
}
