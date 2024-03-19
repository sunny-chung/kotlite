package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class GenericFunctionAndExtensionFunctionWithGenericClassTest {

    @Test
    fun unrelatedTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
                fun <T> identity(value: T): T = value
            }
            val o = MyVal1<String>("def")
            val a: String = o.getValue()
            val b: Int = o.identity<Int>(20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun useBothClassTypeParameterAndFunctionTypeParameter() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
                fun <T> toPair(value: T): MyPair<A, T> = MyPair<A, T>(this.value, value)
            }
            val o = MyVal1<String>("def")
            val p = o.toPair<Int>(20)
            val a: String = p.first
            val b: Int = p.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionUseUnrelatedTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun <T, X> MyVal1<X>.identity(value: T): T = value
            val o = MyVal1<String>("def")
            val a: String = o.getValue()
            val b: Int = o.identity<Int, String>(20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionUseBothClassTypeParameterAndFunctionTypeParameter() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B)
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun <T, X> MyVal1<X>.toPair(value: T): MyPair<X, T> = MyPair<X, T>(this.value, value)
            val o = MyVal1<String>("def")
            val p = o.toPair<Int, String>(20)
            val a: String = p.first
            val b: Int = p.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionUseStarAsTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun MyVal1<*>.individualFunc(): Int = 20
            val o = MyVal1<String>("def")
            val a: String = o.getValue()
            val b: Int = o.individualFunc()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun extensionFunctionUseStarAndUnrelatedTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun <T> MyVal1<*>.identity(value: T): T = value
            val o = MyVal1<String>("def")
            val a: String = o.getValue()
            val b: Int = o.identity<Int>(20)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun multipleExtensionFunctionsWithSameNameButDifferentTypeArguments() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            fun MyVal1<String>.func(): String = "abc" + value
            fun MyVal1<Int>.func(): Int = value * 2
            val a: String = MyVal1<String>("def").func()
            val b: Int = MyVal1<Int>(10).func()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("abcdef", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun inheritFunctionUsingUnrelatedTypeParameterAndInClassExtendedWithTypeParameter() {
        val interpreter = interpreter("""
            open class A<T> {
                fun <X> cast(value: X): X? = value as? T as? X
            }
            open class A2<T> : A<T>()
            open class A3<T> : A2<T>()
            class B : A3<String>()
            class C : A3<Int>()
            val x = B()
            val y = C()
            val a = x.cast("abc")
            val b = x.cast(123)
            val c = y.cast("abc")
            val d = y.cast(123)
            val e = y.cast(null)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("b"))
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("c"))
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun inheritFunctionUsingUnrelatedTypeParameterWhichHasSameNameWithClassTypeParameter() {
        val interpreter = interpreter("""
            open class A<T> {
                fun <T> cast(value: T): T = value as T
            }
            open class A2<T> : A<T>()
            open class A3<T> : A2<T>()
            class B : A3<String>()
            class C : A3<Int>()
            val x = B()
            val y = C()
            val a = x.cast("abc")
            val b = x.cast(123)
            val c = y.cast("abc")
            val d = y.cast(123)
            val e = y.cast(null)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun extensionFunctionOfSuperClass1() {
        val interpreter = interpreter("""
            open class A<T>
            open class A2<T> : A<T>()
            open class A3<T> : A2<T>()
            open class A4<T> : A3<T>()
            fun <T, X> A2<T>.cast(value: X): X? = value as? T as? X
            class B : A4<String>()
            class C : A4<Int>()
            val x = B()
            val y = C()
            val a = x.cast("abc")
            val b = x.cast(123)
            val c = y.cast("abc")
            val d = y.cast(123)
            val e = y.cast(null)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("b"))
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("c"))
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun extensionFunctionOfSuperClass2() {
        val interpreter = interpreter("""
            open class A<T>
            open class A2<T> : A<T>()
            open class A3<T> : A2<T>()
            open class A4<T> : A3<T>()
            fun <X, Y> A2<Y>.cast(value: X): X? = value as? Y as? X
            class B : A4<String>()
            class C : A4<Int>()
            val x = B()
            val y = C()
            val a = x.cast("abc")
            val b = x.cast(123)
            val c = y.cast("abc")
            val d = y.cast(123)
            val e = y.cast(null)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("b"))
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("c"))
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun baseClassExtensionFunction1() {
        val interpreter = interpreter("""
            open class MyStringListBase<T> {
                var elements = ""
                fun add(element: T) {
                    elements += "${'$'}element,"
                }
            }
            
            class StringList : MyStringListBase<String>()
            class IntList : MyStringListBase<Int>()
            
            fun <O : MyStringListBase<*>> MyStringListBase<*>.addAll(list: O) {
                elements += list.elements
            }
            val x = StringList()
            val y = IntList()
            x.add("abc")
            x.add("def")
            y.add(123)
            y.add(45)
            x.add("bcd")
            x.addAll(y)
            val a = x.elements
            val b = y.elements
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("abc,def,bcd,123,45,", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("123,45,", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun baseClassExtensionFunction2() {
        val interpreter = interpreter("""
            open class MyStringListBase<T> {
                var elements = ""
                fun add(element: T) {
                    elements += "${'$'}element,"
                }
            }
            
            class StringList : MyStringListBase<String>()
            class IntList : MyStringListBase<Int>()
            
            fun <A, B, O : MyStringListBase<B>> MyStringListBase<A>.addAll(list: O) {
                elements += list.elements
            }
            val x = StringList()
            val y = IntList()
            x.add("abc")
            x.add("def")
            y.add(123)
            y.add(45)
            x.add("bcd")
            x.addAll(y)
            val a = x.elements
            val b = y.elements
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("abc,def,bcd,123,45,", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("123,45,", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun nestedTypeParametersInExtensionFunction() {
        val interpreter = interpreter("""
            open class MyStringListBase<T> {
                var elements = ""
                fun add(element: T) {
                    elements += "${'$'}element,"
                }
            }
            
            class StringList : MyStringListBase<String>()
            class IntList : MyStringListBase<Int>()
            class Value<T>(val value: T)
            
            fun <A, B, O : MyStringListBase<B>> MyStringListBase<A>.addAll(list: Value<O>) {
                elements += list.value.elements
            }
            val x = StringList()
            val y = IntList()
            x.add("abc")
            x.add("def")
            y.add(123)
            y.add(45)
            x.add("bcd")
            x.addAll(Value(y))
            val a = x.elements
            val b = y.elements
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("abc,def,bcd,123,45,", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals("123,45,", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
    }

    @Test
    fun functionOverloadWithMoreSpecificValueParameter() {
        val interpreter = interpreter("""
            class A<T>
            class B<T>
            fun <T> A<T>.f(x: T) = 10
            fun <T> A<T>.f(x: B<T>) = 12
            val o = A<Int>
            val a = o.f(1)
            val b = o.f(B<Int>())
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun genericExtensionFunctionWithRecursiveUpperBound() {
        val interpreter = interpreter("""
            fun <T: Comparable<T>> makeList(vararg values: T): List<T> = values
            fun <T: Comparable<T>> List<T>.contains(x: T): Boolean {
                for (it in this) {
                    if (it == x) {
                        return true
                    }
                }
                return false
            }
            val l = makeList(1, 3, 4, 3, 8)
            val a = l.contains(0)
            val b = l.contains(1)
            val c = l.contains(2)
            val d = l.contains(3)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(5, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
    }
}
