package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.NumberValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.test.semanticanalysis.assertSemanticFail
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtensionFunctionTest {

    @Test
    fun intExtensionWithoutThis() {
        val interpreter = interpreter("""
            fun Int.f(): Int {
                return 10
            }
            val x: Int = 2
            val y: Int = x.f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun intExtensionWithThis() {
        val interpreter = interpreter("""
            fun Int.triple(): Int {
                return 3 * this
            }
            val x: Int = 2
            val y: Int = x.triple()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun intExtensionOfValue() {
        val interpreter = interpreter("""
            fun Int.triple(): Int {
                return 3 * this
            }
            val x: Int = 2.triple()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun doubleExtensionWithThis() {
        val interpreter = interpreter("""
            fun Double.triple(): Double {
                return 3 * this
            }
            val x: Double = 2.1
            val y: Double = x.triple()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        compareNumber(6.3, symbolTable.findPropertyByDeclaredName("y") as NumberValue<*>)
    }

    @Test
    fun doubleExtensionOfValue() {
        val interpreter = interpreter("""
            fun Double.triple(): Double {
                return 3 * this
            }
            val x: Double = 2.1.triple()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        compareNumber(6.3, symbolTable.findPropertyByDeclaredName("x") as NumberValue<*>)
    }

    @Test
    fun classExtension() {
        val interpreter = interpreter("""
            class Cls(var a: Int)
            fun Cls.f() {
                ++a
            }
            val o: Cls = Cls(20)
            o.f()
            o.f()
            val x: Int = o.a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun classExtensionWithShadowedVariable() {
        val interpreter = interpreter("""
            class Cls(var a: Int)
            fun Cls.f(): Int {
                val a: Int = a++
                return a + 100
            }
            val o: Cls = Cls(20)
            o.f()
            val x: Int = o.f()
            val y: Int = o.a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(121, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun classExtensionOnNestedPath() {
        val interpreter = interpreter("""
            class B(var a: Int)
            class A(val b: B)
            val o: A = A(B(20))
            fun B.f() {
                ++a
            }
            o.b.f()
            o.b.f()
            val x: Int = o.b.a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(22, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun extensionInsideClassAccessingProperties() {
        val interpreter = interpreter("""
            class B(var a: Int)
            class A(val b: B, var c: Int) {
                fun B.f() {
                    a += c++
                }
                
                fun a() {
                    b.f()
                }
            }
            val o: A = A(B(30), 9)
            o.a()
            o.a()
            val x: Int = o.b.a
            val y: Int = o.c
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(49, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun extensionsInsideClassAreNotCallableOutside() {
        assertSemanticFail("""
            class B(var a: Int)
            class A(val b: B, var c: Int) {
                fun B.f() {
                    a += c++
                }
                
                fun a() {
                    b.f()
                }
            }
            val b: B = B(30)
            val o: A = A(b, 9)
            o.a()
            o.a()
            val x: Int = o.b.a
            val y: Int = o.c
            b.f()
        """.trimIndent())
    }

    @Test
    fun extensionInsideClassAccessingFunctions() {
        val interpreter = interpreter("""
            class B(var a: Int) {
                fun inc(x: Int) {
                    a += x
                }
            }
            class A(val b: B, var c: Int) {
                fun addC(): Int {
                    return c++
                }
                
                fun B.f() {
                    inc(addC())
                }
                
                fun a() {
                    b.f()
                }
            }
            val o: A = A(B(30), 9)
            o.a()
            o.a()
            val x: Int = o.b.a
            val y: Int = o.c
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(49, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun extensionAndFunctionWithSameSignatureCanCoexist() {
        val interpreter = interpreter("""
            fun happyNumber(): Int = 6
            fun Int?.happyNumber(): Int = 5
            val a: Int? = 3
            val x: Int = a.happyNumber()
            val y: Int = happyNumber()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun nullableExtension1() {
        val interpreter = interpreter("""
            fun Int?.happyNumber(): Int = 5
            val a: Int? = 3
            val x: Int = a.happyNumber()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun nullableExtension2() {
        val interpreter = interpreter("""
            fun f(a: Int): Int? {
                if (a < 10) return null
                return a
            }
            fun Int?.happyNumber(): Int = 5
            val x: Int = f(0).happyNumber()
            val y: Int = f(20).happyNumber()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun nullableExtensionCanBeUsedOnNonNullType() {
        val interpreter = interpreter("""
            fun f(a: Int): Int {
                return a
            }
            fun Int?.happyNumber(): Int = 5
            val x: Int = f(0).happyNumber()
            val y: Int = 20.happyNumber()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun nullableClassExtension() {
        val interpreter = interpreter("""
            class A
            fun f(): A {
                return A()
            }
            fun A?.happyNumber(): Int = 5
            val a: A? = A()
            val x: Int = f().happyNumber()
            val y: Int = A().happyNumber()
            val z: Int = a.happyNumber()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("z") as IntValue).value)
    }

    @Test
    fun nullableExtensionCanBeUsedOnAnyNullValue() {
        val interpreter = interpreter("""
            fun Int?.happyNumber(): Int = 5
            val a: String? = null
            val x: Int = null.happyNumber()
            // val y: Int = a.happyNumber()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
//        assertEquals(5, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun nullableExtensionCannotBeUsedOnOtherTypeNullValue() {
        assertSemanticFail("""
            fun Int?.happyNumber(): Int = 5
            val a: String? = null
            val x: Int = null.happyNumber()
            val y: Int = a.happyNumber()
        """.trimIndent())
    }

    @Test
    fun nullableExtensionReferenceToThis() {
        val interpreter = interpreter("""
            fun Int?.happyNumber(): Int = (this ?: 100) + 5
            val x: Int = null.happyNumber()
            val y: Int = 2.happyNumber()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(105, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(7, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun superClassExtension() {
        val interpreter = interpreter("""
            open class Base
            class A : Base()
            class B : Base()
            fun Base.f(other: Base): Int = 1000
            val x = A().f(B())
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(1, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(1000, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun genericExtensionFunctionReceiver() {
        val interpreter = interpreter("""
            abstract class Base(val x: Int)
            class A(x: Int) : Base(x)
            class B(x: Int) : Base(x)
            fun <T : Base> T.unwrap(): Int = x
            val a: Int = A(6).unwrap()
            val b = B(25).unwrap()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun genericExtensionFunctionReceiverWithGenericFunctionReceiverAsValueParameter() {
        val interpreter = interpreter("""
            abstract class Base(val x: Int)
            class A(x: Int, val a: Int) : Base(x)
            class B(x: Int, val b: Int) : Base(x)
            fun <T : Base> T.unwrap(f: T.() -> Int): Int = this.f()
            val a: Int = A(6, 20).unwrap { x + a }
            val b = B(25, 29).unwrap { x * b }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(26, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(25 * 29, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun safeCallGenericExtensionFunctionReceiverWithGenericFunctionReceiverAsValueParameter() {
        val interpreter = interpreter("""
            abstract class Base(val x: Int)
            class A(x: Int, val a: Int) : Base(x)
            class B(x: Int, val b: Int) : Base(x)
            fun <T : Base> T.unwrap(f: T.() -> Int): Int = this.f()
            fun f(x: Int) = if (x > 0) A(6, 20) else null
            val a: Int? = f(1)?.unwrap { x + a }
            val b = f(-1)?.unwrap { x * a }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(26, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("b"))
    }

    @Test
    fun callNullableGenericExtensionFunctionReceiver() {
        val interpreter = interpreter("""
            abstract class Base(val x: Int)
            class A(x: Int, val a: Int) : Base(x)
            class B(x: Int, val b: Int) : Base(x)
            fun <T : Base?> T.unwrap(f: T.() -> Int): Int = this.f()
            fun f(x: Int) = if (x > 0) A(6, 20) else null
            val a: Int = f(1).unwrap { (this?.x ?: 0) + (this?.a ?: 0) }
            val b = f(-1).unwrap { (this?.x ?: 0) * (this?.a ?: 0) }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(26, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun resolveNullableTypeToNonNull() {
        val interpreter = interpreter("""
            class A<Z>(val x: Z)
            fun <T : Any> A<T?>.f(alternative: T): A<T> = if (this.x != null) A(x!!) else A(alternative)
            val a: A<Int> = A(30).f(20)
            val x: Int = a.x
            val b: A<Int> = A<Int?>(null).f(25)
            val y: Int = b.x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(30, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun resolveNullableTypeToNonNullAndSameTypeParameterName() {
        val interpreter = interpreter("""
            class A<T>(val x: T)
            fun <T : Any> A<T?>.f(alternative: T): A<T> = if (this.x != null) A(x!!) else A(alternative)
            val a: A<Int> = A(30).f(20)
            val x: Int = a.x
            val b: A<Int> = A<Int?>(null).f(25)
            val y: Int = b.x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(30, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("y") as IntValue).value)
    }

    @Test
    fun returnThis1() {
        val interpreter = interpreter("""
            class A<T>(val x: T)
            fun <T> T.f(): T = this
            val a: Int = 12345.f()
            val b: Any = A(30).f().x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(12345, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(30, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun returnThis2() {
        val interpreter = interpreter("""
            class A<T>(val x: T)
            fun <T> T.f() = this
            val a: Int = 12345.f()
            val b: Any = A(30).f().x
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(12345, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(30, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun nestedGenericExtensionFunctionReceiver() {
        val interpreter = interpreter("""
            abstract class Base<T>(val x: T)
            class A<T>(x: T) : Base<T>(x)
            class B<T>(x: T) : Base<T>(x)
            fun <T, S : Base<T>> S.value(): T = this.x
            val a: Int = A(26).value()
            val b: String = B("abc").value()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(26, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }
}
