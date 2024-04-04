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
