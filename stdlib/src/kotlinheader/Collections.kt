////////////////////
// List

val <T> List<T>.size: Int
    get()
val <T> List<T>.lastIndex: Int
    get()

fun <T> listOf(vararg elements: T): List<T>
fun <T> mutableListOf(vararg elements: T): MutableList<T>
//fun <T> emptyList(): List<T>
fun <T : Any> listOfNotNull(vararg elements: T?): List<T>
fun <T> List(size: Int, init: (index: Int) -> T): List<T>
fun <T> MutableList(size: Int, init: (index: Int) -> T): MutableList<T>

fun <T> MutableList<T>.addAll(elements: List<T>): Boolean
fun <T> List<T>.all(predicate: (T) -> Boolean): Boolean
fun <T> List<T>.any(predicate: (T) -> Boolean): Boolean
fun <T> List<T>.asReversed(): List<T>
fun <T> List<T>.binarySearch(
    fromIndex: Int = 0,
    toIndex: Int = size,
    comparison: (T) -> Int
): Int
//fun <T> List<T>.chunked(size: Int): List<List<T>>
fun <T, R> List<T>.chunked(size: Int, transform: (List<T>) -> R): List<R>
fun <T> List<T>.contains(element: T): Boolean
fun <T> List<T>.containsAll(elements: List<T>): Boolean
fun <T> List<T>.count(predicate: (T) -> Boolean): Int
fun <T> List<T>.count(): Int
fun <T> List<T>.distinct(): List<T>
fun <T, K> List<T>.distinctBy(selector: (T) -> K): List<T>
fun <T> List<T>.drop(n: Int): List<T>
fun <T> List<T>.dropLast(n: Int): List<T>
fun <T> List<T>.dropLastWhile(predicate: (T) -> Boolean): List<T>
fun <T> List<T>.dropWhile(predicate: (T) -> Boolean): List<T>
fun <T> List<T>.elementAt(index: Int): T
fun <T> List<T>.elementAtOrElse(index: Int, defaultValue: (Int) -> T): T
fun <T> List<T>.elementAtOrNull(index: Int): T?
fun <T> MutableList<T>.fill(value: T)
fun <T> List<T>.filter(predicate: (T) -> Boolean): List<T>
fun <T> List<T>.filterIndexed(predicate: (index: Int, T) -> Boolean): List<T>
//fun <R> List<*>.filterIsInstance(): List<R>
fun <T> List<T>.filterNot(predicate: (T) -> Boolean): List<T>
fun <T : Any> List<T?>.filterNotNull(): List<T>
fun <T> List<T>.find(predicate: (T) -> Boolean): T?
fun <T> List<T>.findLast(predicate: (T) -> Boolean): T?
fun <T> List<T>.first(): T
fun <T> List<T>.first(predicate: (T) -> Boolean): T
fun <T, R : Any> List<T>.firstNotNullOf(
    transform: (T) -> R?
): R
fun <T, R : Any> List<T>.firstNotNullOfOrNull(
    transform: (T) -> R?
): R?
fun <T> List<T>.firstOrNull(): T?
fun <T> List<T>.firstOrNull(predicate: (T) -> Boolean): T?
fun <T, R> List<T>.flatMap(
    transform: (T) -> List<R>
): List<R>
fun <T, R> List<T>.flatMapIndexed(
    transform: (index: Int, T) -> List<R>
): List<R>
//fun <T> List<List<T>>.flatten(): List<T>
fun <T, R> List<T>.fold(
    initial: R,
    operation: (acc: R, T) -> R
): R
fun <T, R> List<T>.foldIndexed(
    initial: R,
    operation: (index: Int, acc: R, T) -> R
): R
fun <T, R> List<T>.foldRight(
    initial: R,
    operation: (T, acc: R) -> R
): R
fun <T, R> List<T>.foldRightIndexed(
    initial: R,
    operation: (index: Int, T, acc: R) -> R
): R
fun <T> List<T>.forEach(action: (T) -> Unit)
fun <T> List<T>.forEachIndexed(action: (index: Int, T) -> Unit)
operator fun <T> List<T>.get(index: Int): T
fun <T> List<T>.getOrElse(
    index: Int,
    defaultValue: (Int) -> T
): T
fun <T> List<T>.getOrNull(index: Int): T?
//fun <T, R> List<T>.ifEmpty(defaultValue: () -> R): R
fun <T> List<T>.indexOf(element: T): Int
fun <T> List<T>.indexOfFirst(predicate: (T) -> Boolean): Int
fun <T> List<T>.indexOfLast(predicate: (T) -> Boolean): Int
fun <T> List<T>.isEmpty(): Boolean
fun <T> List<T>.isNotEmpty(): Boolean
fun <T> List<T>?.isNullOrEmpty(): Boolean
fun <T> List<T>.joinToString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    limit: Int = -1,
    truncated: String = "...",
    transform: ((T) -> String)? = null
): String
fun <T> List<T>.last(): T
fun <T> List<T>.last(predicate: (T) -> Boolean): T
fun <T> List<T>.lastIndexOf(element: T): Int
fun <T> List<T>.lastOrNull(): T?
fun <T> List<T>.lastOrNull(predicate: (T) -> Boolean): T?
fun <T, R> List<T>.map(transform: (T) -> R): List<R>
fun <T, R> List<T>.mapIndexed(transform: (index: Int, T) -> R): List<R>
fun <T, R : Any> List<T>.mapIndexedNotNull(transform: (index: Int, T) -> R?): List<R>
fun <T, R : Any> List<T>.mapNotNull(transform: (T) -> R?): List<R>
fun <T> List<T>.minus(element: T): List<T>
fun <T> MutableList<T>.minusAssign(element: T)
fun <T> List<T>.minusElement(element: T): List<T>
fun <T> List<T>.none(predicate: (T) -> Boolean): Boolean
fun <T> List<T>.none(): Boolean
fun <T> List<T>.onEach(action: (T) -> Unit): List<T>
fun <T> List<T>.onEachIndexed(action: (index: Int, T) -> Unit): List<T>
fun <T> List<T>?.orEmpty(): List<T>
fun <T> List<T>.plus(element: T): List<T>
fun <T> List<T>.plus(elements: List<T>): List<T>
fun <T> MutableList<T>.plusAssign(element: T)
fun <T> List<T>.plusElement(element: T): List<T>
fun <T> List<T>.random(): T
fun <T> List<T>.randomOrNull(): T?
fun <T> MutableList<T>.removeAll(predicate: (T) -> Boolean): Boolean
fun <T> MutableList<T>.removeAt(index: Int): T
fun <T> MutableList<T>.removeFirst(): T
fun <T> MutableList<T>.removeFirstOrNull(): T?
fun <T> MutableList<T>.removeLast(): T
fun <T> MutableList<T>.removeLastOrNull(): T?
fun <T> MutableList<T>.retainAll(predicate: (T) -> Boolean): Boolean
fun <T> List<T>.reversed(): List<T>
operator fun <T> MutableList<T>.set(index: Int, element: T): T
fun <T> MutableList<T>.shuffle()
fun <T> List<T>.shuffled(): List<T>
fun <T> List<T>.single(): T
fun <T> List<T>.single(predicate: (T) -> Boolean): T
fun <T> List<T>.singleOrNull(): T?
fun <T> List<T>.singleOrNull(predicate: (T) -> Boolean): T?
fun <T> List<T>.subList(fromIndex: Int, toIndex: Int): List<T>
//fun <T> List<Int>.sum(): Int
//fun <T> List<Long>.sum(): Long
//fun <T> List<Double>.sum(): Double
fun <T> List<T>.take(n: Int): List<T>
fun <T> List<T>.takeLast(n: Int): List<T>
fun <T> List<T>.takeLastWhile(predicate: (T) -> Boolean): List<T>
fun <T> List<T>.takeWhile(predicate: (T) -> Boolean): List<T>
fun <T> MutableList<T>.toList(): List<T>
fun <T> List<T>.toMutableList(): MutableList<T>
//fun <T> List<T>.windowed(
//    size: Int,
//    step: Int = 1,
//    partialWindows: Boolean = false
//): List<List<T>>
fun <T, R> List<T>.windowed(
    size: Int,
    step: Int = 1,
    partialWindows: Boolean = false,
    transform: (List<T>) -> R
): List<R>
fun <T, R, V> List<T>.zip(
    other: List<R>,
    transform: (a: T, b: R) -> V
): List<V>

////////////////////
// Map

val <K, V> Map<K, V>.size: Int
    get()

fun <K, V> mapOf(vararg pairs: Pair<K, V>): Map<K, V>
fun <K, V> mutableMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V>

fun <K, V> Map<K, V>.contains(key: K): Boolean
fun <K> Map<K, *>.containsKey(key: K): Boolean
fun <K, V> Map<K, V>.containsValue(value: V): Boolean

fun <T, K, V> List<T>.associate(
    transform: (T) -> Pair<K, V>
): Map<K, V>

fun <T, K> List<T>.associateBy(
    keySelector: (T) -> K
): Map<K, T>

fun <K, V> List<K>.associateBy(
    valueSelector: (K) -> V
): Map<K, V>

fun <K, V> Map<K, V>.count(): Int
//fun <K, V> Map<K, V>.count(predicate: (Entry<K, V>) -> Boolean): Int
//fun <K, V> emptyMap(): Map<K, V>
//fun <K, V> Map<K, V>.filter(predicate: (Entry<K, V>) -> Boolean): Map<K, V>
fun <K, V> Map<K, V>.filterKeys(predicate: (K) -> Boolean): Map<K, V>
//fun <K, V> Map<K, V>.filterNot(predicate: (Entry<K, V>) -> Boolean): Map<K, V>
fun <K, V> Map<K, V>.filterValues(predicate: (V) -> Boolean): Map<K, V>
//fun <K, V> Map<K, V>.forEach(action: (Entry<K, V>) -> Unit): Int
operator fun <K, V> Map<K, V>.get(key: K): V?
fun <K, V> Map<K, V>.getOrElse(key: K, defaultValue: () -> V): V
fun <K, V> MutableMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <T, K> List<T>.groupBy(
    keySelector: (T) -> K
): Map<K, List<T>>
fun <T, K, V> List<T>.groupBy(
    keySelector: (T) -> K,
    valueSelector: (T) -> V,
): Map<K, List<V>>
fun <K, V> Map<K, V>.isNotEmpty(): Boolean
fun <K, V> Map<K, V>?.isNullOrEmpty(): Boolean
//fun <K, V, R> Map<K, V>.map(transform: (Entry<K, V>) -> R): List<R>
//fun <K, V, R> Map<K, V>.mapKeys(transform: (Entry<K, V>) -> R): Map<R, V>
//fun <K, V, R> Map<K, V>.mapValues(transform: (Entry<K, V>) -> R): Map<K, R>
fun <K, V> Map<K, V>.minus(key: K): Map<K, V>
fun <K, V> Map<K, V>.minus(keys: List<K>): Map<K, V>
fun <K, V> MutableMap<K, V>.minusAssign(key: K)
fun <K, V> MutableMap<K, V>.minusAssign(keys: List<K>)
//fun <K, V> Map<K, V>.none(predicate: (Entry<K, V>) -> Boolean): Boolean
//fun <K, V, M : Map<K, V>> M.onEach(action: (Entry<K, V>) -> Unit): M
//fun <K, V, M : Map<K, V>> M.onEachIndexed(action: (index: Int, Entry<K, V>) -> Unit): M
fun <K, V> Map<K, V>?.orEmpty(): Map<K, V>
fun <K, V> Map<K, V>.plus(pair: Pair<K, V>): Map<K, V>
fun <K, V> Map<K, V>.plus(pairs: List<Pair<K, V>>): Map<K, V>
fun <K, V> MutableMap<K, V>.plusAssign(pair: Pair<K, V>)
fun <K, V> MutableMap<K, V>.plusAssign(pairs: List<Pair<K, V>>)
fun <K, V> MutableMap<K, V>.putAll(pairs: List<Pair<K, V>>)
fun <K, V> MutableMap<K, V>.remove(key: K): V?
operator fun <K, V> MutableMap<K, V>.set(key: K, value: V)
fun <K, V> Map<K, V>.toList(): List<Pair<K, V>>
fun <K, V> List<Pair<K, V>>.toMap(): Map<K, V>
fun <K, V> Map<K, V>.toMap(): Map<K, V>
fun <K, V> Map<K, V>.toMutableMap(): MutableMap<K, V>
fun <K, V> Map<K, V>.withDefault(defaultValue: (key: K) -> V): Map<K, V>
fun <K, V> MutableMap<K, V>.withDefault(defaultValue: (key: K) -> V): MutableMap<K, V>

