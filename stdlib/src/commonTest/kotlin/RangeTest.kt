import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.IOLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.RangeLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

class RangeTest {

    @Test
    fun customClassClosedRangeContainsOperator() {
        val env = ExecutionEnvironment().apply {
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            class A(val x: Int) : Comparable<A> {
                override operator fun compareTo(o: A): Int {
                    return x.compareTo(o.x)
                }
            }
            val r = A(3)..A(15)
            val typeCheck: Boolean = r is ClosedRange<A>
            val a: Boolean = A(2) in r
            val b: Boolean = A(3) in r
            val c: Boolean = A(4) in r
            val d: Boolean = A(12) in r
            val e: Boolean = A(14) in r
            val f: Boolean = A(15) in r
            val g: Boolean = A(16) in r
            val h: Boolean = A(500) in r
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
    }

    @Test
    fun customClassOpenEndRangeContainsOperator() {
        val env = ExecutionEnvironment().apply {
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            class A(val x: Int) : Comparable<A> {
                override operator fun compareTo(o: A): Int {
                    return x.compareTo(o.x)
                }
            }
            val r = A(3)..<A(15)
            val typeCheck: Boolean = r is OpenEndRange<A>
            val a: Boolean = A(2) in r
            val b: Boolean = A(3) in r
            val c: Boolean = A(4) in r
            val d: Boolean = A(12) in r
            val e: Boolean = A(14) in r
            val f: Boolean = A(15) in r
            val g: Boolean = A(16) in r
            val h: Boolean = A(500) in r
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
    }

    @Test
    fun customClassClosedRangeStartEndInclusiveProperty() {
        val env = ExecutionEnvironment().apply {
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            class A(val x: Int) : Comparable<A> {
                override operator fun compareTo(o: A): Int {
                    return x.compareTo(o.x)
                }
            }
            val r = A(3)..A(15)
            val typeCheck: Boolean = r is ClosedRange<A>
            val a: Int = r.start.x
            val b: Int = r.endInclusive.x
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun customClassOpenEndRangeStartEndInclusiveProperty() {
        val env = ExecutionEnvironment().apply {
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            class A(val x: Int) : Comparable<A> {
                override operator fun compareTo(o: A): Int {
                    return x.compareTo(o.x)
                }
            }
            val r = A(3)..<A(15)
            val typeCheck: Boolean = r is OpenEndRange<A>
            val a: Int = r.start.x
            val b: Int = r.endExclusive.x
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun intClosedRangeContainsOperatorFirstLastProperty() {
        val env = ExecutionEnvironment().apply {
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            val r = 3..15
            val typeCheck: Boolean = r is IntRange
            val a: Boolean = 2 in r
            val b: Boolean = 3 in r
            val c: Boolean = 4 in r
            val d: Boolean = 12 in r
            val e: Boolean = 14 in r
            val f: Boolean = 15 in r
            val g: Boolean = 16 in r
            val h: Boolean = 500 in r
            val start: Int = r.first
            val end: Int = r.last
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("start") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("end") as IntValue).value)
    }

    @Test
    fun intOpenEndRangeContainsOperatorFirstLastProperty() {
        val env = ExecutionEnvironment().apply {
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            val r = 3..<15
            val typeCheck: Boolean = r is IntRange
            val a: Boolean = 2 in r
            val b: Boolean = 3 in r
            val c: Boolean = 4 in r
            val d: Boolean = 12 in r
            val e: Boolean = 14 in r
            val f: Boolean = 15 in r
            val g: Boolean = 16 in r
            val h: Boolean = 500 in r
            val start: Int = r.first
            val end: Int = r.last
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("start") as IntValue).value)
        assertEquals(14, (symbolTable.findPropertyByDeclaredName("end") as IntValue).value)
    }

    @Test
    fun intClosedRangeForLoop() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            for (e in 3..15) {
                println(e)
                println(e * 2)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals((3..15).joinToString("") { "$it\n${it * 2}\n" }, console.toString())
    }

    @Test
    fun intClosedRangeForEach() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(CollectionsLibModule())
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            (3..15).forEach { e ->
                println(e)
                println(e * 2)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals((3..15).joinToString("") { "$it\n${it * 2}\n" }, console.toString())
    }

    @Test
    fun intOpenEndRangeForLoop() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            for (e in 3..<15) {
                println(e)
                println(e * 2)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals((3..<15).joinToString("") { "$it\n${it * 2}\n" }, console.toString())
    }

    @Test
    fun intOpenEndRangeForEach() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(CollectionsLibModule())
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            (3..<15).forEach { e ->
                println(e)
                println(e * 2)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals((3..<15).joinToString("") { "$it\n${it * 2}\n" }, console.toString())
    }

    @Test
    fun intProgressionForLoopDownToStep() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(RangeLibModule())
        }
        val interpreter = interpreter("""
            for (e in 12 downTo -6 step 3) {
                println(e)
                println(e + 1)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals("12\n13\n9\n10\n6\n7\n3\n4\n0\n1\n-3\n-2\n-6\n-5\n", console.toString())
    }
}
