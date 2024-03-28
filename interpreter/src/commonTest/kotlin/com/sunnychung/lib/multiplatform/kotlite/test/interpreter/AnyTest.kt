package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals

class AnyTest {

    @Test
    fun createAny() {
        val interpreter = interpreter("""
            val o = Any()
            val a = o is Any
            val b = o is Any?
            val c = o is Int?
            val d = o is Comparable<*>
            val e = o is Nothing
            val f = o !is Any
            val g = o !is Any?
            val h = o !is Int?
            val i = o !is Comparable<*>
            val j = o !is Nothing
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(11, symbolTable.propertyValues.size)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("i") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("j") as BooleanValue).value)
    }

    @Test
    fun overrideEquals() {
        val interpreter = interpreter("""
            var countA = 0
            var countB = 0
            class A(val x: Int) {
                override fun equals(other: Any?): Boolean {
                    ++countA
                    return other is A && (other as A).x == x
                }
            }
            class B(val x: Int) {
                override fun equals(other: Any?): Boolean {
                    ++countB
                    return super.equals(other)
                }
            }
            val a0 = A(3) == B(3)
            val b0 = A(3) == A(3)
            val c0 = B(3) == B(3)
            val d0 = A(4) == A(3)
            val e0 = A(3) == A(4)
            val a1 = A(3) != B(3)
            val b1 = A(3) != A(3)
            val c1 = B(3) != B(3)
            val d1 = A(4) != A(3)
            val e1 = A(3) != A(4)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(12, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a0") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b0") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c0") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d0") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("e0") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b1") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c1") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d1") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e1") as BooleanValue).value)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("countA") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("countB") as IntValue).value)
    }

    @Test
    fun equalsWithNullAndOtherTypes() {
        val interpreter = interpreter("""
            var countA = 0
            class A(val x: Int) {
                override fun equals(other: Any?): Boolean {
                    ++countA
                    return other is A && (other as A).x == x
                }
            }
            val r1 = A(3) == null
            val c1 = countA
            val r2 = A(3) != null
            val c2 = countA
            val r3 = null == A(3)
            val c3 = countA
            val r4 = null != A(3)
            val c4 = countA
            val r5 = A(3) == true
            val c5 = countA
            val r6 = A(3) != true
            val c6 = countA
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(13, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("r1") as BooleanValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("c1") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("r2") as BooleanValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("c2") as IntValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("r3") as BooleanValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("c3") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("r4") as BooleanValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("c4") as IntValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("r5") as BooleanValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c5") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("r6") as BooleanValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("c6") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("countA") as IntValue).value)
    }

    @Test
    fun inheritedAndOverrideEquals() {
        val interpreter = interpreter("""
            var countA = 0
            var countB = 0
            open class A(val x: Int) {
                override fun equals(other: Any?): Boolean {
                    ++countA
                    return other is A && (other as A).x == x * 2
                }
            }
            open class B(override val x: Int) : A(x) {
                override fun equals(other: Any?): Boolean {
                    ++countB
                    return other is A && (other as A).x == x
                }
            }
            class C(x: Int) : B(x)
            val aa0 = A(3) == B(3) // false
            val ca0 = countA // 1
            val cb0 = countB
            val aa1 = A(3) != B(3) // true
            val ca1 = countA // 2
            val cb1 = countB
            val aa2 = A(3) == B(6) // true
            val ca2 = countA // 3
            val cb2 = countB
            val aa3 = A(3) != B(6) // false
            val ca3 = countA // 4
            val cb3 = countB
            val aa4 = B(3) == A(3) // true
            val ca4 = countA
            val cb4 = countB // 1
            val aa5 = B(3) != A(3) // false
            val ca5 = countA
            val cb5 = countB // 2
            val aa6 = B(6) == A(3) // false
            val ca6 = countA
            val cb6 = countB // 3
            val aa7 = B(6) != A(3) // true
            val ca7 = countA
            val cb7 = countB // 4
            val aa8 = C(3) == A(3) // true
            val ca8 = countA
            val cb8 = countB // 5
            val aa9 = C(3) != A(3) // false
            val ca9 = countA
            val cb9 = countB // 6
            val aa10 = C(6) == A(3) // false
            val ca10 = countA
            val cb10 = countB // 7
            val aa11 = C(6) != A(3) // true
            val ca11 = countA
            val cb11 = countB // 8
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(38, symbolTable.propertyValues.size)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("aa0") as BooleanValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("ca0") as IntValue).value)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("cb0") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("aa1") as BooleanValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("ca1") as IntValue).value)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("cb1") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("aa2") as BooleanValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("ca2") as IntValue).value)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("cb2") as IntValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("aa3") as BooleanValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("ca3") as IntValue).value)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("cb3") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("aa4") as BooleanValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("ca4") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("cb4") as IntValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("aa5") as BooleanValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("ca5") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("cb5") as IntValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("aa6") as BooleanValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("ca6") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("cb6") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("aa7") as BooleanValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("ca7") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("cb7") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("aa8") as BooleanValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("ca8") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("cb8") as IntValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("aa9") as BooleanValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("ca9") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("cb9") as IntValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("aa10") as BooleanValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("ca10") as IntValue).value)
        assertEquals(7, (symbolTable.findPropertyByDeclaredName("cb10") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("aa11") as BooleanValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("ca11") as IntValue).value)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("cb11") as IntValue).value)
    }

    @Test
    fun overrideHashCode() {
        val interpreter = interpreter("""
            open class A(val x: Int) {
                override fun hashCode(): Int {
                    return x
                }
            }
            
            class B(override val x: Int, val y: Int) : A(x) {
                override fun hashCode(): Int {
                    return super.hashCode() * 47 + y
                }
            }
            
            val a = A(5).hashCode()
            val b = B(5, 7).hashCode()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(5 * 47 + 7, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }
}
