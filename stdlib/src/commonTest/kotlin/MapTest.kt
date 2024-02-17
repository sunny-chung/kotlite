import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.NullValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
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
            val a: Int = m["ab"]?.size
            val b: Int = m["bcd"]?.size
            val c: Int = m["e"]?.size
            val d: Int = m["fghi"]?.size
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
            m["bcd"] = ++counter
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
}
