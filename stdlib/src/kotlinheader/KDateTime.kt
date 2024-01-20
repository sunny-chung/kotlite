fun KInstant.plus(duration: KDuration): KInstant
fun KInstant.minus(duration: KDuration): KInstant
fun KInstant.toMilliseconds(): Long
fun KInstant.toEpochMilliseconds(): Long
fun KInstant.atZoneOffset(zoneOffset: KZoneOffset): KZonedInstant
//fun KInstant.atLocalZoneOffset(): KZonedInstant
fun KInstant.Companion.now(): KInstant
//fun KInstant.Companion.parseFrom(input: String, formats: List<KDateTimeFormat>): KInstant
fun KInstant.hourPart(): Int
fun KInstant.minutePart(): Int
fun KInstant.secondPart(): Int
fun KInstant.millisecondPart(): Int
fun KInstant.format(pattern: String): String

// fun minus(other: KPointOfTime): KDuration

fun KZonedInstant.plus(duration: KDuration): KZonedInstant
fun KZonedInstant.minus(duration: KDuration): KZonedInstant
fun KZonedInstant.toMilliseconds(): Long
fun KZonedInstant.toEpochMilliseconds(): Long
fun KZonedInstant.startOfDay(): KZonedInstant
fun KZonedInstant.dropZoneOffset(): KInstant
fun KZonedInstant.Companion.nowAtLocalZoneOffset(): KZonedInstant
fun KZonedInstant.Companion.nowAtZoneOffset(zoneOffset: KZoneOffset): KZonedInstant
//fun KZonedInstant.Companion.parseFrom(input: String, formats: List<KDateTimeFormat>): KZonedInstant
fun KZonedInstant.Companion.parseFromIso8601String(input: String): KZonedInstant
fun KZonedInstant.toKZonedDateTime(): KZonedDateTime
fun KZonedInstant.hourPart(): Int
fun KZonedInstant.minutePart(): Int
fun KZonedInstant.secondPart(): Int
fun KZonedInstant.millisecondPart(): Int
fun KZonedInstant.format(pattern: String): String
val KZonedInstant.zoneOffset: KZoneOffset
    get()

fun KZoneOffset.toMilliseconds(): Long
fun KZoneOffset.toDisplayString(): String
val KZoneOffset.Companion.UTC: KZoneOffset
    get()
fun KZoneOffset.Companion.parseFrom(string: String): KZoneOffset
fun KZoneOffset.Companion.fromMilliseconds(millis: Long): KZoneOffset
fun KZoneOffset.Companion.local(): KZoneOffset

fun KDuration.plus(other: KDuration): KDuration
fun KDuration.toMilliseconds(): Long
//fun KDuration.Companion.of(value: Int, unit: KFixedTimeUnit): KDuration
//fun KDuration.Companion.of(value: Long, unit: KFixedTimeUnit): KDuration
val KDuration.Companion.ZERO: KDuration
    get()
fun KDuration.hourPart(): Int
fun KDuration.minutePart(): Int
fun KDuration.secondPart(): Int
fun KDuration.millisecondPart(): Int
fun KDuration.format(pattern: String): String

fun KZonedDateTime.toKZonedInstant(): KZonedInstant
//fun KZonedDateTime.datePart(): KDate
fun KZonedDateTime.startOfDay(): KZonedDateTime
fun KZonedDateTime.copy(
    year: Int? = null,
    month: Int? = null,
    day: Int? = null,
    hour: Int? = null,
    minute: Int? = null,
    second: Int? = null,
    millisecond: Int? = null,
    zoneOffset: KZoneOffset? = null
): KZonedDateTime
fun KZonedDateTime.plus(duration: KDuration): KZonedDateTime
fun KZonedDateTime.minus(duration: KDuration): KZonedDateTime
//fun KZonedDateTime.format(pattern: String): String
val KZonedDateTime.year: Int
    get()
val KZonedDateTime.month: Int
    get()
val KZonedDateTime.day: Int
    get()
val KZonedDateTime.hour: Int
    get()
val KZonedDateTime.minute: Int
    get()
val KZonedDateTime.second: Int
    get()
val KZonedDateTime.millisecond: Int
    get()
val KZonedDateTime.zoneOffset: KZoneOffset
    get()

//fun KDate.addDays(days: Int): KDate
//fun KDate.dayOfWeek(): Int

//var KDateTimeFormat.weekDayNames: List<String>
//    get()
//    set(value)

//fun KDateTimeFormat.format(datetime: KDateTimeFormattable): String
fun KDateTimeFormat.format(datetime: KInstant): String
fun KDateTimeFormat.format(datetime: KZonedInstant): String
fun KDateTimeFormat.format(datetime: KDuration): String
fun KDateTimeFormat.parseToKZonedDateTime(input: String): KZonedDateTime
val KDateTimeFormat.pattern: String
    get()
val KDateTimeFormat.Companion.ISO8601_DATETIME: KDateTimeFormat
    get()
val KDateTimeFormat.Companion.FULL: KDateTimeFormat
    get()
//val KDateTimeFormat.Companion.ISO8601_FORMATS: List<KDateTimeFormat>
//    get()
//val KDateTimeFormat.Companion.IOS_DATE_FORMATS: List<KDateTimeFormat>
//    get()
//val KDateTimeFormat.Companion.WEEKDAY_NAMES: List<String>
//    get()

fun Int.milliseconds(): KDuration
fun Int.seconds(): KDuration
fun Int.minutes(): KDuration
fun Int.hours(): KDuration
fun Int.days(): KDuration
fun Int.weeks(): KDuration

