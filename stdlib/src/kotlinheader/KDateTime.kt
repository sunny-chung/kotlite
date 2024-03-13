operator fun KInstant.plus(duration: KDuration): KInstant
operator fun KInstant.minus(duration: KDuration): KInstant
infix fun KInstant.at(zoneOffset: KZoneOffset): KZonedInstant
fun KInstant.atZoneOffset(zoneOffset: KZoneOffset): KZonedInstant
fun KInstant.atLocalZoneOffset(): KZonedInstant
fun KInstant.Companion.now(): KInstant
fun KInstant.Companion.parseFrom(input: String, formats: List<KDateTimeFormat>): KInstant

fun KDateTimeFormattable.toMilliseconds(): Long
fun KDateTimeFormattable.hourPart(): Int
fun KDateTimeFormattable.minutePart(): Int
fun KDateTimeFormattable.secondPart(): Int
fun KDateTimeFormattable.millisecondPart(): Int
fun KDateTimeFormattable.format(pattern: String): String

fun KPointOfTime.toEpochMilliseconds(): Long
operator fun KPointOfTime.minus(other: KPointOfTime): KDuration
fun KPointOfTime.toIso8601String(): String
fun KPointOfTime.toIso8601StringWithMilliseconds(): String

operator fun KZonedInstant.plus(duration: KDuration): KZonedInstant
operator fun KZonedInstant.minus(duration: KDuration): KZonedInstant
fun KZonedInstant.startOfDay(): KZonedInstant
fun KZonedInstant.dropZoneOffset(): KInstant
fun KZonedInstant.Companion.nowAtLocalZoneOffset(): KZonedInstant
fun KZonedInstant.Companion.nowAtZoneOffset(zoneOffset: KZoneOffset): KZonedInstant
fun KZonedInstant.Companion.parseFrom(input: String, formats: List<KDateTimeFormat>): KZonedInstant
fun KZonedInstant.Companion.parseFromIso8601String(input: String): KZonedInstant
fun KZonedInstant.toKZonedDateTime(): KZonedDateTime
val KZonedInstant.zoneOffset: KZoneOffset
    get()

fun KZoneOffset.toMilliseconds(): Long
fun KZoneOffset.toDisplayString(): String
val KZoneOffset.Companion.UTC: KZoneOffset
    get()
fun KZoneOffset.Companion.parseFrom(string: String): KZoneOffset
fun KZoneOffset.Companion.fromMilliseconds(millis: Long): KZoneOffset
fun KZoneOffset.Companion.local(): KZoneOffset

operator fun KDuration.plus(other: KDuration): KDuration
//fun KDuration.Companion.of(value: Int, unit: KFixedTimeUnit): KDuration
//fun KDuration.Companion.of(value: Long, unit: KFixedTimeUnit): KDuration
val KDuration.Companion.ZERO: KDuration
    get()
fun KDuration.toSeconds(): Long
fun KDuration.toMinutes(): Long
fun KDuration.toHours(): Long
fun KDuration.toDays(): Long
fun KDuration.toWeeks(): Long

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
fun KZonedDateTime.format(pattern: String): String
fun KZonedDateTime.toIso8601String(): String
fun KZonedDateTime.toIso8601StringWithMilliseconds(): String
operator fun KZonedDateTime.plus(duration: KDuration): KZonedDateTime
operator fun KZonedDateTime.minus(duration: KDuration): KZonedDateTime
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

var KDateTimeFormat.pattern: String
    get()
var KDateTimeFormat.weekDayNames: List<String>
    get()
    set(value)

fun KDateTimeFormat.format(datetime: KDateTimeFormattable): String
fun KDateTimeFormat.parseToKZonedDateTime(input: String): KZonedDateTime
val KDateTimeFormat.Companion.ISO8601_DATETIME: KDateTimeFormat
    get()
val KDateTimeFormat.Companion.FULL: KDateTimeFormat
    get()
val KDateTimeFormat.Companion.ISO8601_FORMATS: List<KDateTimeFormat>
    get()
val KDateTimeFormat.Companion.IOS_DATE_FORMATS: List<KDateTimeFormat>
    get()
val KDateTimeFormat.Companion.WEEKDAY_NAMES: List<String>
    get()

//val KFixedTimeUnit.ratioToMillis: Long
//    get()
//val KFixedTimeUnit.Companion.MilliSecond: KFixedTimeUnit
//    get()
//val KFixedTimeUnit.Companion.Second: KFixedTimeUnit
//    get()
//val KFixedTimeUnit.Companion.Minute: KFixedTimeUnit
//    get()
//val KFixedTimeUnit.Companion.Hour: KFixedTimeUnit
//    get()
//val KFixedTimeUnit.Companion.Day: KFixedTimeUnit
//    get()
//val KFixedTimeUnit.Companion.Week: KFixedTimeUnit
//    get()

fun Int.milliseconds(): KDuration
fun Long.milliseconds(): KDuration
fun Int.seconds(): KDuration
fun Long.seconds(): KDuration
fun Int.minutes(): KDuration
fun Int.hours(): KDuration
fun Int.days(): KDuration
fun Int.weeks(): KDuration

