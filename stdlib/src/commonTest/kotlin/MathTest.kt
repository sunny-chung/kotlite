import com.sunnychung.lib.multiplatform.kotlite.model.DoubleValue
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.model.IntValue
import com.sunnychung.lib.multiplatform.kotlite.stdlib.MathLibModule
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertEquals

class MathTest {

    @Test
    fun intSignAbsMaxMinRoundToInt() {
        val env = ExecutionEnvironment().apply {
            install(MathLibModule())
        }
        val interpreter = interpreter("""
            val a: Int = 12.sign
            val b: Int = -8.sign
            val c: Int = abs(-2)
            val d: Int = max(6, 9)
            val e: Int = min(-100, -99)
            val f: Int = 2.1.roundToInt()
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        assertEquals(1, (symbolTable.findPropertyByDeclaredName("a") as IntValue).value)
        assertEquals(-1, (symbolTable.findPropertyByDeclaredName("b") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("c") as IntValue).value)
        assertEquals(9, (symbolTable.findPropertyByDeclaredName("d") as IntValue).value)
        assertEquals(-100, (symbolTable.findPropertyByDeclaredName("e") as IntValue).value)
        assertEquals(2, (symbolTable.findPropertyByDeclaredName("f") as IntValue).value)
    }

    @Test
    fun doubleAbsoluteValueSignPowExpFloorCeilLog2Log10RoundSqrtWithSignTruncate() {
        val env = ExecutionEnvironment().apply {
            install(MathLibModule())
        }
        val interpreter = interpreter("""
            val a: Double = 12.3.absoluteValue
            val b: Double = sign(-8.0)
            val c: Double = 9.1.pow(3.6)
            val d: Double = 9.1.pow(3)
            val e: Double = exp(1.34)
            val f: Double = floor(2.6)
            val g: Double = ceil(2.6)
            val h: Double = log2(1048576.0)
            val i: Double = log10(1048576.0)
            val j: Double = round(7.5)
            val k: Double = sqrt(625.0)
            val l: Double = k.withSign(-100)
            val m: Double = truncate(-4.9)
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        compareNumber(12.3, symbolTable.findPropertyByDeclaredName("a") as DoubleValue)
        compareNumber(-1.0, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        compareNumber(9.1.pow(3.6), symbolTable.findPropertyByDeclaredName("c") as DoubleValue)
        compareNumber(9.1.pow(3), symbolTable.findPropertyByDeclaredName("d") as DoubleValue)
        compareNumber(exp(1.34), symbolTable.findPropertyByDeclaredName("e") as DoubleValue)
        compareNumber(2.0, symbolTable.findPropertyByDeclaredName("f") as DoubleValue)
        compareNumber(3.0, symbolTable.findPropertyByDeclaredName("g") as DoubleValue)
        compareNumber(log2(1048576.0), symbolTable.findPropertyByDeclaredName("h") as DoubleValue)
        compareNumber(log10(1048576.0), symbolTable.findPropertyByDeclaredName("i") as DoubleValue)
        compareNumber(round(7.5), symbolTable.findPropertyByDeclaredName("j") as DoubleValue)
        compareNumber(25.0, symbolTable.findPropertyByDeclaredName("k") as DoubleValue)
        compareNumber(-25.0, symbolTable.findPropertyByDeclaredName("l") as DoubleValue)
        compareNumber(-4.0, symbolTable.findPropertyByDeclaredName("m") as DoubleValue)
    }

    @Test
    fun piSinCosTan() {
        val env = ExecutionEnvironment().apply {
            install(MathLibModule())
        }
        val interpreter = interpreter("""
            val a: Double = sin(PI / 6)
            val b: Double = cos(2 * PI / 3)
            val c: Double = tan(PI / 4)
        """.trimIndent(), executionEnvironment = env, isDebug = true)
        interpreter.eval()
        val symbolTable = interpreter.symbolTable()
        compareNumber(0.5, symbolTable.findPropertyByDeclaredName("a") as DoubleValue)
        compareNumber(-0.5, symbolTable.findPropertyByDeclaredName("b") as DoubleValue)
        compareNumber(1.0, symbolTable.findPropertyByDeclaredName("c") as DoubleValue)
    }
}
