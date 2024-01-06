package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
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
}
