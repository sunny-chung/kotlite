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

fun <T> MutableCollection<T>.add(element: T): Boolean
fun <T> MutableCollection<T>.addAll(elements: Iterable<T>): Boolean
fun <T> Iterable<T>.all(predicate: (T) -> Boolean): Boolean
fun <T> Iterable<T>.any(predicate: (T) -> Boolean): Boolean
fun <T> List<T>.asReversed(): List<T>
fun <T : Comparable<T>> List<T?>.binarySearch(
    element: T?,
    fromIndex: Int = 0,
    toIndex: Int = size,
): Int
fun <T> List<T>.binarySearch(
    fromIndex: Int = 0,
    toIndex: Int = size,
    comparison: (T) -> Int
): Int
fun <T, K : Comparable<K>> List<T>.binarySearchBy(
    key: K?,
    fromIndex: Int = 0,
    toIndex: Int = size,
    selector: (T) -> K?
): Int
//fun <T> Iterable<T>.chunked(size: Int): List<List<T>>
fun <T, R> Iterable<T>.chunked(size: Int, transform: (List<T>) -> R): List<R>
operator fun <T> Iterable<T>.contains(element: T): Boolean
fun <T> List<T>.containsAll(elements: List<T>): Boolean
fun <T> MutableCollection<T>.clear()
fun <T> Iterable<T>.count(predicate: (T) -> Boolean): Int
fun <T> Iterable<T>.count(): Int
fun <T> Iterable<T>.distinct(): List<T>
fun <T, K> Iterable<T>.distinctBy(selector: (T) -> K): List<T>
fun <T> Iterable<T>.drop(n: Int): List<T>
fun <T> List<T>.dropLast(n: Int): List<T>
fun <T> List<T>.dropLastWhile(predicate: (T) -> Boolean): List<T>
fun <T> Iterable<T>.dropWhile(predicate: (T) -> Boolean): List<T>
fun <T> Iterable<T>.elementAt(index: Int): T
fun <T> Iterable<T>.elementAtOrElse(index: Int, defaultValue: (Int) -> T): T
fun <T> Iterable<T>.elementAtOrNull(index: Int): T?
fun <T> MutableList<T>.fill(value: T)
fun <T> Iterable<T>.filter(predicate: (T) -> Boolean): List<T>
fun <T> Iterable<T>.filterIndexed(predicate: (index: Int, T) -> Boolean): List<T>
//fun <R> Iterable<*>.filterIsInstance(): List<R>
fun <T> Iterable<T>.filterNot(predicate: (T) -> Boolean): List<T>
fun <T : Any> Iterable<T?>.filterNotNull(): List<T>
fun <T> Iterable<T>.find(predicate: (T) -> Boolean): T?
fun <T> Iterable<T>.findLast(predicate: (T) -> Boolean): T?
fun <T> Iterable<T>.first(): T
fun <T> Iterable<T>.first(predicate: (T) -> Boolean): T
fun <T, R : Any> Iterable<T>.firstNotNullOf(
    transform: (T) -> R?
): R
fun <T, R : Any> Iterable<T>.firstNotNullOfOrNull(
    transform: (T) -> R?
): R?
fun <T> Iterable<T>.firstOrNull(): T?
fun <T> Iterable<T>.firstOrNull(predicate: (T) -> Boolean): T?
fun <T, R> Iterable<T>.flatMap(
    transform: (T) -> Iterable<R>
): List<R>
fun <T, R> Iterable<T>.flatMapIndexed(
    transform: (index: Int, T) -> Iterable<R>
): List<R>
//fun <T> Iterable<Iterable<T>>.flatten(): List<T>
fun <T, R> Iterable<T>.fold(
    initial: R,
    operation: (acc: R, T) -> R
): R
fun <T, R> Iterable<T>.foldIndexed(
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
fun <T> Iterable<T>.forEach(action: (T) -> Unit)
fun <T> Iterable<T>.forEachIndexed(action: (index: Int, T) -> Unit)
operator fun <T> List<T>.get(index: Int): T
fun <T> List<T>.getOrElse(
    index: Int,
    defaultValue: (Int) -> T
): T
fun <T> List<T>.getOrNull(index: Int): T?
//fun <T, R> List<T>.ifEmpty(defaultValue: () -> R): R
fun <T> Iterable<T>.indexOf(element: T): Int
fun <T> Iterable<T>.indexOfFirst(predicate: (T) -> Boolean): Int
fun <T> Iterable<T>.indexOfLast(predicate: (T) -> Boolean): Int
fun <T> List<T>.isEmpty(): Boolean
fun <T> List<T>.isNotEmpty(): Boolean
fun <T> List<T>?.isNullOrEmpty(): Boolean
fun <T> Iterable<T>.joinToString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    limit: Int = -1,
    truncated: String = "...",
    transform: ((T) -> String)? = null
): String
fun <T> Iterable<T>.last(): T
fun <T> Iterable<T>.last(predicate: (T) -> Boolean): T
fun <T> Iterable<T>.lastIndexOf(element: T): Int
fun <T> Iterable<T>.lastOrNull(): T?
fun <T> Iterable<T>.lastOrNull(predicate: (T) -> Boolean): T?
fun <T, R> Iterable<T>.map(transform: (T) -> R): List<R>
fun <T, R> Iterable<T>.mapIndexed(transform: (index: Int, T) -> R): List<R>
fun <T, R : Any> Iterable<T>.mapIndexedNotNull(transform: (index: Int, T) -> R?): List<R>
fun <T, R : Any> Iterable<T>.mapNotNull(transform: (T) -> R?): List<R>
fun <T : Comparable<T>> Iterable<T>.max(): T
fun <T, R : Comparable<R>> Iterable<T>.maxBy(selector: (T) -> R): T
fun <T, R : Comparable<R>> Iterable<T>.maxByOrNull(selector: (T) -> R): T?
fun <T, R : Comparable<R>> Iterable<T>.maxOf(selector: (T) -> R): R
fun <T, R : Comparable<R>> Iterable<T>.maxOfOrNull(selector: (T) -> R): R?
fun <T : Comparable<T>> Iterable<T>.maxOrNull(): T?
fun <T : Comparable<T>> Iterable<T>.min(): T
fun <T, R : Comparable<R>> Iterable<T>.minBy(selector: (T) -> R): T
fun <T, R : Comparable<R>> Iterable<T>.minByOrNull(selector: (T) -> R): T?
fun <T, R : Comparable<R>> Iterable<T>.minOf(selector: (T) -> R): R
fun <T, R : Comparable<R>> Iterable<T>.minOfOrNull(selector: (T) -> R): R?
fun <T : Comparable<T>> Iterable<T>.minOrNull(): T?
operator fun <T> Iterable<T>.minus(element: T): List<T>
operator fun <T> Iterable<T>.minus(elements: Iterable<T>): List<T>
operator fun <T> MutableCollection<T>.minusAssign(element: T)
operator fun <T> MutableCollection<T>.minusAssign(elements: Iterable<T>)
fun <T> Iterable<T>.minusElement(element: T): List<T>
fun <T> Iterable<T>.none(predicate: (T) -> Boolean): Boolean
fun <T> Iterable<T>.none(): Boolean
//fun <T, C : Iterable<T>> C.onEach(action: (T) -> Unit): C
fun <T> List<T>.onEach(action: (T) -> Unit): List<T>
fun <T> List<T>.onEachIndexed(action: (index: Int, T) -> Unit): List<T>
fun <T> List<T>?.orEmpty(): List<T>
fun <T> Iterable<T>.partition(predicate: (T) -> Boolean): Pair<List<T>, List<T>>
operator fun <T> Iterable<T>.plus(element: T): List<T>
operator fun <T> Iterable<T>.plus(elements: List<T>): List<T>
operator fun <T> MutableCollection<T>.plusAssign(element: T)
operator fun <T> MutableCollection<T>.plusAssign(elements: Iterable<T>)
fun <T> Iterable<T>.plusElement(element: T): List<T>
fun <T> List<T>.random(): T
fun <T> List<T>.randomOrNull(): T?
fun <T> MutableCollection<T>.remove(element: T): Boolean
fun <T> MutableCollection<T>.removeAll(elements: Collection<T>): Boolean
fun <T> MutableList<T>.removeAll(predicate: (T) -> Boolean): Boolean
fun <T> MutableList<T>.removeAt(index: Int): T
fun <T> MutableList<T>.removeFirst(): T
fun <T> MutableList<T>.removeFirstOrNull(): T?
fun <T> MutableList<T>.removeLast(): T
fun <T> MutableList<T>.removeLastOrNull(): T?
fun <T> MutableCollection<T>.retainAll(elements: Collection<T>): Boolean
fun <T> MutableList<T>.retainAll(predicate: (T) -> Boolean): Boolean
fun <T> Iterable<T>.reversed(): List<T>
fun <T, R> Iterable<T>.scan(initial: R, operation: (acc: R, T) -> R): List<R>
fun <T, R> Iterable<T>.scanIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): List<R>
operator fun <T> MutableList<T>.set(index: Int, element: T): T
fun <T> MutableList<T>.shuffle()
fun <T> Iterable<T>.shuffled(): List<T>
fun <T> Iterable<T>.single(): T
fun <T> Iterable<T>.single(predicate: (T) -> Boolean): T
fun <T> Iterable<T>.singleOrNull(): T?
fun <T> Iterable<T>.singleOrNull(predicate: (T) -> Boolean): T?
//fun <T> List<T>.slice(indices: Iterable<Int>): List<T>
fun <T : Comparable<T>> MutableList<T>.sort()
fun <T, R : Comparable<R>> MutableList<T>.sortBy(selector: (T) -> R?)
fun <T, R : Comparable<R>> MutableList<T>.sortByDescending(selector: (T) -> R?)
fun <T : Comparable<T>> MutableList<T>.sortDescending()
fun <T : Comparable<T>> Iterable<T>.sorted(): List<T>
fun <T, R : Comparable<R>> Iterable<T>.sortedBy(selector: (T) -> R?): List<T>
fun <T, R : Comparable<R>> Iterable<T>.sortedByDescending(selector: (T) -> R?): List<T>
fun <T : Comparable<T>> Iterable<T>.sortedDescending(): List<T>
fun <T> List<T>.subList(fromIndex: Int, toIndex: Int): List<T>
//fun <T> List<Int>.sum(): Int
//fun <T> List<Long>.sum(): Long
//fun <T> List<Double>.sum(): Double
fun <T> Iterable<T>.take(n: Int): List<T>
fun <T> List<T>.takeLast(n: Int): List<T>
fun <T> List<T>.takeLastWhile(predicate: (T) -> Boolean): List<T>
fun <T> Iterable<T>.takeWhile(predicate: (T) -> Boolean): List<T>
fun <T> Iterable<T>.toList(): List<T>
fun <T> Iterable<T>.toMutableList(): MutableList<T>
fun <T, R> Iterable<Pair<T, R>>.unzip(): Pair<List<T>, List<R>>
//fun <T> Iterable<T>.windowed(
//    size: Int,
//    step: Int = 1,
//    partialWindows: Boolean = false
//): List<List<T>>
fun <T, R> Iterable<T>.windowed(
    size: Int,
    step: Int = 1,
    partialWindows: Boolean = false,
    transform: (List<T>) -> R
): List<R>
infix fun <T, R> Iterable<T>.zip(
    other: Iterable<R>
): List<Pair<T, R>>
fun <T, R, V> Iterable<T>.zip(
    other: Iterable<R>,
    transform: (a: T, b: R) -> V
): List<V>

////////////////////
// Map

val <K, V> Map<K, V>.size: Int
    get()
val <K, V> Map<K, V>.keys: Collection<K>
    get()
val <K, V> Map<K, V>.values: Collection<V>
    get()

val <K, V> MapEntry<K, V>.key: K
    get()
val <K, V> MapEntry<K, V>.value: V
    get()

fun <K, V> mapOf(vararg pairs: Pair<K, V>): Map<K, V>
fun <K, V> mutableMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V>

operator fun <K, V> Map<K, V>.contains(key: K): Boolean
fun <K> Map<K, *>.containsKey(key: K): Boolean
fun <K, V> Map<K, V>.containsValue(value: V): Boolean

operator fun <K, V> Map<K, V>.iterator(): Iterator<MapEntry<K, V>>

fun <K, V> Map<K, V>.all(
    predicate: (MapEntry<K, V>) -> Boolean
): Boolean

fun <K, V> Map<K, V>.any(): Boolean
fun <K, V> Map<K, V>.any(
    predicate: (MapEntry<K, V>) -> Boolean
): Boolean

//fun <K, V> Map<K, V>.asIterable(): Iterable<MapEntry<K, V>>

fun <T, K, V> Iterable<T>.associate(
    transform: (T) -> Pair<K, V>
): Map<K, V>

fun <T, K> Iterable<T>.associateBy(
    keySelector: (T) -> K
): Map<K, T>

fun <K, V> Iterable<K>.associateBy(
    valueSelector: (K) -> V
): Map<K, V>

fun <K, V> Iterable<K>.associateWith(
    valueSelector: (K) -> V
): Map<K, V>

fun <K, V> MutableMap<K, V>.clear()
fun <K, V> Map<K, V>.count(): Int
fun <K, V> Map<K, V>.count(predicate: (MapEntry<K, V>) -> Boolean): Int
//fun <K, V> emptyMap(): Map<K, V>
fun <K, V> Map<K, V>.filter(predicate: (MapEntry<K, V>) -> Boolean): Map<K, V>
fun <K, V> Map<K, V>.filterKeys(predicate: (K) -> Boolean): Map<K, V>
fun <K, V> Map<K, V>.filterNot(predicate: (MapEntry<K, V>) -> Boolean): Map<K, V>
fun <K, V> Map<K, V>.filterValues(predicate: (V) -> Boolean): Map<K, V>
fun <K, V, R : Any> Map<K, V>.firstNotNullOf(
    transform: (MapEntry<K, V>) -> R?
): R
fun <K, V, R : Any> Map<K, V>.firstNotNullOfOrNull(
    transform: (MapEntry<K, V>) -> R?
): R?
fun <K, V, R> Map<K, V>.flatMap(
    transform: (MapEntry<K, V>) -> Iterable<R>
): List<R>
fun <K, V> Map<K, V>.forEach(action: (MapEntry<K, V>) -> Unit)
operator fun <K, V> Map<K, V>.get(key: K): V?
fun <K, V> Map<K, V>.getOrElse(key: K, defaultValue: () -> V): V
fun <K, V> MutableMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <T, K> Iterable<T>.groupBy(
    keySelector: (T) -> K
): Map<K, List<T>>
fun <T, K, V> Iterable<T>.groupBy(
    keySelector: (T) -> K,
    valueSelector: (T) -> V,
): Map<K, List<V>>
fun <K, V> Map<K, V>.isNotEmpty(): Boolean
fun <K, V> Map<K, V>?.isNullOrEmpty(): Boolean
fun <K, V, R> Map<K, V>.map(transform: (MapEntry<K, V>) -> R): List<R>
fun <K, V, R> Map<K, V>.mapKeys(transform: (MapEntry<K, V>) -> R): Map<R, V>
fun <K, V, R : Any> Map<K, V>.mapNotNull(transform: (MapEntry<K, V>) -> R?): List<R>
fun <K, V, R> Map<K, V>.mapValues(transform: (MapEntry<K, V>) -> R): Map<K, R>
fun <K, V, R : Comparable<R>> Map<K, V>.maxBy(selector: (MapEntry<K, V>) -> R): MapEntry<K, V>
fun <K, V, R : Comparable<R>> Map<K, V>.maxByOrNull(selector: (MapEntry<K, V>) -> R): MapEntry<K, V>?
fun <K, V, R : Comparable<R>> Map<K, V>.maxOf(selector: (MapEntry<K, V>) -> R): R
fun <K, V, R : Comparable<R>> Map<K, V>.maxOfOrNull(selector: (MapEntry<K, V>) -> R): R?
fun <K, V, R : Comparable<R>> Map<K, V>.minBy(selector: (MapEntry<K, V>) -> R): MapEntry<K, V>
fun <K, V, R : Comparable<R>> Map<K, V>.minByOrNull(selector: (MapEntry<K, V>) -> R): MapEntry<K, V>?
fun <K, V, R : Comparable<R>> Map<K, V>.minOf(selector: (MapEntry<K, V>) -> R): R
fun <K, V, R : Comparable<R>> Map<K, V>.minOfOrNull(selector: (MapEntry<K, V>) -> R): R?
operator fun <K, V> Map<K, V>.minus(key: K): Map<K, V>
operator fun <K, V> Map<K, V>.minus(keys: List<K>): Map<K, V>
operator fun <K, V> MutableMap<K, V>.minusAssign(key: K)
operator fun <K, V> MutableMap<K, V>.minusAssign(keys: List<K>)
fun <K, V> Map<K, V>.none(): Boolean
fun <K, V> Map<K, V>.none(predicate: (MapEntry<K, V>) -> Boolean): Boolean
fun <K, V> Map<K, V>.onEach(action: (MapEntry<K, V>) -> Unit): Map<K, V>
fun <K, V> MutableMap<K, V>.onEach(action: (MapEntry<K, V>) -> Unit): MutableMap<K, V>
fun <K, V> Map<K, V>.onEachIndexed(action: (index: Int, MapEntry<K, V>) -> Unit): Map<K, V>
fun <K, V> MutableMap<K, V>.onEachIndexed(action: (index: Int, MapEntry<K, V>) -> Unit): MutableMap<K, V>
fun <K, V> Map<K, V>?.orEmpty(): Map<K, V>
operator fun <K, V> Map<K, V>.plus(pair: Pair<K, V>): Map<K, V>
operator fun <K, V> Map<K, V>.plus(pairs: List<Pair<K, V>>): Map<K, V>
operator fun <K, V> MutableMap<K, V>.plusAssign(pair: Pair<K, V>)
operator fun <K, V> MutableMap<K, V>.plusAssign(pairs: List<Pair<K, V>>)
fun <K, V> MutableMap<K, V>.put(key: K, value: V): V?
fun <K, V> MutableMap<K, V>.putAll(pairs: List<Pair<K, V>>)
fun <K, V> MutableMap<K, V>.remove(key: K): V?
operator fun <K, V> MutableMap<K, V>.set(key: K, value: V)
fun <K, V> Map<K, V>.toList(): List<Pair<K, V>>
fun <K, V> Iterable<Pair<K, V>>.toMap(): Map<K, V>
fun <K, V> Map<K, V>.toMap(): Map<K, V>
fun <K, V> Map<K, V>.toMutableMap(): MutableMap<K, V>
fun <K, V> Map<K, V>.withDefault(defaultValue: (key: K) -> V): Map<K, V>
fun <K, V> MutableMap<K, V>.withDefault(defaultValue: (key: K) -> V): MutableMap<K, V>

////////////////////
// Set

val <T> Set<T>.size: Int
    get()

operator fun <T> Set<T>.contains(element: T): Boolean
fun <T> Set<T>.containsAll(elements: Collection<T>): Boolean
fun <T> Set<T>.isEmpty(): Boolean
operator fun <T> Set<T>.iterator(): Iterator<T>
infix fun <T> Iterable<T>.intersect(
    other: Iterable<T>
): Set<T>
operator fun <T> Set<T>.minus(element: T): Set<T>
operator fun <T> Set<T>.minus(elements: Iterable<T>): Set<T>
fun <T> Set<T>.minusElement(element: T): Set<T>
fun <T> Set<T>?.orEmpty(): Set<T>
operator fun <T> Set<T>.plus(element: T): Set<T>
operator fun <T> Set<T>.plus(elements: Iterable<T>): Set<T>
fun <T> Set<T>.plusElement(element: T): Set<T>
infix fun <T> Iterable<T>.subtract(other: Iterable<T>): Set<T>
fun <T> Iterable<T>.toMutableSet(): MutableSet<T>
fun <T> Iterable<T>.toSet(): Set<T>
infix fun <T> Iterable<T>.union(other: Iterable<T>): Set<T>

fun <T> MutableSet<T>.add(element: T): Boolean
fun <T> MutableSet<T>.addAll(elements: Collection<T>): Boolean
fun <T> MutableSet<T>.clear()
fun <T> MutableSet<T>.remove(element: T): Boolean
fun <T> MutableSet<T>.removeAll(elements: Collection<T>): Boolean
fun <T> MutableSet<T>.retainAll(elements: Collection<T>): Boolean

//fun <T> mutableSetOf(): MutableSet<T>
fun <T> mutableSetOf(vararg elements: T): MutableSet<T>
//fun <T> setOf(): Set<T>
fun <T> setOf(vararg elements: T): Set<T>
fun <T : Any> setOfNotNull(vararg elements: T?): Set<T>

