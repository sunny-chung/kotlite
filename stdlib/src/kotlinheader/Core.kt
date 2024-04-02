fun Double.isFinite(): Boolean
fun Double.isInfinite(): Boolean
fun Double.isNaN(): Boolean

fun Int.toLong(): Long
fun Int.toDouble(): Double
fun Long.toInt(): Int
fun Long.toDouble(): Double
fun Double.toInt(): Int
fun Double.toLong(): Long

fun <T> Pair<T, T>.toList(): List<T>

fun repeat(times: Int, action: (Int) -> Unit)
fun <T> T.takeIf(predicate: (T) -> Boolean): T?
fun <T> T.takeUnless(predicate: (T) -> Boolean): T?
fun TODO(): Nothing
fun check(value: Boolean)
fun check(value: Boolean, lazyMessage: () -> Any)
nullaware fun <T : Any> checkNotNull(value: T?): T
nullaware fun <T : Any> checkNotNull(value: T?, lazyMessage: () -> Any): T
fun require(value: Boolean)
fun require(value: Boolean, lazyMessage: () -> Any)
nullaware fun <T : Any> requireNotNull(value: T?): T
nullaware fun <T : Any> requireNotNull(value: T?, lazyMessage: () -> Any): T
