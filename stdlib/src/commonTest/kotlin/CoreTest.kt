import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.LongValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CoreLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.IOLibModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CoreTest {

    @Test
    fun pairToList() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
        }
        val interpreter = interpreter("""
            var counter = 0
            val p: Pair<Int, Int> = Pair(3, 6)
            fun pairToList(p: Pair<Int, Int>): List<Int> {
                ++counter
                return p.toList()
            }
            for (e in pairToList(p)) {
                println(e)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals("3\n6\n", console.toString())
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("counter") as IntValue).value)
    }

    @Test
    fun repeat() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            var a = 0
            repeat(10) { a += it }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(45, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun doubleFiniteInfinite() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            val a = 3.14.isFinite()
            val b = 3.14.isInfinite()
            val c = 3.14.isNaN()
            val d = (1.0 / 0.0).isFinite()
            val e = (1.0 / 0.0).isInfinite()
            val f = (1.0 / 0.0).isNaN()
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
    }

    @Test
    fun numberTypeConversions() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            val a = 3.14.toInt()
            val b = a.toLong()
            val c = b.toDouble()
            val d = a is Int
            val e = b is Long
            val f = c is Double
            val g = a is Double
            val h = b is Int
            val i = c is Long
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(3L, (symbolTable.findPropertyByDeclaredName("b") as LongValue).value)
        compareNumber(3.0, symbolTable.findPropertyByDeclaredName("c") as DoubleValue)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("e") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("f") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("g") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("h") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("i") as BooleanValue).value)
    }

    @Test
    fun repeatTakeIf() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            var a = 0
            repeat(10) {
                a += it.takeIf { it % 2 == 1 } ?: 0
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(1 + 3 + 5 + 7 + 9, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun check() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            var a = 0
            check(true)
            ++a
            check(true) { "error message" }
            ++a
            check(false)
            ++a
            check(false) { "error message" }
            ++a
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        assertFailsWith<IllegalStateException> {
            interpreter.eval()
        }
        val symbolTable = interpreter.symbolTable()
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun require() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            var a = 0
            require(true) { "error message" }
            ++a
            require(true)
            ++a
            require(false) { "error message" }
            ++a
            require(false)
            ++a
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        assertFailsWith<IllegalArgumentException> {
            interpreter.eval()
        }
        val symbolTable = interpreter.symbolTable()
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun checkNotNull() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            fun f(x: Int) = if (x > 0) x else null
            var a = 0
            val b = checkNotNull(f(123))
            ++a
            val c = checkNotNull(f(234)) { "error message" }
            ++a
            val d = checkNotNull(f(-999))
            ++a
            val e = checkNotNull(f(-9990)) { "error message" }
            ++a
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        assertFailsWith<IllegalStateException> {
            interpreter.eval()
        }
        val symbolTable = interpreter.symbolTable()
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun TODO() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            var a = 0
            TODO()
            ++a
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        assertFailsWith<NotImplementedError> {
            interpreter.eval()
        }
        val symbolTable = interpreter.symbolTable()
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }
}
