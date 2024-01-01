package com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis

import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.UnitValue
import com.sunnychung.lib.multiplatform.kotlite.test.interpreter.compareNumber
import com.sunnychung.lib.multiplatform.kotlite.test.interpreter.interpreter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyTypeTest {
    @Test
    fun assignToInt1() {
        assertTypeCheckFail("""
            val a: Int = 3.5
        """.trimIndent())
    }

    @Test
    fun assignToInt2() {
        assertTypeCheckFail("""
            var a: Int = 3
            a = 3.5
        """.trimIndent())
    }

    @Test
    fun assignToInt3() {
        assertTypeCheckFail("""
            var a: Int = 3
            a = true
        """.trimIndent())
    }

    @Test
    fun assignToInt4() {
        assertTypeCheckFail("""
            var a: Int = 3
            a = null
        """.trimIndent())
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
        assertTypeCheckFail("""
            var a: Int? = 3
            a = 3.5
        """.trimIndent())
    }

    @Test
    fun assignToDouble1() {
        assertTypeCheckFail("""
            val a: Double = 3
        """.trimIndent())
    }

    @Test
    fun assignToDouble2() {
        assertTypeCheckFail("""
            var a: Double = 3.5
            a = 3
        """.trimIndent())
    }

    @Test
    fun assignToDouble3() {
        assertTypeCheckFail("""
            var a: Double = 3.5
            a = true
        """.trimIndent())
    }

    @Test
    fun assignToDouble4() {
        assertTypeCheckFail("""
            var a: Double = 3.5
            a = null
        """.trimIndent())
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
        assertTypeCheckFail("""
            if (true) {
                var a: Int = 2
                a = 30.0
            }
        """.trimIndent())
    }

    @Test
    fun blockFail2() {
        assertTypeCheckFail("""
            var a: Int = 2
            if (true) {
                a = 30.0
            }
        """.trimIndent())
    }

    @Test
    fun assignToDoubleNullableFail() {
        assertTypeCheckFail("""
            var a: Double? = 3.5
            a = 3
        """.trimIndent())
    }

    @Test
    fun functionArgumentFail() {
        assertTypeCheckFail("""
            fun f(a: Double = 20): Double {
                return a
            }
            f()
        """.trimIndent())
    }

    @Test
    fun functionModifyOutsideFail() {
        assertTypeCheckFail("""
            var a: Int = 2
            fun f() {
                a = 30.0
            }
            f()
        """.trimIndent())
    }

    @Test
    fun functionReturnTypeFail1() {
        assertTypeCheckFail("""
            fun f(a: Double): Double {
                return 20
            }
            f(1.0)
        """.trimIndent())
    }

    @Test
    fun functionReturnTypeFail2() {
        assertTypeCheckFail("""
            fun f(a: Double): Int {
                return 20
            }
            var result: Double = f(1.0)
        """.trimIndent())
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
        assertTypeCheckFail("""
            fun f(a: Double): Unit {
                return 20.0
            }
            f(1.0)
        """.trimIndent())
    }

    @Test
    fun classPrimaryConstructorFail1() {
        assertTypeCheckFail("""
            class Cls(c: Int = 10, var a: Int = 60, var b: Double = c)
            Cls()
        """.trimIndent())
    }

    @Test
    fun classPrimaryConstructorFail2() {
        assertTypeCheckFail("""
            class Cls(var a: Int = 60, var b: Double = 1)
            Cls()
        """.trimIndent())
    }

    @Test
    fun classInitFail1() {
        assertTypeCheckFail("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                val c: Double = b
            }
            Cls()
        """.trimIndent())
    }

    @Test
    fun classInitFail2() {
        assertTypeCheckFail("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                val c: Double = 12
            }
            Cls()
        """.trimIndent())
    }

    @Test
    fun classInitFail3() {
        assertTypeCheckFail("""
            class Cls(var a: Int = 60) {
                var b: Int = 10
                init {
                    b = 20.0
                }
            }
            Cls()
        """.trimIndent())
    }

    @Test
    fun classFunctionArgument() {
        assertTypeCheckFail("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                fun f(c: Double) {
                    val d: Int = c
                }
            }
            Cls().f(1)
        """.trimIndent())
    }

    @Test
    fun classFunction1() {
        assertTypeCheckFail("""
            class Cls(var a: Int = 60) {
                val b: Int = 10
                fun f(): Double {
                    return b
                }
            }
            Cls().f()
        """.trimIndent())
    }

    @Test
    fun classFunction2() {
        assertTypeCheckFail("""
            class Cls(var a: Int = 60) {
                val b: Double = 10.0
                fun f(): Double {
                    return b
                }
            }
            val r: Int = Cls().f()
        """.trimIndent())
    }

    @Test
    fun classPropertyAccessorsFail1() {
        assertTypeCheckFail("""
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
    }

    @Test
    fun classPropertyAccessorsFail2() {
        assertTypeCheckFail("""
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
    }

    @Test
    fun classPropertyAccessorsFail3() {
        assertTypeCheckFail("""
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
    }

    @Test
    fun classPropertyAccessorsFail4() {
        assertTypeCheckFail("""
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
    }
}
