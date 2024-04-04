import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NumberValue
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
    fun intLongClosedRangeContainsOperatorFirstLastProperty() {
        listOf("Int", "Long").forEach { type ->
            val l = if (type == "Long") "L" else ""
            val cast: Int.() -> Number = { if (type == "Long") toLong() else this }
            val env = ExecutionEnvironment().apply {
                install(RangeLibModule())
            }
            val interpreter = interpreter("""
                val r = 3$l..15$l
                val typeCheck: Boolean = r is ${type}Range
                val a: Boolean = 2$l in r
                val b: Boolean = 3$l in r
                val c: Boolean = 4$l in r
                val d: Boolean = 12$l in r
                val e: Boolean = 14$l in r
                val f: Boolean = 15$l in r
                val g: Boolean = 16$l in r
                val h: Boolean = 500$l in r
                val start: $type = r.first
                val end: $type = r.last
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
            assertEquals(3.cast(), (symbolTable.findPropertyByDeclaredName("start") as NumberValue<*>).value)
            assertEquals(15.cast(), (symbolTable.findPropertyByDeclaredName("end") as NumberValue<*>).value)
        }
    }

    @Test
    fun intLongOpenEndRangeContainsOperatorFirstLastProperty() {
        listOf("Int", "Long").forEach { type ->
            val l = if (type == "Long") "L" else ""
            val cast: Int.() -> Number = { if (type == "Long") toLong() else this }
            val env = ExecutionEnvironment().apply {
                install(RangeLibModule())
            }
            val interpreter = interpreter("""
                val r = 3$l..<15$l
                val typeCheck: Boolean = r is ${type}Range
                val a: Boolean = 2$l in r
                val b: Boolean = 3$l in r
                val c: Boolean = 4$l in r
                val d: Boolean = 12$l in r
                val e: Boolean = 14$l in r
                val f: Boolean = 15$l in r
                val g: Boolean = 16$l in r
                val h: Boolean = 500$l in r
                val start: $type = r.first
                val end: $type = r.last
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
            assertEquals(3.cast(), (symbolTable.findPropertyByDeclaredName("start") as NumberValue<*>).value)
            assertEquals(14.cast(), (symbolTable.findPropertyByDeclaredName("end") as NumberValue<*>).value)
        }
    }

    @Test
    fun intLongClosedRangeForLoop() {
        listOf("Int", "Long").forEach { type ->
            val l = if (type == "Long") "L" else ""
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
                for (e in 3$l..15$l) {
                    println(e)
                    println(e * 2)
                }
            """.trimIndent(), executionEnvironment = env, isDebug = true)
            interpreter.eval()
            assertEquals((3..15).joinToString("") { "$it\n${it * 2}\n" }, console.toString())
        }
    }

    @Test
    fun intLongClosedRangeForEach() {
        listOf("Int", "Long").forEach { type ->
            val l = if (type == "Long") "L" else ""
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
                (3$l..15$l).forEach { e ->
                    println(e)
                    println(e * 2$l)
                }
            """.trimIndent(), executionEnvironment = env, isDebug = true)
            interpreter.eval()
            assertEquals((3..15).joinToString("") { "$it\n${it * 2}\n" }, console.toString())
        }
    }

    @Test
    fun intLongOpenEndRangeForLoop() {
        listOf("Int", "Long").forEach { type ->
            val l = if (type == "Long") "L" else ""
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
                for (e in 3$l..<15$l) {
                    println(e)
                    println(e * 2)
                }
            """.trimIndent(), executionEnvironment = env, isDebug = true)
            interpreter.eval()
            assertEquals((3..<15).joinToString("") { "$it\n${it * 2}\n" }, console.toString())
        }
    }

    @Test
    fun intLongOpenEndRangeForEach() {
        listOf("Int", "Long").forEach { type ->
            val l = if (type == "Long") "L" else ""
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
                (3$l..<15$l).forEach { e ->
                    println(e)
                    println(e * 2$l)
                }
            """.trimIndent(), executionEnvironment = env, isDebug = true)
            interpreter.eval()
            assertEquals((3..<15).joinToString("") { "$it\n${it * 2}\n" }, console.toString())
        }
    }

    @Test
    fun intLongProgressionForLoopDownToStep() {
        listOf("Int", "Long").forEach { type ->
            val l = if (type == "Long") "L" else ""
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
                for (e in 12$l downTo -6$l step 3$l) {
                    println(e)
                    println(e + 1)
                }
            """.trimIndent(), executionEnvironment = env, isDebug = true)
            interpreter.eval()
            assertEquals("12\n13\n9\n10\n6\n7\n3\n4\n0\n1\n-3\n-2\n-6\n-5\n", console.toString())
        }
    }

    @Test
    fun intLongProgressionForLoopUntil() {
        listOf("Int", "Long").forEach { type ->
            val l = if (type == "Long") "L" else ""
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
                for (e in 12$l until 16$l) {
                    println(e)
                    println(e * 2$l)
                }
            """.trimIndent(), executionEnvironment = env, isDebug = true)
            interpreter.eval()
            assertEquals("12\n24\n13\n26\n14\n28\n15\n30\n", console.toString())
        }
    }
}
