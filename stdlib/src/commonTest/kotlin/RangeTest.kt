import com.sunnychung.lib.multiplatform.kotlite.model.BooleanValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
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
}
