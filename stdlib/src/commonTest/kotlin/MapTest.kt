import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CoreLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.IOLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.TextLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

class MapTest {
    @Test
    fun simpleRead() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val m = mapOf(
                "ab" to 123,
                "bcd" to 234,
                "e" to 345,
                "fghi" to 56789,
            )
            val size: Int = m.size
            val typeCheck1: Boolean = m is Map<String, Int>
            val typeCheck2: Boolean = m is Map<Int, String>
            val a: Int? = m["ab"]
            val b: Int? = m["bcd"]
            val c: Int? = m["e"]
            val d: Int? = m["fghi"]
            val e: Int? = m["notexist"]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2") as BooleanValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(345, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(56789, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun listToMap() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            class Data<A, B>(val key: A, val value: B)
            val m = listOf(
                Data("ab", 123),
                Data("bcd", 234),
                Data("e", 345),
                Data("fghi", 56789),
            ).map { Pair(it.key, it.value) }
            .toMap<String, Int>() // TODO remove diamand
            val size: Int = m.size
            val typeCheck1: Boolean = m is Map<String, Int>
            val typeCheck2: Boolean = m is Map<Int, String>
            val a: Int? = m["ab"]
            val b: Int? = m["bcd"]
            val c: Int? = m["e"]
            val d: Int? = m["fghi"]
            val e: Int? = m["notexist"]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2") as BooleanValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(345, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(56789, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun listAssociateToMap() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            class Data<A, B>(val key: A, val value: B)
            val m = listOf(
                Data("ab", 123),
                Data("bcd", 234),
                Data("e", 345),
                Data("fghi", 56789),
            ).associate { it.key to it.value }
            val size: Int = m.size
            val typeCheck1: Boolean = m is Map<String, Int>
            val typeCheck2: Boolean = m is Map<Int, String>
            val a: Int? = m["ab"]
            val b: Int? = m["bcd"]
            val c: Int? = m["e"]
            val d: Int? = m["fghi"]
            val e: Int? = m["notexist"]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2") as BooleanValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(345, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(56789, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
    }

    @Test
    fun listGroupBy() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            class Data<A, B>(val key: A, val value: B)
            val m = listOf(
                Data("ab", 123),
                Data("bcd", 234),
                Data("e", 345),
                Data("ab", 3),
                Data("fghi", 56789),
                Data("e", 8),
                Data("e", 9),
                Data("fghi", 12),
            ).groupBy(
                keySelector = { it.key },
                valueSelector = { it.value },
            )
            val size: Int = m.size
            val typeCheck1: Boolean = m is Map<String, List<Int>>
            val typeCheck2: Boolean = m is Map<String, Int>
            val a: Int? = m["ab"]?.size
            val b: Int? = m["bcd"]?.size
            val c: Int? = m["e"]?.size
            val d: Int? = m["fghi"]!!.size
            val e: Int = m["notexist"]?.size ?: -1
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2") as BooleanValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }

    @Test
    fun simpleWrite() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val m = mutableMapOf(
                "ab" to 123,
                "bcd" to 234,
                "e" to 345,
                "fghi" to 56789,
            )
            val size1: Int = m.size
            val typeCheck1a: Boolean = m is MutableMap<String, Int>
            val typeCheck1b: Boolean = m is Map<String, Int>
            val typeCheck2a: Boolean = m is MutableMap<Int, String>
            val typeCheck2b: Boolean = m is Map<Int, String>
            val a: Int? = m["ab"]
            val b: Int? = m["bcd"]
            val c: Int? = m["e"]
            val d: Int? = m["fghi"]
            val e: Int? = m["notexist"]
            
            var counter = 100
            m["bcd"] = ++counter
            m["ab"] = ++counter
            m.put("bcd", ++counter)
            m["hi"] = ++counter
            val size2: Int = m.size
            val f: Int? = m["ab"]
            val g: Int? = m["bcd"]
            val h: Int? = m["e"]
            val i: Int? = m["fghi"]
            val j: Int? = m["hi"]
            val k: Int? = m["notexist"]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size1") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1b") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2b") as BooleanValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(345, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(56789, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))

        assertEquals(5, (symbolTable.findPropertyByDeclaredName("size2") as IntValue).value)
        assertEquals(102, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(103, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
        assertEquals(345, (symbolTable.findPropertyByDeclaredName("h") as IntValue).value)
        assertEquals(56789, (symbolTable.findPropertyByDeclaredName("i") as IntValue).value)
        assertEquals(104, (symbolTable.findPropertyByDeclaredName("j") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("k"))
    }

    @Test
    fun mutableMapOperations() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            fun f(x: Int) = if (x >= 0) mapOf(30 to 100) else null
            val m = f(-1).orEmpty().toMutableMap()
            val typeCheck1: Boolean = m is MutableMap<Int, Int>
            
            var counter = 0
            
            m.putAll(listOf(
                11 to ++counter,
                12 to ++counter,
                13 to ++counter,
                14 to ++counter,
                15 to ++counter,
            ))
            m.remove(13)
            
            val a: Int = m.getOrPut(12) { ++counter }
            val b: Int = m.getOrPut(29) { ++counter }
            val c: Int = m.getOrPut(20) { ++counter }
            val d: Int = m[13] ?: -1
            val e: Int = m[30] ?: -1
            
            val m2 = m.toMap()
            val typeCheck2a: Boolean = m2 is MutableMap<Int, Int>
            val typeCheck2b: Boolean = m2 is Map<Int, Int>
            
            val f: Int = m.getOrElse(15) { -5 } 
            val g: Int = m.getOrElse(25) { -5 } 
            
            val size1 = m.size
            val size2 = m2.size
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("size1") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("size2") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("typeCheck2a") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck2b") as BooleanValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(7, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(-5, (symbolTable.findPropertyByDeclaredName("g") as IntValue).value)
    }

    @Test
    fun toList() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val m = mutableMapOf(
                "ab" to 123,
                "def" to 234,
            )
            m as MutableMap<String, Int>
            var counter = 100
            m["bcd"] = ++counter
            m["ab"] = ++counter
            m["bcd"] = ++counter
            m["hi"] = ++counter
            val l = m.toList()
            val typeCheck = l is List<Pair<String, Int>>
            l as List<Pair<String, Int>>
            val size: Int = l.size
            val a: Int = l.firstOrNull { it.first == "ab" }?.second ?: -10
            val b: Int = l.firstOrNull { it.first == "bcd" }?.second ?: -10
            val c: Int = l.firstOrNull { it.first == "def" }?.second ?: -10
            val d: Int = l.firstOrNull { it.first == "hi" }?.second ?: -10
            val e: Int = l.firstOrNull { it.first == "jk" }?.second ?: -10
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck") as BooleanValue).value)
        assertEquals(102, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(103, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(104, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-10, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
    }

    @Test
    fun transformWithMapEntry() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(TextLibModule())
        }
        val interpreter = interpreter("""
            var counter = 0
            val m = mapOf(
                "abc" to ++counter,
                "d" to ++counter,
                "ef" to ++counter,
            )
            
            val m1 = m.mapKeys { it.key.length }
                .mapValues { it.value + 100 }
            
            val typeCheck1: Boolean = m1 is Map<Int, Int>
            
            val a = m1.count { it.value <= 102 }
            
            m1.forEach {
                println("${'$'}{it.key}: ${'$'}{it.value}")
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(listOf("1: 102", "2: 103", "3: 101"), console.split("\n").sorted().filter { it.isNotEmpty() })
    }

    @Test
    fun forLoopMapEntry() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
        }
        val interpreter = interpreter("""
            var counter = 0
            val m = mapOf(
                "abc" to ++counter,
                "d" to ++counter,
                "ef" to ++counter,
            )
            
            val typeCheck1: Boolean = m is Map<String, Int>
            
            for (it in m) {
                println("${'$'}{it.key}: ${'$'}{it.value}")
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(listOf("abc: 1", "d: 2", "ef: 3"), console.split("\n").sorted().filter { it.isNotEmpty() })
    }

    @Test
    fun forLoopMapValues() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
        }
        val interpreter = interpreter("""
            var counter = 100
            val m = mapOf(
                "abc" to ++counter,
                "d" to ++counter,
                "ef" to ++counter,
            )
            
            val typeCheck1: Boolean = m is Map<String, Int>
            
            for (value in m.values) {
                println("${'$'}value")
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(listOf("101", "102", "103"), console.split("\n").sorted().filter { it.isNotEmpty() })
    }

    @Test
    fun mutableMapOnEach() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(CoreLibModule())
            install(CollectionsLibModule())
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
        }
        val interpreter = interpreter("""
            var counter = 0
            var typeCheck1: Boolean = false
            mutableMapOf(
                "abc" to ++counter,
                "d" to ++counter,
                "ef" to ++counter,
            ).onEach {
                println("${'$'}{it.key}: ${'$'}{it.value}")
            }.also { m -> typeCheck1 = m is MutableMap<String, Int> }
            .apply { this["aa"] = ++counter }
            .also { m ->
                for (it in m) {
                    println("${'$'}{it.key}: ${'$'}{it.value}")
                }
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("typeCheck1") as BooleanValue).value)
        assertEquals(listOf("abc: 1", "d: 2", "ef: 3", "abc: 1", "d: 2", "ef: 3", "aa: 4").sorted(), console.split("\n").sorted().filter { it.isNotEmpty() })
    }

    @Test
    fun maxOfOrNull() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val m1 = mapOf(
                "a" to 3,
                "abc" to 1590,
                "d" to -26,
                "ef" to 891,
            )
            val m2 = mapOf<String, Int>()
            
            val a = m1.maxOfOrNull { it.value }
            val b = m2.maxOfOrNull { it.value }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(1590, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("b"))
    }

    @Test
    fun customKey() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            class MyTriple(val a: Int, val b: Int, val c: Int) {
                override fun equals(other: Any?): Boolean {
                    if (other !is MyTriple) return false
                    val o = other as MyTriple
                    return o.a == a && o.b == b && o.c == c
                }
                
                override fun hashCode(): Int {
                    return (a * 43 + b) * 43 + c
                }
            }
            fun t(a: Int, b: Int, c: Int) = MyTriple(a, b, c)
            val m = mutableMapOf(
                t(1, 3, 4) to 123,
                t(2, 0, 2) to 234,
            )
            val a = m[t(2, 0, 2)]
            val b = m[t(1, 3, 4)]
            
            m[t(1, 6, 8)] = 2000
            m[t(1, 3, 4)] = 15
            m[t(7, 2, 1)] = 831
            
            val c = m[t(2, 0, 2)]
            val d = m[t(1, 3, 4)]
            val e = m[t(1, 2, 3)]
            val f = m[t(1, 6, 8)]
            val g = m[t(8, 6, 1)]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(123, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(234, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("e"))
        assertEquals(2000, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
        assertEquals(NullValue, symbolTable.findPropertyByDeclaredName("g"))
    }
}
