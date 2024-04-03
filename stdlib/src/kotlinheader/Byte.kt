// NOTE: ByteLibModule depends on TextLibModule and CollectionsLibModule

fun Int.toByte(): Byte
fun Long.toByte(): Byte

fun Byte.toInt(): Int
fun Byte.toLong(): Long
fun Byte.toDouble(): Double
fun Byte.toChar(): Char

val ByteArray.lastIndex: Int get()
val ByteArray.size: Int get()

fun byteArrayOf(vararg elements: Byte): ByteArray

fun ByteArray.asList(): List<Byte>
fun ByteArray.average(): Double
infix fun ByteArray?.contentEquals(other: ByteArray?): Boolean
fun ByteArray?.contentToString(): String
nullaware fun ByteArray.firstOrNull(predicate: (Byte) -> Boolean): Byte?
fun ByteArray?.contentHashCode(): Int
fun ByteArray.copyOf(): ByteArray
fun ByteArray.copyOf(newSize: Int): ByteArray
fun ByteArray.copyOfRange(fromIndex: Int, toIndex: Int): ByteArray
fun ByteArray.count(): Int
fun ByteArray.drop(n: Int): List<Byte>
fun ByteArray.dropLast(n: Int): List<Byte>
fun ByteArray.fill(element: Byte, fromIndex: Int = 0, toIndex: Int = size)
fun ByteArray.forEach(action: (Byte) -> Unit)
fun ByteArray.forEachIndexed(action: (index: Int, Byte) -> Unit)
fun ByteArray.first(): Byte
operator fun ByteArray.get(index: Int): Byte
operator fun ByteArray.iterator(): Iterator<Byte>
fun ByteArray.getOrElse(index: Int, defaultValue: (Int) -> Byte): Byte
fun ByteArray.getOrNull(index: Int): Byte?
fun ByteArray.indexOf(element: Byte): Int
fun ByteArray.indexOfFirst(predicate: (Byte) -> Boolean): Int
fun ByteArray.indexOfLast(predicate: (Byte) -> Boolean): Int
fun ByteArray.joinToString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    limit: Int = -1,
    truncated: String = "...",
    transform: ((Byte) -> String)? = null
): String
fun ByteArray.last(): Byte
fun <R> ByteArray.map(transform: (Byte) -> R): List<R>
fun <R> ByteArray.mapIndexed(transform: (index: Int, Byte) -> R): List<R>
fun ByteArray.max(): Byte
fun ByteArray.min(): Byte
fun ByteArray.none(): Boolean
fun ByteArray.onEach(action: (Byte) -> Unit): ByteArray
fun ByteArray.onEachIndexed(action: (index: Int, Byte) -> Unit): ByteArray
operator fun ByteArray.plus(element: Byte): ByteArray
operator fun ByteArray.plus(elements: ByteArray): ByteArray
fun ByteArray.random(): Byte
fun ByteArray.randomOrNull(): Byte?
fun ByteArray.reverse()
fun ByteArray.reversed(): List<Byte>
fun ByteArray.reversedArray(): ByteArray
operator fun ByteArray.set(index: Int, value: Byte)
fun ByteArray.single(): Byte
fun ByteArray.singleOrNull(): Byte?
fun ByteArray.sort()
fun ByteArray.sortDescending()
fun ByteArray.sorted(): List<Byte>
fun ByteArray.sortedArray(): ByteArray
fun ByteArray.sortedArrayDescending(): ByteArray
fun ByteArray.sortedDescending(): List<Byte>
fun ByteArray.sum(): Int
fun ByteArray.take(n: Int): List<Byte>
fun ByteArray.takeLast(n: Int): List<Byte>
fun Collection<Byte>.toByteArray(): ByteArray
fun ByteArray.toList(): List<Byte>
fun ByteArray.toSet(): Set<Byte>

//fun ByteArray.decodeToString(): String
fun ByteArray.decodeToString(
    startIndex: Int = 0,
    endIndex: Int = this.size,
    throwOnInvalidSequence: Boolean = false
): String
//fun String.encodeToByteArray(): ByteArray
fun String.encodeToByteArray(
    startIndex: Int = 0,
    endIndex: Int = this.length,
    throwOnInvalidSequence: Boolean = false
): ByteArray
