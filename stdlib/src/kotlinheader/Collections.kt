fun <T> listOf(vararg elements: T): List<T>
fun <T : Any> listOfNotNull(vararg elements: T?): List<T>

fun <T> List<T>.forEach(action: (T) -> Unit)
fun <T, R> List<T>.map(transform: (T) -> R): List<R>
