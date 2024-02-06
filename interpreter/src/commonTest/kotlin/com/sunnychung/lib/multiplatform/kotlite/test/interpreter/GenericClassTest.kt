package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ClassInstance
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenericClassTest {

    @Test
    fun pairReadProperties() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B) {
                fun concat(): String = "${'$'}first,${'$'}second"
            }
            val p1 = MyPair<Int, Double>(10, 2.345)
            val a: Int = p1.first
            val b: Double = p1.second
            val c: String = p1.concat()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(4, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        assertEquals("10,2.345", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
    }

    @Test
    fun pairFunctions() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B) {
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first
                fun getSecond(): B {
                    return second
                }
            }
            val p1 = MyPair<Int, Double>(10, 2.345)
            val d: Int = p1.getFirst()
            val e: Double = p1.getSecond()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("e") as DoubleValue)
    }

    @Test
    fun pairReadWriteProperties() {
        val interpreter = interpreter("""
            class MyPair<A, B>(var first: A, var second: B) {
                fun concat(): String = "${'$'}first,${'$'}second"
            }
            val p1 = MyPair<Int, Double>(10, 2.345)
            val a: Int = p1.first
            val b: Double = p1.second
            val c: String = p1.concat()
            ++p1.first
            p1.second += 5
            val d: Int = p1.first
            val e: Double = p1.second
            val f: String = p1.concat()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(7, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        assertEquals("10,2.345", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        compareNumber(7.345, symbolTable.findPropertyByDeclaredName("e") as DoubleValue)
        assertTrue((symbolTable.findPropertyByDeclaredName("f") as StringValue).value.startsWith("11,7.34"))
    }

    @Test
    fun nestedPair() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B) {
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first
                fun getSecond(): B {
                    return second
                }
            }
            val p1 = MyPair<MyPair<Int, String>, Double>(MyPair<Int, String>(10, "abc"), 2.345)
            val a: Int = p1.first.first
            val b: String = p1.first.second
            val c: Double = p1.second
            val aa: Int = p1.getFirst().getFirst()
            val bb: String = p1.getFirst().second
            val bbb: String = p1.first.getSecond()
            val cc: Double = p1.getSecond()
            val s: String = p1.concat()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(9, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("c") as DoubleValue)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("aa") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("bb") as StringValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("bbb") as StringValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("cc") as DoubleValue)
        assertEquals("MyPair(),2.345", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun multipleNestedPairs() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B) {
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first
                fun getSecond(): B {
                    return second
                }
            }
            val p1 = MyPair<MyPair<Int, String>, Double>(MyPair<Int, String>(10, "abc"), 2.345)
            val p2 = MyPair<Double, MyPair<Int, String>>(3.456, MyPair<Int, String>(16, "def"))
            val a1: Int = p1.first.first
            val b1: String = p1.first.second
            val c1: Double = p1.second
            val aa1: Int = p1.getFirst().getFirst()
            val bb1: String = p1.getFirst().second
            val bbb1: String = p1.first.getSecond()
            val cc1: Double = p1.getSecond()
            val s1: String = p1.concat()
            val a2: Int = p2.second.first
            val b2: String = p2.second.second
            val c2: Double = p2.first
            val aa2: Int = p2.getSecond().getFirst()
            val bb2: String = p2.getSecond().second
            val bbb2: String = p2.second.getSecond()
            val cc2: Double = p2.getFirst()
            val s2: String = p2.concat()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(18, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)

        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b1") as StringValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("c1") as DoubleValue)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("aa1") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("bb1") as StringValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("bbb1") as StringValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("cc1") as DoubleValue)
        assertEquals("MyPair(),2.345", (symbolTable.findPropertyByDeclaredName("s1") as StringValue).value)

        assertEquals(16, (symbolTable.findPropertyByDeclaredName("a2") as IntValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("b2") as StringValue).value)
        compareNumber(3.456, symbolTable.findPropertyByDeclaredName("c2") as DoubleValue)
        assertEquals(16, (symbolTable.findPropertyByDeclaredName("aa2") as IntValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("bb2") as StringValue).value)
        assertEquals("def", (symbolTable.findPropertyByDeclaredName("bbb2") as StringValue).value)
        compareNumber(3.456, symbolTable.findPropertyByDeclaredName("cc2") as DoubleValue)
        assertEquals("3.456,MyPair()", (symbolTable.findPropertyByDeclaredName("s2") as StringValue).value)
    }

    @Test
    fun deeplyNestedClass() {
        val interpreter = interpreter("""
            class MyPair<A, B>(val first: A, val second: B) {
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first
                fun getSecond(): B {
                    return second
                }
            }
            class MyVal<T>(val value: T) {
                fun getValue(): T = value
            }
            val p = MyPair<MyVal<MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>>, Double>(
                MyVal<MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>>(
                    MyVal<MyVal<MyVal<MyVal<MyPair<Int, String>>>>>(
                        MyVal<MyVal<MyVal<MyPair<Int, String>>>>(
                            MyVal<MyVal<MyPair<Int, String>>>(
                                MyVal<MyPair<Int, String>>(
                                    MyPair<Int, String>(10, "abc")
                                )
                            )
                        )
                    )
                ),
                2.345
            )
            val a: Int = p.first.value.value.value.value.value.first
            val b: String = p.first.value.value.value.value.value.second
            val c: Double = p.second
            val aa: Int = p.getFirst().getValue().getValue().getValue().getValue().getValue().getFirst()
            val bb: String = p.getFirst().getValue().getValue().value.value.getValue().second
            val bbb: String = p.first.value.getValue().getValue().value.value.getSecond()
            val cc: Double = p.getSecond()
            val s: String = p.concat()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(9, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("b") as StringValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("c") as DoubleValue)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("aa") as IntValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("bb") as StringValue).value)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("bbb") as StringValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("cc") as DoubleValue)
        assertEquals("MyVal(),2.345", (symbolTable.findPropertyByDeclaredName("s") as StringValue).value)
    }

    @Test
    fun memberPropertiesOfTypeParametersInClassBody() {
        val interpreter = interpreter("""
            class MyPair<A, B>(first: A, second: B) {
                val first: A = first
                val second: B = second
                
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first
                fun getSecond(): B {
                    return second
                }
            }
            val p1 = MyPair<Int, Double>(10, 2.345)
            val a: Int = p1.first
            val b: Double = p1.second
            val c: String = p1.concat()
            val d: Int = p1.getFirst()
            val e: Double = p1.getSecond()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(6, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        assertEquals("10,2.345", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("e") as DoubleValue)
    }

    @Test
    fun memberPropertiesOfTypeParametersInInit1() {
        val interpreter = interpreter("""
            class MyPair<A, B>(first: A, second: B) {
                var first: A? = null
                var second: B? = null
                
                init {
                    this.first = first
                    this.second = second
                }
                
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first!!
                fun getSecond(): B {
                    return second!!
                }
            }
            val p1 = MyPair<Int, Double>(10, 2.345)
            val a: Int = p1.first!!
            val b: Double = p1.second!!
            val c: String = p1.concat()
            val d: Int = p1.getFirst()
            val e: Double = p1.getSecond()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(6, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        assertEquals("10,2.345", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("e") as DoubleValue)
    }

    @Test
    fun memberPropertiesOfTypeParametersInInit2() {
        val interpreter = interpreter("""
            class MyPair<A, B>(first: A, second: B) {
                var first: A? = null
                var second: B? = null
                
                init {
                    val a: A = first
                    val b: B = second
                    this.first = a
                    this.second = b
                }
                
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first!!
                fun getSecond(): B {
                    return second!!
                }
            }
            val p1 = MyPair<Int, Double>(10, 2.345)
            val a: Int = p1.first!!
            val b: Double = p1.second!!
            val c: String = p1.concat()
            val d: Int = p1.getFirst()
            val e: Double = p1.getSecond()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(6, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        assertEquals("10,2.345", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("e") as DoubleValue)
    }

    @Test
    fun classesWithSameTypeParameterName() {
        val interpreter = interpreter("""
            class MyVal1<T>(val value: T) {
                fun getValue(): T = value
            }
            class MyVal2<T>(val value: T) {
                fun getValue(): T = value
            }
            val o = MyVal1<MyVal2<Int>>(MyVal2<Int>(10))
            val a: Int = o.getValue().getValue()
            val b: Int = o.value.value
            val c: Int = o.value.getValue()
            val d: Int = o.getValue().value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(5, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun lambdaReferringClassTypeParameter() {
        val interpreter = interpreter("""
            class MyVal2<T>(val value: T) {
                fun getValueReader(): () -> T = { value }
            }
            val o = MyVal2<Int>(10)
            val f: () -> Int = o.getValueReader()
            val a: Int = f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun lambdaInferringClassTypeParameter() {
        val interpreter = interpreter("""
            class MyVal2<T>(val value: T) {
                fun getValueReader(): () -> T = { value }
            }
            val o = MyVal2<Int>(10)
            val a: Int = o.getValueReader()()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun lambdaUsesClassTypeParameter() {
        val interpreter = interpreter("""
            class MyVal2<T>(val value: T) {
                fun getValueReader(): () -> T = {
                    val x: T = value
                    x
                }
            }
            val o = MyVal2<Int>(10)
            val a: Int = o.getValueReader()()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun lambdaUsesClassTypeParameterDifferentFromReturnType() {
        val interpreter = interpreter("""
            class MyVal2<T>(val value: T) {
                fun getValueReader(): () -> Int = {
                    val x: T = value
                    x as Int
                }
            }
            val o = MyVal2<Int>(10)
            val a: Int = o.getValueReader()()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun lambdaUsesClassTypeParameterOfNonTypeParameterType() {
        val interpreter = interpreter("""
            class MyVal1<T>(val value: T) {
                fun getValue(): T = value
            }
            class MyVal2<T>(val value: T) {
                fun getValueReader(): () -> Int = {
                    val x: T = value
                    (x as MyVal1<Int>).getValue()
                }
            }
            val o = MyVal2<MyVal1<Int>>(MyVal1<Int>(10))
            val a: Int = o.getValueReader()()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun nestedLambdaUsesClassTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<T>(val value: T) {
                fun getValue(): T = value
            }
            class MyVal2<T>(val value: T) {
                fun getValueReader(): () -> (() -> Int) = {
                    {
                        val x: T = value
                        (x as MyVal1<Int>).getValue()
                    }
                }
            }
            val o = MyVal2<MyVal1<Int>>(MyVal1<Int>(10))
            val a: Int = o.getValueReader()()()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun genericClassCallAnotherGenericClass1() {
        val interpreter = interpreter("""
            class MyVal1<T>(val value: T) {
                fun getValue(): T = value
            }
            val o1 = MyVal1<String>("abc")
            class MyVal2<T>(val value: T) {
                fun getValue(): T = value
                fun func(): String = o1.getValue()
            }
            val o2 = MyVal2<Int>(10)
            val a: String = o2.func()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
    }

    @Test
    fun genericClassCallAnotherGenericClass2() {
        val interpreter = interpreter("""
            class MyVal1<A>(val value: A) {
                fun getValue(): A = value
            }
            val o1 = MyVal1<String>("abc")
            class MyVal2<B>(val value: B) {
                fun getValue(): B = value
                fun func(): String = o1.getValue()
            }
            val o2 = MyVal2<Int>(10)
            val a: String = o2.func()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
    }

    @Test
    fun castConstantToGeneric() {
        val interpreter = interpreter("""
            class MyVal1<T>(val value: T) {
                fun getValue(): T = value
                fun func(): T = "abc" as T
            }
            val o = MyVal1<String>("def")
            val a: String = o.func()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("abc", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
    }

    @Test
    fun castExpressionToGeneric() {
        val interpreter = interpreter("""
            class MyVal1<T>(val value: T) {
                fun getValue(): T = value
                fun func(): T = ("abc" + (value as String)) as T
            }
            val o = MyVal1<String>("def")
            val a: String = o.func()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("abcdef", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
    }

    @Test
    fun lambdaAsArgument() {
        val interpreter = interpreter("""
            class MyVal1<T>(var value: T) {
                fun getValue(): T = value
                fun func(op: () -> T): T = op()
            }
            val o = MyVal1<Int>(10)
            val a = o.func { 15 }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(2, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun constructOtherClassOfSameTypeParameter() {
        val interpreter = interpreter("""
            class MyVal1<T>(var value: T) {
                fun getValue(): T = value
                fun func(op: () -> T): T = op()
            }
            class MyVal2<T>(var value: T) {
                fun produce(): MyVal1<T> = MyVal1<T>(value)
            }
            val o2 = MyVal2<Int>(10)
            val o1 = o2.produce()
            val a = o1.getValue()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(3, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals("MyVal2", (symbolTable.findPropertyByDeclaredName("o2") as ClassInstance).clazz!!.fullQualifiedName)
        assertEquals("Int", (symbolTable.findPropertyByDeclaredName("o2") as ClassInstance).typeArgumentByName["T"]!!.descriptiveName)
        assertEquals("MyVal1", (symbolTable.findPropertyByDeclaredName("o1") as ClassInstance).clazz!!.fullQualifiedName)
        assertEquals("Int", (symbolTable.findPropertyByDeclaredName("o1") as ClassInstance).typeArgumentByName["T"]!!.descriptiveName)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun otherClassNameIsSameAsTypeParameterName() {
        val interpreter = interpreter("""
            class T {
                fun f(): Int = 5
            }
            class MyVal1<T>(var value: T) {
                fun getValue(): T = value
                fun func(op: () -> T): T = op()
                fun asT(): T = 20 as T
            }
            val o = MyVal1<Int>(10)
            val a = o.func { 15 }
            val b = o.value
            val c = o.getValue()
            val d = o.asT()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(5, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun classNameIsSameAsTypeParameterName() {
        val interpreter = interpreter("""
            class T<T>(var value: T) {
                fun getValue(): T = value
                fun func(op: () -> T): T = op()
                fun asT(): T = 20 as T
            }
            val o = T<Int>(10)
            val a = o.func { 15 }
            val b = o.value
            val c = o.getValue()
            val d = o.asT()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(5, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun classNameIsSameAsTypeParameterNameNested() {
        val interpreter = interpreter("""
            class T<T>(var value: T) {
                fun getValue(): T = value
                fun func(op: () -> T): T = op()
                fun asT(): T = 20 as T
            }
            val o = T<T<T<Int>>>(T<T<Int>>(T<Int>(10)))
            val a = o.getValue().value.func { 15 }
            val b = o.value.value.value
            val c = o.getValue().getValue().getValue()
            val d = o.value.getValue().asT()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(5, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
    }

    @Test
    fun memberPropertiesOfTypeParametersInInitOfDeeplyInheritedClass() {
        val interpreter = interpreter("""
            open class MyPair<A, B>(first: A, second: B) {
                var first: A? = null
                var second: B? = null
                
                init {
                    val a: A = first
                    val b: B = second
                    this.first = a
                    this.second = b
                }
                
                fun concat(): String = "${'$'}first,${'$'}second"
                fun getFirst(): A = this.first!!
                fun getSecond(): B {
                    return second!!
                }
            }
            open class MyPair2<A, B>(first: A, second: B) : MyPair<A, B>(first, second)
            open class MyPair3<A, B>(first: A, second: B) : MyPair2<A, B>(first, second)
            open class MyPair4<A, B>(first: A, second: B) : MyPair3<A, B>(first, second)
            class MyPair5<A, B>(first: A, second: B) : MyPair4<A, B>(first, second)
            val p1 = MyPair5<Int, Double>(10, 2.345)
            val a: Int = p1.first!!
            val b: Double = p1.second!!
            val c: String = p1.concat()
            val d: Int = p1.getFirst()
            val e: Double = p1.getSecond()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        assertEquals(6, symbolTable.propertyValues.size)
        println(symbolTable.propertyValues)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        assertEquals("10,2.345", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        compareNumber(2.345, symbolTable.findPropertyByDeclaredName("e") as DoubleValue)
    }
}
