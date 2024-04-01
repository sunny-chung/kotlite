import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CoreLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.IOLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

class CoreScopeFunctionTest {

    @Test
    fun objectAlso() {
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
            class A(var x: Int)
            val t = A(29)
                .also { println(it.x) }
                .also { it.x *= 2 }
                .also { println(it.x) }
            val typeCheck = t is A
            val b = t.x
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(58, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("29\n58\n", console.toString())
    }

    @Test
    fun objectAlsoLet() {
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
            class A(val x: Int)
            val t = A(29)
                .also { println(it.x) }
                .let { A(it.x * 2) }
                .also { println(it.x) }
                .let { A(it.x + 1) }
                .let { A(it.x * 10) }
                .also { println(it.x) }
            val typeCheck = t is A
            val b = t.x
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(590, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("29\n58\n590\n", console.toString())
    }

    @Test
    fun intAlsoLet() {
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
            val t = 29
                .also { println(it) }
                .let { x -> x * 2 }
                .also { x -> println(x) }
                .let { it + 1 }
                .let { it * 10 }
                .also { println(it) }
            val typeCheck = t is Int
            val b = t
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(590, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("29\n58\n590\n", console.toString())
    }

    @Test
    fun objectApplyRun() {
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
            class A(val x: Int)
            val t = A(29)
                .apply { println(x) }
                .run { A(x * 2) }
                .apply { println(this.x) }
                .run { A(this.x + 1) }
                .run { A(x * 10) }
                .apply { println(x) }
            val typeCheck = t is A
            val b = t.x
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(590, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("29\n58\n590\n", console.toString())
    }

    @Test
    fun intApplyRun() {
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
            val t = 29
                .apply { println(this) }
                .run { this * 2 }
                .apply { println(this) }
                .run { this + 1 }
                .run { this * 10 }
                .apply { println(this) }
            val typeCheck = t is Int
            val b = t
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(590, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("29\n58\n590\n", console.toString())
    }

    @Test
    fun runWithoutReceiver() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            val t = run {
                var t = 123
                t *= 3
                t
            }
            val typeCheck = t is Int
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(369, (symbolTable.findPropertyByDeclaredName("t") as IntValue).value)
    }

    @Test
    fun with() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            class A(var x: Int, val y: Int)
            val o = A(x = 29, y = 25)
            val t = with (o) {
                x += y
                y * 3
            }
            val typeCheck = t is Int
            val b = o.x
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(75, (symbolTable.findPropertyByDeclaredName("t") as IntValue).value)
        assertEquals(54, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun nestedWith() {
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
        }
        val interpreter = interpreter("""
            class A(var x: Int, val y: Int)
            class B(var x: Int)
            val oa = A(x = 29, y = 25)
            val ob = B(x = 6)
            val t = with (oa) {
                with (ob) {
                    x += y
                }
                y * 3
            }
            val typeCheck = t is Int
            val b = oa.x
            val c = ob.x
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(75, (symbolTable.findPropertyByDeclaredName("t") as IntValue).value)
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(31, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun nullableObjectAlsoLet1() {
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
            class A(val x: Int)
            fun f(x: Int) = if (x > 0) {
                A(x)
            } else {
                null
            }
            fun g(x: Int) = f(x)
                ?.also { println(it.x) }
                ?.let { A(it.x * 2) }
                ?.also { println(it.x) }
                ?.let { A(it.x + 1) }
                ?.let { A(it.x * 10) }
                ?.let { it.x }
                ?.also { println(it) }
                ?: -1
            val a = g(29)
            val b = g(-20)
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(590, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("29\n58\n590\n", console.toString())
    }

    @Test
    fun nullableObjectAlsoLet2() {
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
            class A(val x: Int)
            fun f(x: Int) = if (x > 0) {
                A(x)
            } else {
                null
            }
            fun g(x: Int) = f(x)
                .also { println(it?.x) }
                .let { it?.let { A(it.x * 2) } }
                .also { println(it?.x) }
                .let { it?.let { A(it.x + 1) } }
                .let { it?.let { A(it.x * 10) } }
                .let { it?.x }
                ?.also { println(it) }
                ?: -1
            val a = g(29)
            val b = g(-20)
        """.trimIndent(), executionEnvironment = env)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(590, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("29\n58\n590\nnull\nnull\n", console.toString())
    }
}
