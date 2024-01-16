// package kotlin.text

fun String.matches(regex: Regex): Boolean
fun String.replace(regex: Regex, replacement: String): String
fun String.replaceFirst(regex: Regex, replacement: String): String
//fun String.split(regex: Regex, limit: Int = 0): List<String>
fun String.toRegex(): Regex
//fun String.toRegex(option: RegexOption): Regex
//fun String.toRegex(options: Set<RegexOption>): Regex
