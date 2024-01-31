package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class LambdaTest {
    @Test
    fun lambdaSameType() {
        val interpreter = interpreter("""
            val f: (Int) -> Int = { i: Int ->
                i * 2
            }
            val x: Int = f(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun lambdaMultipleInputs() {
        val interpreter = interpreter("""
            val f: (Int, Int) -> Int = { a: Int, b: Int ->
                a + b
            }
            val x: Int = f(10, 19)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun lambdaTransformType() {
        val interpreter = interpreter("""
            val f: (Int) -> String = { i: Int ->
                "<${'$'}{i * 2}>"
            }
            val x: String = f(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("<20>", (symbolTable.findPropertyByDeclaredName("x") as StringValue).value)
    }

    @Test
    fun lambdaNoInput() {
        val interpreter = interpreter("""
            val f: () -> String = {
                var i: Int = 10
                var s: String = ""
                while (i > 0) {
                    s += i
                    i--
                }
                s
            }
            val x: String = f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals("10987654321", (symbolTable.findPropertyByDeclaredName("x") as StringValue).value)
    }

    @Test
    fun lambdaNoOutput() {
        val interpreter = interpreter("""
            var x: Int = 10
            val f: (Int) -> Unit = { i: Int ->
                x += 2 * i
            }
            f(5)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun lambdaNoInputNoOutput() {
        val interpreter = interpreter("""
            var x: Int = 10
            val f: () -> Unit = {
                x *= 2
            }
            f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun reassignLambda() {
        val interpreter = interpreter("""
            var x: Int = 10
            var f: (Int) -> Int = { x: Int ->
                x + 1
            }
            if (x > 5) {
                f = { i: Int -> i + i / 2 - 1 }
            } else {
                f = { x: Int -> x * 2 }
            }
            x = f(2 * x)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun functionHasHigherPrecedenceThanLambda() {
        val interpreter = interpreter("""
            var x: Int = 10
            val f: (Int) -> Int = { x: Int ->
                x + 1
            }
            fun f(x: Int): Int {
                return x + 9
            }
            x = f(2 * x)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun passLambdaIntoFunction() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int, f: (Int) -> Int): Int {
                return f(2 * x)
            }
            var b: Int = higherOrderFunction(
                x = 29,
                f = { i: Int ->
                    ++a
                    i + a
                }
            )
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(69, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun returnLambdaFromFunction() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(): (Int) -> Int {
                return { i: Int ->
                    ++a
                    i + a
                }
            }
            var b: (Int) -> Int = higherOrderFunction()
            var c: Int = b(25)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(36, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun returnLambdaFromFunctionWithLocalVariableReadReference() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int): (Int) -> Int {
                return { i: Int ->
                    ++a
                    i + x + a
                }
            }
            var b: (Int) -> Int = higherOrderFunction(15)
            var c: (Int) -> Int = higherOrderFunction(25)
            var d: Int = b(25)
            var e: Int = c(6)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(5, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(51, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(43, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }

    @Test
    fun returnLambdaFromFunctionChainedCallsWithLocalVariableReadReference() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int): (Int) -> Int {
                return { i: Int ->
                    ++a
                    i + x + a
                }
            }
            var b: Int = higherOrderFunction(29)(20)
            var c: Int = higherOrderFunction(60)(30)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(12, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(60, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(102, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun returnLambdaFromFunctionWithLocalVariableWriteReference() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int): (Int) -> Int {
                var b: Int = 5
                return { i: Int ->
                    ++a
                    ++b
                    i + x + a + b
                }
            }
            var b: (Int) -> Int = higherOrderFunction(15)
            var c: (Int) -> Int = higherOrderFunction(25)
            var d: Int = b(25)
            var e: Int = c(6)
            var f: Int = b(25)
            var g: Int = c(6)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(57, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(49, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(60, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(52, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun returnLambdaFromFunctionWithLocalFunctionReference() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int): (Int) -> Int {
                var b: Int = 5
                fun f() {
                    ++b
                }
                
                return { i: Int ->
                    ++a
                    f()
                    i + x + a + b
                }
            }
            var b: (Int) -> Int = higherOrderFunction(15)
            var c: (Int) -> Int = higherOrderFunction(25)
            var d: Int = b(25)
            var e: Int = c(6)
            var f: Int = b(25)
            var g: Int = c(6)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(57, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(49, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(60, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(52, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun returnLambdaFromLambdaWithLocalReferences() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int): (Int) -> (() -> Int) {
                var b: Int = 5
                
                return { i: Int ->
                    ++a
                    ++b
                    val y: Int = x
                    
                    fun add(a: Int, b: Int): Int {
                        return a + b
                    }
                    
                    if (y > 100) {
                        { add(x, ++b) + y }
                    } else {
                        { add(x, --b) + y }
                    }
                }
            }
            var b: (Int) -> (() -> Int) = higherOrderFunction(50)
            var c: (Int) -> (() -> Int) = higherOrderFunction(20)
            var d: Int = b(200)()
            var e: Int = c(10)()
            var f: Int = b(200)()
            var g: Int = c(10)()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(105, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(105, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun returnLambdaFromLambdaWithLocalClassRef1() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int): (Int) -> (() -> Int) {
                var b: Int = 5
                
                class LocalClass(val la: Int, val lb: Int) {
                    fun calc(): Int {
                        return la + lb
                    }
                }
                
                return { i: Int ->
                    ++a
                    ++b
                    if (i > 100) {
                        { LocalClass(x, ++b).calc() }
                    } else {
                        { LocalClass(x, --b).calc() }
                    }
                }
            }
            var b: (Int) -> (() -> Int) = higherOrderFunction(50)
            var c: (Int) -> (() -> Int) = higherOrderFunction(20)
            var d: Int = b(200)()
            var e: Int = c(10)()
            var f: Int = b(200)()
            var g: Int = c(10)()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(57, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(59, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun returnLambdaFromLambdaWithLocalClassRef2() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int): (Int) -> (() -> Int) {
                var b: Int = 5
                
                class LocalClass(val la: Int, val lb: Int) {
                    fun calc(): Int {
                        return la + lb
                    }
                }
                
                return { i: Int ->
                    ++a
                    ++b
                    if (i > 100) {
                        val o: LocalClass = LocalClass(x, ++b)
                        {
                            val r: LocalClass = o
                            r.calc()
                        }
                    } else {
                        val o: LocalClass = LocalClass(x, --b)
                        {
                            val r: LocalClass = o
                            r.calc()
                        }
                    }
                }
            }
            var b: (Int) -> (() -> Int) = higherOrderFunction(50)
            var c: (Int) -> (() -> Int) = higherOrderFunction(20)
            var d: Int = b(200)()
            var e: Int = c(10)()
            var f: Int = b(200)()
            var g: Int = c(10)()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(57, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(59, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun returnLambdaFromLambdaWithLocalClassRef3() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int): (Int) -> (() -> Int) {
                var b: Int = 5
                
                class LocalClass(val la: Int, val lb: Int) {
                    fun calc(): Int {
                        return la + lb
                    }
                }
                
                fun makeLocalClass(a: Int, b: Int): LocalClass {
                    return LocalClass(a, b)
                }
                
                return { i: Int ->
                    ++a
                    ++b
                    if (i > 100) {
                        {
                            makeLocalClass(x, ++b).calc()
                        }
                    } else {
                        {
                            makeLocalClass(x, --b).calc()
                        }
                    }
                }
            }
            var b: (Int) -> (() -> Int) = higherOrderFunction(50)
            var c: (Int) -> (() -> Int) = higherOrderFunction(20)
            var d: Int = b(200)()
            var e: Int = c(10)()
            var f: Int = b(200)()
            var g: Int = c(10)()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(57, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(59, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun returnLambdaFromLambdaWithLocalClassRef4() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int): (Int) -> (() -> Int) {
                var b: Int = 5
                
                class LocalClass(val la: Int, val lb: Int) {
                    fun calc(): Int {
                        return la + lb
                    }
                }
                
                fun makeLocalClass(a: Int, b: Int): LocalClass {
                    return LocalClass(a, b)
                }
                
                fun calc(c: LocalClass): Int = c.calc()
                
                return { i: Int ->
                    ++a
                    ++b
                    if (i > 100) {
                        val o: LocalClass = makeLocalClass(x, ++b)
                        {
                            calc(o)
                        }
                    } else {
                        val o: LocalClass = makeLocalClass(x, --b)
                        {
                            calc(o)
                        }
                    }
                }
            }
            var b: (Int) -> (() -> Int) = higherOrderFunction(50)
            var c: (Int) -> (() -> Int) = higherOrderFunction(20)
            var d: Int = b(200)()
            var e: Int = c(10)()
            var f: Int = b(200)()
            var g: Int = c(10)()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(57, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(59, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(25, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun returnLambdaFromLambdaWithLocalClass() {
        val interpreter = interpreter("""
            var a: Int = 10
            fun higherOrderFunction(x: Int): (Int) -> (() -> Int) {
                var b: Int = 5
                
                return { i: Int ->
                    ++a
                    ++b
                    if (i > 100) {
                        {
                            class LocalClass(val la: Int, val lb: Int) {
                                fun calc(): Int {
                                    return la + lb
                                }
                            }
                            
                            LocalClass(x, ++b).calc()
                        }
                    } else {
                        {
                            class LocalClass(val la: Int, val lb: Int) {
                                fun calc(): Int {
                                    return - la - lb
                                }
                            }
                            LocalClass(x, --b).calc()
                        }
                    }
                }
            }
            var b: (Int) -> (() -> Int) = higherOrderFunction(50)
            var c: (Int) -> (() -> Int) = higherOrderFunction(20)
            var d: Int = b(200)()
            var e: Int = c(10)()
            var f: Int = b(200)()
            var g: Int = c(10)()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(57, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-25, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(59, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(-25, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun returnLambdaFromClassFunctionWithClassMemberAccess1() {
        val interpreter = interpreter("""
            var a: Int = 10
            class A(var y: Int) {
                var z: Int = 2
                fun higherOrderFunction(x: Int): (Int) -> Int {
                    var b: Int = 5
                    return { i: Int ->
                        ++a
                        ++y
                        this.z += 2
                        i + x + y + z + a + b
                    }
                }
            }
            var b: (Int) -> Int = A(100).higherOrderFunction(15)
            var c: (Int) -> Int = A(200).higherOrderFunction(25)
            var d: Int = b(25)
            var e: Int = c(6)
            var f: Int = b(25)
            var g: Int = c(6)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(161, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(253, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(166, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(258, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun returnLambdaFromClassFunctionWithClassMemberAccess2() {
        val interpreter = interpreter("""
            var a: Int = 10
            class A(var y: Int) {
                var z: Int = 2
                var var1: Int = 0
                var var2: Int = 20
                
                fun f1() {
                    ++var1
                }
                
                fun f2() {
                    ++this.var2
                }
                
                fun higherOrderFunction(x: Int): (Int) -> Int {
                    var b: Int = 5
                    return { i: Int ->
                        ++a
                        ++y
                        this.z += 2
                        f1()
                        this.f2()
                        i + x + y + z + a + b + var1 + var2
                    }
                }
            }
            var b: (Int) -> Int = A(100).higherOrderFunction(15)
            var c: (Int) -> Int = A(200).higherOrderFunction(25)
            var d: Int = b(25)
            var e: Int = c(6)
            var f: Int = b(25)
            var g: Int = c(6)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(183, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(275, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(190, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(282, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun returnNestedLambdaFromClassFunctionWithClassMemberAccess() {
        val interpreter = interpreter("""
            class MyCls(val value: Int) {
                fun getValueReader(): () -> (() -> Int) = {
                    {
                        val x: Int = value
                        x
                    }
                }
            }
            val o = MyCls(10)
            val a: Int = o.getValueReader()()()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun executeLambdaReturnedBySameClass() {
        val interpreter = interpreter("""
            var a: Int = 10
            class A(var y: Int) {
                var z: Int = 2
                var var1: Int = 0
                var var2: Int = 20
                
                fun f1() {
                    ++var1
                }
                
                fun f2() {
                    ++this.var2
                }
                
                fun higherOrderFunction(x: Int): (Int) -> Int {
                    var b: Int = 5
                    return { i: Int ->
                        ++a
                        ++y
                        ++b
                        this.z += 2
                        f1()
                        this.f2()
                        i + x + y + z + a + b + var1 + var2
                    }
                }
                
                fun execute(x: Int, z: Int): Int {
                    val f: (Int) -> Int = higherOrderFunction(2 * x)
                    return f(z)
                }
            }
            var b: A = A(100)
            var c: A = A(200)
            var d: Int = b.execute(25, 15)
            var e: Int = c.execute(6, 9)
            var f: Int = b.execute(25, 100)
            var g: Int = c.execute(6, 60)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(7, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(209, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(266, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(301, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(324, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun executeLambdaReturnedByDifferentClass() {
        val interpreter = interpreter("""
            var a: Int = 10
            class A(var y: Int) {
                var z: Int = 2
                var var1: Int = 0
                var var2: Int = 20
                
                fun f1() {
                    ++var1
                }
                
                fun f2() {
                    ++this.var2
                }
                
                fun higherOrderFunction(x: Int): (Int) -> Int {
                    var b: Int = 5
                    return { i: Int ->
                        ++a
                        ++y
                        ++b
                        this.z += 2
                        f1()
                        this.f2()
                        i + x + y + z + a + b + var1 + var2
                    }
                }
            }
            class B(var y: Int = 17, val a: A) {
                var x: Int = 10
                var z: Int = 21
                var var1: Int = 29
                var var2: Int = 33
                var b: Int = 36
                
                fun execute(x: Int, z: Int): Int {
                    val f: (Int) -> Int = a.higherOrderFunction(2 * x)
                    return f(z)
                }
                
                fun check(): Int = x + y + z + var1 + var2 + b
            }
            var b: B = B(a = A(100))
            var c: B = B(a = A(200))
            var d: Int = b.execute(25, 15)
            var e: Int = c.execute(6, 9)
            var f: Int = b.execute(25, 100)
            var g: Int = c.execute(6, 60)
            var h: Int = b.check()
            var i: Int = c.check()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(9, symbolTable.propertyValues.size)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(209, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(266, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(301, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(324, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
        assertEquals(146, (symbolTable.findPropertyByDeclaredName("h") as IntValue).value)
        assertEquals(146, (symbolTable.findPropertyByDeclaredName("i") as IntValue).value)
    }

    @Test
    fun invokeLambdaDirectly() {
        val interpreter = interpreter("""
            var a: Int = 0
            val b: Int = { x: Int ->
                var i: Int = x
                while (i > 1) {
                    a += 2
                    --i
                }
                i
            }(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(18, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun recursiveLambdas() {
        val interpreter = interpreter("""
            var a: Int = 0
            fun f(x: Int): () -> Int {
                if (x < 1) return { 0 }
                return {
                    ++a
                    x + f(x-1)()
                }
            }
            var b: Int = f(10)()
            var c: Int = f(50)()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(60, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals((1 + 10) * 10 / 2, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals((1 + 50) * 50 / 2, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun manyReturnLevelsOfCallablesInReturn() {
        val interpreter = interpreter("""
            fun f(x: Int): () -> (() -> (() -> (() -> (() -> Int)))) {
                return {
                    val x: Int = x - 1
                    {
                        val x: Int = x - 1
                        {
                            val x: Int = x - 1
                            {
                                val x: Int = x - 1
                                {
                                    x - 1
                                }
                            }
                        }
                    }
                }
            }
            var b: Int = f(10)()()()()()
            var c: Int = f(50)()()()()()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun manyReturnLevelsOfCallablesInArgument() {
        val interpreter = interpreter("""
            fun f(x: Int): () -> (() -> (() -> (() -> (() -> Int)))) {
                return {
                    val x: Int = x - 1
                    {
                        val x: Int = x - 1
                        {
                            val x: Int = x - 1
                            {
                                val x: Int = x - 1
                                {
                                    x - 1
                                }
                            }
                        }
                    }
                }
            }
            fun g(function: () -> (() -> (() -> (() -> (() -> Int))))): Int {
                return function()()()()()
            }
            var b: Int = g(f(10))
            var c: Int = g(f(50))
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun manyArgumentLevelsOfCallablesInReturn() {
        val interpreter = interpreter("""
            fun f(x: Int): ((Int) -> Int) -> Int {
                return { g: (Int) -> Int ->
                    g(2 * x)
                }
            }
            val b: ((Int) -> Int) -> Int = f(10)
            val c: Int = b({x: Int -> 2*x})
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(40, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    @Ignore
    fun manyArgumentLevelsOfCallablesInReturn2() {
        val interpreter = interpreter("""
            var a: Int = 0
            fun f(x: Int): (((((Int) -> Unit) -> Unit) -> Unit) -> Unit) -> Int {
                return { f: ((((Int) -> Unit) -> Unit) -> Unit) -> Unit ->
                    var b: Int = 0
                    x
                }
            }
            var b: Int = f(10)({x ->
                a += x
                {x ->
                    a+=x
                    {x ->
                        a+=x
                        {x ->
                            a+=x
                        }
                    }
                }
            })
            var c: Int = f(50)()()()()()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun nullableCallableInReturn() {
        val interpreter = interpreter("""
            fun f(x: Int, g: ((Int) -> Int)? = null): Int {
                if (g == null) {
                    return x
                }
                return g!!(x)
            }
            var b: Int = f(10, {x: Int -> x * 2})
            var c: Int = f(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun lastLambdaArgumentShorthandSyntax1() {
        val interpreter = interpreter("""
            fun f(x: Int, g: ((Int) -> Int)? = null): Int {
                if (g == null) {
                    return x
                }
                return g!!(x)
            }
            var b: Int = f(10) {x: Int -> x * 2}
            var c: Int = f(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun lastLambdaArgumentShorthandSyntax2() {
        val interpreter = interpreter("""
            fun f(x: Int, g: ((Int) -> Int)? = null): Int {
                if (g == null) {
                    return x
                }
                return g!!(x)
            }
            var b: Int = f(10) { x: Int ->
                x * 2
            }
            var c: Int = f(10)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun aloneLambdaArgumentShorthandSyntax() {
        val interpreter = interpreter("""
            val x: Int = 10
            fun f(g: ((Int) -> Int)? = null): Int {
                if (g == null) {
                    return x
                }
                return g!!(x)
            }
            var b: Int = f { x: Int ->
                x * 2
            }
            var c: Int = f()
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(3, symbolTable.propertyValues.size)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun underscoreArgumentsInLambda() {
        val interpreter = interpreter("""
            fun f(g: ((Int, Int, Int, Int) -> Int)? = null): Int {
                if (g == null) {
                    return 1
                }
                return g!!(2, 5, 11, 19)
            }
            val x = f { x, _, _, y ->
                x + y
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(21, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun implicitItArgument1() {
        val interpreter = interpreter("""
            fun f(g: (Int) -> Int): Int {
                return g(4) + g(5)
            }
            val x = f { it * 2 }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(1, symbolTable.propertyValues.size)
        assertEquals(18, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }

    @Test
    fun implicitItArgument2() {
        val interpreter = interpreter("""
            fun f(g: (Int) -> Int): Int {
                return g(4) + g(5)
            }
            val lambda: (Int) -> Int = { it * 2 }
            val x = f(lambda)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(18, (symbolTable.findPropertyByDeclaredName("x") as IntValue).value)
    }
}
