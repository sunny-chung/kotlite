import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.IOLibModule
import kotlin.test.Test
import kotlin.test.assertEquals

class SetTest {

    @Test
    fun setOfForEach() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val set1 = setOf(1, 2, 2, 3, 2)
            val set2 = setOf(2, 3, 9, 8)
            val a = set1.size
            val b = set2.size
            
            set1.forEach {
                println(it)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(listOf("1", "2", "3"), console.split("\n").sorted().filter { it.isNotEmpty() })
    }

    @Test
    fun mutableSetOfAddAllRemove() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val set = mutableSetOf(1, 2, 3, 4, 5)
            val list = listOf(2, 3, 9)
            
            set.addAll(list)
            val a = set.size
            
            set.remove(2)
            set.remove(5)
            set.remove(123456)
            val b = set.size
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
    }

    @Test
    fun toSetToMutableSetPlusMinus() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val list1 = listOf(1, 2, 3, 4, 5)
            val list2 = listOf(9, 2, 3, 9)
            
            var set1: Set<Int> = list1.toMutableSet()
            val set2: Set<Int> = list2.toSet()
            set1 = set1.plus(-5).plus(set2).plus(8)
            set1 = set1.minus(4).minus(3)
            val size1: Int = set1.size
            val size2: Int = set2.size
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("size1") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("size2") as IntValue).value)
    }

    @Test
    fun forLoopSet() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val set = setOf(1, 2, 2, 3, 2)
            for (e in set) {
                println(e)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals(listOf("1", "2", "3"), console.split("\n").sorted().filter { it.isNotEmpty() })
    }

    @Test
    fun plusMinusAssignOperators() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val set1 = setOf(1, 2, 2, 3, 2)
            val set2 = setOf(2, 3, 9, 8)
            var set = set1 + set2 - 9
            set -= 2
            val size = set.size
            
            set.forEach {
                println(it)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(listOf("1", "3", "8"), console.split("\n").sorted().filter { it.isNotEmpty() })
    }

    @Test
    fun mutableSetPlusMinusAssignOperators() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val set1 = setOf(1, 2, 2, 3, 2)
            val set2 = setOf(2, 3, 9, 8)
            val set = mutableSetOf(15, 7)
            set += set1
            set -= set2
            set += -6
            set += 9
            set -= 7
            val size = set.size
            
            set.forEach {
                println(it)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(listOf(-6, 1, 9, 15), console.split("\n").filter { it.isNotEmpty() }.map { it.toInt() }.sorted())
    }

    @Test
    fun infixFunctionsUnionIntersect() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val set1 = listOf(1, 2, 2, 3, 2)
            val set2 = listOf(2, 3, 9, 8)
            var set = set1 union set2 union mutableSetOf(15, 7)
            val a = set.size
            set = set intersect listOf(1, 3, 3, 6, 7, 10)
            val b = set.size
            for (e in set) {
                println(e)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(7, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(listOf(1, 3, 7), console.split("\n").filter { it.isNotEmpty() }.map { it.toInt() }.sorted())
    }

    @Test
    fun inOperator() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val set = setOf("hi", "abc", "g", "jk", "def")
            val a = "hi" in set
            val b = "abcd" in set
            val c = "k" !in set
            val d = "def" !in set
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("a") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("b") as BooleanValue).value)
        assertEquals(true, (symbolTable.findPropertyByDeclaredName("c") as BooleanValue).value)
        assertEquals(false, (symbolTable.findPropertyByDeclaredName("d") as BooleanValue).value)
    }

    @Test
    fun toMutableSetOnEachMap() {
        val console = StringBuilder()
        val env = ExecutionEnvironment().apply {
            install(object : IOLibModule() {
                override fun outputToConsole(output: String) {
                    console.append(output)
                }
            })
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val set = setOf(1, 2, 2, 3, 2).toMutableSet()
            set += 7
            val set2 = set.onEach {
                println("a${'$'}it")
            }
            set2 += 6
            set2.map {
                it * 5
            }.onEach {
                println("b${'$'}it")
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals(
            expected = listOf("a1", "a2", "a3", "a7", "b5", "b10", "b15", "b35", "b30").sorted(),
            actual = console.split("\n").filter { it.isNotEmpty() }.sorted(),
        )
    }
}
