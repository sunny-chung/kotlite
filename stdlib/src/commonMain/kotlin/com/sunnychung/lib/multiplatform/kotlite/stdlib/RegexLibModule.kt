package com.sunnychung.lib.multiplatform.kotlite.stdlib

import com.sunnychung.lib.multiplatform.kotlite.model.ProvidedClassDefinition
import com.sunnychung.lib.multiplatform.kotlite.stdlib.regex.RegexValue

class RegexLibModule : AbstractRegexLibModule() {
    override val classes: List<ProvidedClassDefinition> = listOf(RegexValue.clazz)
}
