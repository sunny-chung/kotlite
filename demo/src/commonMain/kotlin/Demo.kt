import com.sunnychung.lib.multiplatform.kotlite.KotliteInterpreter
import com.sunnychung.lib.multiplatform.kotlite.extension.fullClassName
import com.sunnychung.lib.multiplatform.kotlite.model.ExecutionEnvironment
import com.sunnychung.lib.multiplatform.kotlite.stdlib.AllStdLibModules

fun interpretKotlite(code: String): String {
    val stdout = StringBuilder()
    try {
        KotliteInterpreter("UserScript", code, ExecutionEnvironment().apply {
            install(AllStdLibModules { out -> stdout.append(out) })
        }).eval()
    } catch (e: Throwable) {
        return "[Error!] ${e.fullClassName}: ${e.message}"
    }
    return stdout.toString()
}

val demoScripts = mapOf(
    "Hello World" to """
        println("Execute at: ${'$'}{KZonedInstant.nowAtLocalZoneOffset()}")
        println()
        println("Hello World!")
    """.trimIndent(),

    "Fibonacci Sequence" to """
        println("Execute at: ${'$'}{KZonedInstant.nowAtLocalZoneOffset()}")
        println()
        
        fun fib(n: Int): Long {
            if (n > 100) throw Exception("n is too large")
            
            val dp = mutableMapOf<Int, Long>()
            fun f(i: Int): Long {
                if (i < 0) throw Exception("Invalid i: ${'$'}i")
                if (i <= 1) return i.toLong()
                if (i in dp) {
                    return dp[i]!!
                }
                return (f(i - 2) + f(i - 1)).also {
                    dp[i] = it
                }
            }
            
            return f(n)
        }
        println(fib(19))
    """.trimIndent(),

    "Mutable Map with Custom Objects as Keys" to """
        println("Execute at: ${'$'}{KZonedInstant.nowAtLocalZoneOffset()}")
        println()
        
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
        
        println("a = ${'$'}a")
        println("b = ${'$'}b")
        println("c = ${'$'}c")
        println("d = ${'$'}d")
        println("e = ${'$'}e")
        println("f = ${'$'}f")
        println("g = ${'$'}g")
    """.trimIndent(),
)
