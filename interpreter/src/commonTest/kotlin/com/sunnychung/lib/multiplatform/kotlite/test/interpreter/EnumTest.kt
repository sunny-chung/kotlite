package com.sunnychung.lib.multiplatform.kotlite.test.interpreter

import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.ListValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class EnumTest {

    @Test
    fun accessEnum1() {
        val interpreter = interpreter("""
            enum class MyEnum {
                A, B, C, D
            }
            fun f(e: MyEnum): Int = when(e) {
                MyEnum.A, MyEnum.C -> 11
                MyEnum.B -> 29
                else -> 19
            }
            val e1: MyEnum = MyEnum.C
            val e2: MyEnum = MyEnum.B
            val x1 = f(e1)
            val x2 = f(e2)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("x1") as IntValue).value)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("x2") as IntValue).value)
    }

    @Test
    fun accessEnum2() {
        val interpreter = interpreter("""
            enum class MyEnum {
                A, B, C(), D
            }
            fun f(e: MyEnum): Int {
                if (e == MyEnum.C) {
                    return 11
                }
                if (e != MyEnum.B) {
                    return 19
                }
                return 29
            }
            val e1: MyEnum = MyEnum.C
            val e2: MyEnum = MyEnum.B
            val x1 = f(e1)
            val x2 = f(e2)
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(4, symbolTable.propertyValues.size)
        assertEquals(11, (symbolTable.findPropertyByDeclaredName("x1") as IntValue).value)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("x2") as IntValue).value)
    }

    @Test
    fun enumWithValues() {
        val interpreter = interpreter("""
            enum class MyEnum(val customName: String, val value: Int = 8) {
                A("a", 15),
                B(customName = "bb", value = 2),
                C(value = 20, customName = "ccc"),
                D("dddd"),
            }
            val a: MyEnum = MyEnum.A
            val b: MyEnum = MyEnum.B
            val c: MyEnum = MyEnum.C
            val d: MyEnum = MyEnum.D
            val na = a.customName
            val nb = b.customName
            val nc = c.customName
            val nd = d.customName
            val va = a.value
            val vb = b.value
            val vc = c.value
            val vd = d.value
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(12, symbolTable.propertyValues.size)
        assertEquals("a", (symbolTable.findPropertyByDeclaredName("na") as StringValue).value)
        assertEquals("bb", (symbolTable.findPropertyByDeclaredName("nb") as StringValue).value)
        assertEquals("ccc", (symbolTable.findPropertyByDeclaredName("nc") as StringValue).value)
        assertEquals("dddd", (symbolTable.findPropertyByDeclaredName("nd") as StringValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("va") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("vb") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("vc") as IntValue).value)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("vd") as IntValue).value)
    }

    @Test
    fun valueOf() {
        val interpreter = interpreter("""
            enum class MyEnum(val customName: String, val value: Int = 8) {
                A("a", 15),
                B(customName = "bb", value = 2),
                C(value = 20, customName = "ccc"),
                D("dddd"),
            }
            val a: MyEnum = MyEnum.valueOf("A")
            val b: MyEnum = MyEnum.valueOf("B")
            val c: MyEnum = MyEnum.valueOf("C")
            val d: MyEnum = MyEnum.valueOf("D")
            val na = a.customName
            val nb = b.customName
            val nc = c.customName
            val nd = d.customName
            val va = a.value
            val vb = b.value
            val vc = c.value
            val vd = d.value
            val e = try {
                MyEnum.valueOf("not exist")
            } catch (_: Exception) {
                null
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(13, symbolTable.propertyValues.size)
        assertEquals("a", (symbolTable.findPropertyByDeclaredName("na") as StringValue).value)
        assertEquals("bb", (symbolTable.findPropertyByDeclaredName("nb") as StringValue).value)
        assertEquals("ccc", (symbolTable.findPropertyByDeclaredName("nc") as StringValue).value)
        assertEquals("dddd", (symbolTable.findPropertyByDeclaredName("nd") as StringValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("va") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("vb") as IntValue).value)
        assertEquals(20, (symbolTable.findPropertyByDeclaredName("vc") as IntValue).value)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("vd") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun enumEntries() {
        val interpreter = interpreter("""
            enum class MyEnum(val customName: String, val value: Int = 8) {
                A("a", 15),
                B(customName = "bb", value = 2),
                C(value = 20, customName = "ccc"),
                D("dddd"),
            }
            val allValues: List<MyEnum> = MyEnum.entries
            var sum = 0
            for (e in allValues) {
                sum += e.value
            }
        """.trimIndent())
        interpreter.eval()
        val symbolTable = interpreter.callStack.currentSymbolTable()
        println(symbolTable.propertyValues)
        assertEquals(2, symbolTable.propertyValues.size)
        assertEquals(15 + 2 + 20 + 8, (symbolTable.findPropertyByDeclaredName("sum") as IntValue).value)
    }
}
