package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ClassInheritanceTest {

    @Test
    fun inheritProperty() {
        val interpreter = interpreter("""
            open class A {
                var a = 1
            }
            class B(var b: Int) : A()
            
            val x = B(123)
            val a = x.a
            val b = x.b
            x.a += 2
            val c = x.a
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun inheritPropertyInConstructor() {
        val interpreter = interpreter("""
            open class A(val c: Int) {
                var a = 1
            }
            class B(var b: Int) : A(b + 10)
            
            val x = B(123)
            val a = x.a
            val b = x.b
            x.a += 2
            val c = x.a
            val d = x.c
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(133, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun passArgumentToSuperConstructor() {
        val interpreter = interpreter("""
            open class A(val c: Int) {
                var a = 1
            }
            class B(b: Int) : A(b + 10)
            
            val x = B(123)
            val a = x.a
            x.a += 2
            val c = x.a
            val d = x.c
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(133, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun inheritFunction() {
        val interpreter = interpreter("""
            open class A {
                var a = 1
                fun hello() = a
            }
            class B(var b: Int) : A()
            
            val x = B(123)
            val a = x.a
            val b = x.b
            x.a += 2
            val c = x.hello()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun inheritExtensionFunction() {
        val interpreter = interpreter("""
            open class A {
                fun Int.double() = this * 2
            }
            class B(val b: Int) : A() {
                fun doubleB() = b.double()
            }
            
            val x = B(123)
            val a = x.doubleB()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(246, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun overrideMemberFunction() {
        val interpreter = interpreter("""
            var a = 1
            open class A {
                open fun f() {
                    a += 5
                }
            }
            class B : A() {
                override fun f() {
                    a += 7
                }
            }
            
            val x = B()
            x.f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun memberFunctionsAreNotOverriddenIfSignaturesAreDifferent() {
        val interpreter = interpreter("""
            var a = 1
            open class A {
                fun f(x: Int) {
                    a += 5
                }
            }
            class B : A() {
                fun f(x: String) {
                    a += 7
                }
            }
            
            val x = B()
            x.f(123)
            val b = a
            x.f("abc")
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(13, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun overrideMemberProperty() {
        val interpreter = interpreter("""
            open class A {
                open var x = 1
                fun f() = x
            }
            class B : A() {
                override var x = 2
            }
            val x = B()
            val a = x.x
            val b = x.f()
            x.x += 10
            val c = x.x
            val d = x.f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun inheritPropertyInClassExtendedWithTypeParameter() {
        val interpreter = interpreter("""
            open class A<T> {
                var a: T? = null
            }
            class B : A<String>()
            class C : A<Int>()
            val x = B()
            val y = C()
            x.a = "abc"
            y.a = 123
            val a = x.a!!
            val b = y.a!!
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun inheritPropertyInClassDeeplyExtendedWithTypeParameter1() {
        val interpreter = interpreter("""
            open class A<T> {
                var a: T? = null
            }
            open class B<T> : A<T>()
            open class C<T> : B<T>()
            class D<T> : C<T>()
            val x = D<String>()
            val y = D<Int>()
            x.a = "abc"
            y.a = 123
            val a = x.a!!
            val b = y.a!!
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun inheritPropertyInClassDeeplyExtendedWithTypeParameter2() {
        val interpreter = interpreter("""
            open class A<T> {
                var a: T? = null
            }
            open class B<T> : A<T>()
            open class C<T> : B<Int>()
            open class D<T> : C<T>()
            class E<T> : D<T>()
            val x = E<String>()
            val y = E<Int>()
            x.a = 456
            y.a = 123
            val a = x.a!!
            val b = y.a!!
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(456, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun inheritPropertyInClassExtendedWithDifferentTypeParameters() {
        val interpreter = interpreter("""
            open class A<T> {
                var a: T? = null
            }
            class B<T1, T2> : A<T2>()
            val x = B<Int, String>()
            val y = B<Double, Int>()
            x.a = "abc"
            y.a = 123
            val a: String = x.a!!
            val b: Int = y.a!!
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun inheritPropertyInClassDeeplyExtendedWithDifferentTypeParameters() {
        val interpreter = interpreter("""
            open class A<T> {
                var a: T? = null
            }
            open class B<Y, Z> : A<Z>()
            open class C<BB, AA> : B<AA, BB>()
            open class D<X, Y> : C<Y, X>()
            class E<T1, T2> : D<T1, T2>()
            val x = E<Int, String>()
            val y = E<Double, Int>()
            x.a = "abc"
            y.a = 123
            val a: String = x.a!!
            val b: Int = y.a!!
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun inheritPropertyInClassExtendedWithDifferentNestedTypeParameters() {
        val interpreter = interpreter("""
            open class A<T>(v: T) {
                var value: T? = null
                
                init {
                    value = v
                }
            }
            class MyPair<T1, T2>(val first: T1, val second: T2)
            class B<T1, T2>(x: MyPair<T1, T2>) : A<MyPair<T1, T2>>(x)
            val x = B<Int, String>(MyPair(123, "abc"))
            val y = B<String, Int>(MyPair("def", 456))
            val a: Int = x.value!!.first
            val b: String = x.value!!.second
            val c: String = y.value!!.first
            val d: Int = y.value!!.second
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(6, symbolTable.propertyValues.size)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(456, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun inheritFunctionInClassExtendedWithTypeParameter() {
        val interpreter = interpreter("""
            open class A<T> {
                var a: T? = null
                
                fun getA(): T? = a
                
                fun setA(value: T) {
                    this.a = value
                }
            }
            class B : A<String>()
            class C : A<Int>()
            val x = B()
            val y = C()
            x.setA("abc")
            y.setA(123)
            val a: String = x.getA()!!
            val b: Int = y.getA()!!
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun inheritFunctionUsingTypeParameterAndInClassExtendedWithTypeParameter() {
        val interpreter = interpreter("""
            open class A<T> {
                fun cast(value: Any?): T? = value as? T
            }
            class B : A<String>()
            class C : A<Int>()
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
    fun inheritFunctionInClassDeeplyExtendedWithTypeParameter1() {
        val interpreter = interpreter("""
            open class A<T> {
                var a: T? = null
                
                fun getA(): T? = a
                
                fun setA(value: T) {
                    this.a = value
                }
            }
            open class B<T> : A<T>()
            open class C<T> : B<T>()
            class D<T> : C<T>()
            val x = D<String>()
            val y = D<Int>()
            x.setA("abc")
            y.setA(123)
            val a: String = x.getA()!!
            val b: Int = y.getA()!!
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun inheritFunctionInClassDeeplyExtendedWithTypeParameter2() {
        val interpreter = interpreter("""
            open class A<T> {
                var a: T? = null
                
                fun getA(): T? = a
                
                fun setA(value: T) {
                    this.a = value
                }
            }
            open class B<T> : A<T>()
            open class C<T> : B<Int>()
            open class D<T> : C<T>()
            class E<T> : D<T>()
            val x = E<String>()
            val y = E<Int>()
            x.setA(456)
            y.setA(123)
            val a: Int = x.getA()!!
            val b: Int = y.getA()!!
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(456, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun functionAccessSuperFunction() {
        val interpreter = interpreter("""
            var s = ""
            open class A {
                open fun f(value: Int) {
                    s += "A${'$'}{value + 10},"
                }
            }
            class B : A() {
                override fun f(value: Int) {
                    s += "B${'$'}{value + 20},"
                    super.f(value + 2)
                    s += "B${'$'}{value + 30},"
                }
            }
            val x = B()
            x.f(2)
            val s1 = s
            x.f(3)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("B22,A14,B32,", (symbolTable.findPropertyByDeclaredName("s1") as StringValue).value)
        assertEquals("B22,A14,B32,B23,A15,B33,", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun functionAccessDeepSuperFunction() {
        val interpreter = interpreter("""
            var s = ""
            open class A {
                open fun f(value: Int) {
                    s += "A${'$'}{value + 10},"
                }
            }
            open class B : A()
            open class C : B() {
                open override fun f(value: Int) {
                    s += "C${'$'}{value + 20},"
                    super.f(value + 2)
                    s += "C${'$'}{value + 30},"
                }
            }
            class D : C() {
                override fun f(value: Int) {
                    s += "D${'$'}{value + 20},"
                    super.f(value + 2)
                    s += "D${'$'}{value + 30},"
                }
            }
            val x = D()
            x.f(2)
            val s1 = s
            x.f(3)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("D22,C24,A16,C34,D32,", (symbolTable.findPropertyByDeclaredName("s1") as StringValue).value)
        assertEquals("D22,C24,A16,C34,D32,D23,C25,A17,C35,D33,", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun functionAccessSuperProperty() {
        val interpreter = interpreter("""
            open class A {
                open var a: Int = 1
            }
            class B : A() {
                override var a: Int = 10
                fun getSuperA() = super.a
                fun setSuperA(x: Int) {
                    super.a = x
                }
            }
            val x = B()
            val a = x.a
            val b = x.getSuperA()
            x.setSuperA(6)
            val c = x.a
            val d = x.getSuperA()
            x.a += 3
            val e = x.a
            val f = x.getSuperA()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(13, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
    }

    @Test
    fun functionAccessDeepSuperProperty() {
        val interpreter = interpreter("""
            open class A {
                open var a: Int = 1
            }
            open class B : A()
            open class C : B()
            open class D : C() {
                open override var a: Int = 10
                fun getSuperA2() = super.a
                fun setSuperA2(x: Int) {
                    super.a += x
                }
            }
            class E : D() {
                override var a: Int = 20
                fun getSuperA1() = super.a
                fun setSuperA1(x: Int) {
                    super.a = x
                }
            }
            val x = E()
            val a0 = x.a
            val a1 = x.getSuperA1()
            val a2 = x.getSuperA2()
            x.setSuperA1(6)
            val b0 = x.a
            val b1 = x.getSuperA1()
            val b2 = x.getSuperA2()
            x.a += 3
            val c0 = x.a
            val c1 = x.getSuperA1()
            val c2 = x.getSuperA2()
            x.setSuperA2(-5)
            val d0 = x.a
            val d1 = x.getSuperA1()
            val d2 = x.getSuperA2()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(13, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("a0") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a2") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b0") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("b1") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b2") as IntValue).value)
        assertEquals(23, (symbolTable.findPropertyByDeclaredName("c0") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("c1") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("c2") as IntValue).value)
        assertEquals(23, (symbolTable.findPropertyByDeclaredName("d0") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("d1") as IntValue).value)
        assertEquals(-4, (symbolTable.findPropertyByDeclaredName("d2") as IntValue).value)
    }

    @Test
    fun propertyAccessSuperFunction() {
        val interpreter = interpreter("""
            var s = ""
            open class A {
                open fun f(value: Int) {
                    s += "A${'$'}{value + 10},"
                }
            }
            class B : A() {
                val v = super.f(2)
                override fun f(value: Int) {
                    s += "B${'$'}{value + 20},"
                    super.f(value + 2)
                    s += "B${'$'}{value + 30},"
                }
            }
            val x = B()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("A12,", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun propertyAccessDeepSuperFunction() {
        val interpreter = interpreter("""
            var s = ""
            open class A {
                open fun f(value: Int) {
                    s += "A${'$'}{value + 10},"
                }
            }
            open class B : A()
            open class C : B() {
                val a = super.f(4)
                val s1 = s
                open override fun f(value: Int) {
                    s += "C${'$'}{value + 20},"
                    super.f(value + 2)
                    s += "C${'$'}{value + 30},"
                }
            }
            class D : C() {
                val b = super.f(7)
                override fun f(value: Int) {
                    s += "D${'$'}{value + 20},"
                    super.f(value + 2)
                    s += "D${'$'}{value + 30},"
                }
            }
            val x = D()
            val s1 = x.s1
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals("A14,", (symbolTable.findPropertyByDeclaredName("s1") as StringValue).value)
        assertEquals("A14,C27,A19,C37,", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun propertyAccessSuperProperty() {
        val interpreter = interpreter("""
            open class A {
                open var a: Int = 1
            }
            class B : A() {
                override var a: Int = 10
                fun getSuperA() = super.a
                fun setSuperA(x: Int) {
                    super.a = x
                }
                val b = super.a
            }
            val x = B()
            val a = x.b
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun propertyAccessDeepSuperProperty() {
        val interpreter = interpreter("""
            open class A {
                open var a: Int = 1
            }
            open class B : A()
            open class C : B()
            open class D : C() {
                open override var a: Int = 10
                fun getSuperA2() = super.a
                fun setSuperA2(x: Int) {
                    super.a += x
                }
                val d = super.a
            }
            class E : D() {
                override var a: Int = 20
                fun getSuperA1() = super.a
                fun setSuperA1(x: Int) {
                    super.a = x
                }
                val e = super.a
                val f = a
            }
            val x = E()
            val d = x.d
            val e = x.e
            val a = x.a
            val f = x.f
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
    }
}
