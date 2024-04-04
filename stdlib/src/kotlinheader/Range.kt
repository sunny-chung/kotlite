val <T : Comparable<T>> ClosedRange<T>.start: T get()
val <T : Comparable<T>> ClosedRange<T>.endInclusive: T get()

operator fun <T : Comparable<T>> ClosedRange<T>.contains(value: T): Boolean
fun <T : Comparable<T>> ClosedRange<T>.isEmpty(): Boolean

val <T : Comparable<T>> OpenEndRange<T>.start: T get()
val <T : Comparable<T>> OpenEndRange<T>.endExclusive: T get()

operator fun <T : Comparable<T>> OpenEndRange<T>.contains(value: T): Boolean
fun <T : Comparable<T>> OpenEndRange<T>.isEmpty(): Boolean

operator fun <T : Comparable<T>> T.rangeTo(that: T): ClosedRange<T>
operator fun <T : Comparable<T>> T.rangeUntil(that: T): OpenEndRange<T>

//operator fun ClosedRange<Int>.contains(value: Byte): Boolean
//operator fun ClosedRange<Long>.contains(value: Byte): Boolean
//operator fun ClosedRange<Long>.contains(value: Int): Boolean
//operator fun ClosedRange<Byte>.contains(value: Int): Boolean
//operator fun ClosedRange<Int>.contains(value: Long): Boolean
//operator fun ClosedRange<Byte>.contains(value: Long): Boolean
//operator fun OpenEndRange<Int>.contains(value: Byte): Boolean
//operator fun OpenEndRange<Long>.contains(value: Byte): Boolean
//operator fun OpenEndRange<Long>.contains(value: Int): Boolean
//operator fun OpenEndRange<Byte>.contains(value: Int): Boolean
//operator fun OpenEndRange<Int>.contains(value: Long): Boolean
//operator fun OpenEndRange<Byte>.contains(value: Long): Boolean

val IntProgression.first: Int get()
val IntProgression.last: Int get()
val IntProgression.step: Int get()
fun IntProgression.isEmpty(): Boolean
fun IntProgression.reversed(): IntProgression
infix fun IntProgression.step(step: Int): IntProgression

operator fun IntRange.contains(element: Int?): Boolean
fun IntRange.random(): Int
fun IntRange.randomOrNull(): Int?

infix fun Int.downTo(to: Byte): IntProgression
infix fun Int.downTo(to: Int): IntProgression
//infix fun Int.downTo(to: Long): LongProgression

operator fun Int.rangeTo(that: Int): IntRange
operator fun Int.rangeUntil(that: Int): IntRange
