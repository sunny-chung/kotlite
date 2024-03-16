import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.model.StringValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.CollectionsLibModule
import com.sunnychung.lib.multiplatform.kotlite.stdlib.IOLibModule
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class ListTest {
    @Test
    fun forEach() {
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
            val l: List<Int> = listOf(1, 2, 3, 4, 5)
            l.forEach { println(it) }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals("1\n2\n3\n4\n5\n", console.toString())
    }

    @Test
    fun map() {
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
            listOf(1, 2, 3, 4, 5)
                .map { it * 2 }
                .map { "(${'$'}it)" }
                .forEach { println(it) }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals("(2)\n(4)\n(6)\n(8)\n(10)\n", console.toString())
    }

    @Test
    fun filterAndSize() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val a = listOf(1, 2, 3, 4, 5)
                .shuffled()
                .filter { it <= 3 }
                .size
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
    }

    @Test
    fun mapIndexedAndTakeLastAndJoinToString() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val a = listOf(1, 2, 3, 4, 5)
                .mapIndexed { index, it -> "(${'$'}index: ${'$'}{ it * 2 })" }
                .takeLast(3)
                .joinToString(separator = "\n") { it -> ">${'$'}it" }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(">(2: 6)\n>(3: 8)\n>(4: 10)", (symbolTable.findPropertyByDeclaredName("a") as StringValue).value)
    }

    @Test
    fun getOperator() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val a = listOf(1, 2, 3, 4, 5)
                .map { it * 2 }
            val a0 = a[0]
            val a1 = a[1]
            val a2 = a[2]
            val a3 = a[3]
            val a4 = a[4]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a0") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("a2") as IntValue).value)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("a3") as IntValue).value)
        assertEquals(10, (symbolTable.findPropertyByDeclaredName("a4") as IntValue).value)
    }

    @Test
    fun setOperatorOnNonMutableListShouldFail() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        assertSemanticFail("""
            val a = listOf(1, 2, 3, 4, 5)
                .map { it * 2 }
            a[1] = 2
        """.trimIndent(), environment = env)
    }

    @Test
    fun setOperatorOnMutableList() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val a = mutableListOf(1, 2, 3, 4, 5)
                .map { it * 2 }
                .toMutableList()
            
            a[1] = 5
            a[2] += 10
            a[a.lastIndex] *= 3
            val a0 = a[0]
            val a1 = a[1]
            val a2 = a[2]
            val a3 = a[3]
            val a4 = a[4]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a0") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("a1") as IntValue).value)
        assertEquals(16, (symbolTable.findPropertyByDeclaredName("a2") as IntValue).value)
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("a3") as IntValue).value)
        assertEquals(30, (symbolTable.findPropertyByDeclaredName("a4") as IntValue).value)
    }

    @Test
    fun addAll() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val a = mutableListOf(1, 2, 3, 4, 5)
            val b = listOf(2, 3, 9)
            
            a.addAll(b)
            
            val size = a.count()
            
            val a3 = a[3]
            val a5 = a[5]
            val a7 = a[7]
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(8, (symbolTable.findPropertyByDeclaredName("size") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("a3") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("a5") as IntValue).value)
        assertEquals(9, (symbolTable.findPropertyByDeclaredName("a7") as IntValue).value)
    }

    @Test
    @Ignore
    fun ifEmpty() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            fun f(l: List<Int>): Int {
                return l.ifEmpty { listOf(4) }
                    .fold(0) { acc, it -> acc + it }
            }
            val a = f(mutableListOf(1, 2, 3, 4, 5))
            val b = f(mutableListOf<Int>())
            val c = f(listOf<Int>())
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
    }

    @Test
    fun onEachAndFold() {
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
            fun f(l: List<Int>): Int {
                return l
                    .onEach { println(it) }
                    .fold(0) { acc, it -> acc + it }
            }
            val a = f(mutableListOf(1, 2, 3, 4, 5))
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(15, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("1\n2\n3\n4\n5\n", console.toString())
    }

    @Test
    fun plusMinusOperators() {
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
            val list1 = listOf(1, 7, 4, 9)
            val list2 = listOf(2, 1, 3)
            val list3 = listOf(3, 4)
            var list = list1 + list2 - list3
            list += 5
            val a = list.size
            for (e in list) {
                println(e)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("1\n7\n9\n2\n1\n5\n", console.toString())
    }

    @Test
    fun plusAssignMinusAssignOperators() {
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
            val list1 = listOf(1, 7, 4, 9)
            val list2 = listOf(2, 1, 3)
            val list3 = listOf(3, 4)
            val list = mutableListOf<Int>()
            list += list1
            list += list2
            list -= list3
            list += 5
            list -= 1
            list -= 13579
            val a = list.size
            for (e in list) {
                println(e)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals("7\n9\n2\n1\n5\n", console.toString())
    }

    @Test
    fun binarySearchElement() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            val l = mutableListOf(1, 4, 6, 7, 11)
            l += 16
            val a = l.binarySearch(1)
            val b = l.binarySearch(4)
            val c = l.binarySearch(6)
            val d = l.binarySearch(7)
            val e = l.binarySearch(11)
            val f = l.binarySearch(16)
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(0, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(3, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(4, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(5, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
    }

    @Test
    fun maxMaxByMinMinByOrNull() {
        val env = ExecutionEnvironment().apply {
            install(CollectionsLibModule())
        }
        val interpreter = interpreter("""
            class V(val name: String, val value: Int)
            val l = listOf(V("A", 7), V("B", 29), V("C", 6), V("D", 14), V("E", 27))
            val l2 = l.map { it.value }
            val a = l2.max()
            val b = l2.min()
            val c = l.maxBy { it.value }.name
            val d = l.minByOrNull { it.value }?.name
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(29, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(6, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals("B", (symbolTable.findPropertyByDeclaredName("c") as StringValue).value)
        assertEquals("C", (symbolTable.findPropertyByDeclaredName("d") as StringValue).value)
    }

    @Test
    fun sorted() {
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
            val list1 = listOf(1, 7, 4, -2, 9)
            val list = list1.sorted()
            for (e in list) {
                println(e)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals("-2\n1\n4\n7\n9\n", console.toString())
    }

    @Test
    fun sort() {
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
            val list = mutableListOf(1, 7, 4)
            list += -2
            list += 9
            list.sort()
            for (e in list) {
                println(e)
            }
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        assertEquals("-2\n1\n4\n7\n9\n", console.toString())
    }
}
