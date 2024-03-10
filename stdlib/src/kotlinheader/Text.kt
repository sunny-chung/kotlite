// package kotlin.text

val String.length: Int
    get()

val String.lastIndex: Int
    get()

fun String.all(predicate: (Char) -> Boolean): Boolean
fun String.any(predicate: (Char) -> Boolean): Boolean
//fun String.chunked(size: Int): List<String>
fun String.commonPrefixWith(other: String, ignoreCase: Boolean = false): String
fun String.commonSuffixWith(other: String, ignoreCase: Boolean = false): String
fun String.compareTo(other: String, ignoreCase: Boolean = false): Int
fun String.contains(other: String, ignoreCase: Boolean = false): Boolean
fun String.contains(other: Char, ignoreCase: Boolean = false): Boolean
fun String.count(): Int
fun String.drop(n: Int): String
fun String.dropLast(n: Int): String
fun String.dropLastWhile(predicate: (Char) -> Boolean): String
fun String.dropWhile(predicate: (Char) -> Boolean): String
fun String.endsWith(suffix: String, ignoreCase: Boolean = false): Boolean
fun String?.equals(other: String?, ignoreCase: Boolean = false): Boolean
fun String.filter(predicate: (Char) -> Boolean): String
fun String.filterIndexed(predicate: (Int, Char) -> Boolean): String
fun String.filterNot(predicate: (Char) -> Boolean): String
fun String.first(): Char
fun String.firstOrNull(): Char?
//fun <R> String.flatMap(transform: (Char) -> List<R>): List<R>
//fun <R> String.flatMapIndexed(transform: (Int, Char) -> List<R>): List<R>
fun String.forEach(action: (Char) -> Unit)
fun String.forEachIndexed(action: (Int, Char) -> Unit)
fun String.getOrElse(index: Int, defaultValue: (Int) -> Char): Char
fun String.getOrNull(index: Int): Char?
fun String.indexOf(string: String, startIndex: Int = 0, ignoreCase: Boolean = false): Int
fun String.indexOfFirst(predicate: (Char) -> Boolean): Int
fun String.indexOfLast(predicate: (Char) -> Boolean): Int
fun String.isBlank(): Boolean
fun String.isEmpty(): Boolean
fun String.isNotBlank(): Boolean
fun String.isNotEmpty(): Boolean
fun String?.isNullOrBlank(): Boolean
fun String?.isNullOrEmpty(): Boolean
fun String.last(): Char
fun String.lastIndexOf(string: String, startIndex: Int = lastIndex, ignoreCase: Boolean = false): Int
fun String.lastOrNull(): Char?
//fun String.lines(): List<String>
fun String.lowercase(): String
//fun <R> String.map(transform: (Char) -> R): List<R>
//fun <R> String.mapIndexed(transform: (Int, Char) -> R): List<R>
//fun <R> String.mapIndexedNotNull(transform: (Int, Char) -> R?): List<R>
//fun <R> String.mapNotNull(transform: (Char) -> R?): List<R>
fun String.none(predicate: (Char) -> Boolean): Boolean
fun String?.orEmpty(): String
fun String.padEnd(length: Int, padChar: Char = ' '): String
fun String.padStart(length: Int, padChar: Char = ' '): String
fun String.prependIndent(indent: String = " "): String // TODO verify
fun String.random(): Char
fun String.randomOrNull(): Char?
fun String.removePrefix(prefix: String): String
fun String.removeRange(startIndex: Int, endIndex: Int): String
fun String.removeSuffix(suffix: String): String
fun String.removeSurrounding(prefix: String, suffix: String): String
fun String.repeat(n: Int): String
fun String.replace(oldValue: Char, newValue: Char, ignoreCase: Boolean = false): String
fun String.replace(oldValue: String, newValue: String, ignoreCase: Boolean = false): String
fun String.replaceAfter(delimiter: String, replacement: String, missingDelimiterValue: String = this): String
fun String.replaceAfterLast(delimiter: String, replacement: String, missingDelimiterValue: String = this): String
fun String.replaceBefore(delimiter: String, replacement: String, missingDelimiterValue: String = this): String
fun String.replaceBeforeLast(delimiter: String, replacement: String, missingDelimiterValue: String = this): String
fun String.replaceFirst(oldValue: String, newValue: String, ignoreCase: Boolean = false): String
fun String.replaceRange(startIndex: Int, endIndex: Int, replacement: String): String
fun String.reversed(): String
fun String.single(): Char
fun String.singleOrNull(): Char?
//fun String.split(delimiter: String, ignoreCase: Boolean = false, limit: Int = 0): List<String>
fun String.startsWith(prefix: String, ignoreCase: Boolean = false): Boolean
fun String.substring(startIndex: Int, endIndex: Int = length): String
fun String.substringAfter(delimiter: String, missingDelimiterValue: String = this): String
fun String.substringAfterLast(delimiter: String, missingDelimiterValue: String = this): String
fun String.substringBefore(delimiter: String, missingDelimiterValue: String = this): String
fun String.substringBeforeLast(delimiter: String, missingDelimiterValue: String = this): String
fun String.take(n: Int): String
fun String.takeLast(n: Int): String
fun String.takeLastWhile(predicate: (Char) -> Boolean): String
fun String.takeWhile(predicate: (Char) -> Boolean): String
fun String?.toBoolean(): Boolean
fun String.toBooleanStrictOrNull(): Boolean?
fun String.toDouble(): Double
fun String.toDoubleOrNull(): Double?
fun String.toInt(): Int
fun String.toInt(radix: Int): Int
fun String.toIntOrNull(): Int?
fun String.toIntOrNull(radix: Int): Int?
fun String.trim(): String
fun String.trimEnd(): String
fun String.trimStart(): String
fun String.uppercase(): String

fun Char.isDefined(): Boolean
fun Char.isDigit(): Boolean
fun Char.isHighSurrogate(): Boolean
fun Char.isISOControl(): Boolean
fun Char.isLetter(): Boolean
fun Char.isLetterOrDigit(): Boolean
fun Char.isLowerCase(): Boolean
fun Char.isLowSurrogate(): Boolean
fun Char.isSurrogate(): Boolean
fun Char.isUpperCase(): Boolean
fun Char.isWhitespace(): Boolean
fun Char.lowercase(): String
fun Char.lowercaseChar(): Char
fun Char.uppercase(): String
fun Char.uppercaseChar(): Char
