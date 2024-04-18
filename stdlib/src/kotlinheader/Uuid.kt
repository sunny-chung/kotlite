fun uuid4(): Uuid
fun uuidFrom(string: String): Uuid
fun uuidOf(bytes: ByteArray): Uuid

val Uuid.mostSignificantBits: Long get()
val Uuid.leastSignificantBits: Long get()
val Uuid.bytes: ByteArray get()
val Uuid.variant: Int get()
val Uuid.version: Int get()

fun Uuid.toString(): String
