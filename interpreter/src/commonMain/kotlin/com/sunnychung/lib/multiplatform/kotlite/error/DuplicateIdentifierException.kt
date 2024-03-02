package com.sunnychung.lib.multiplatform.kotlite.error

import com.sunnychung.lib.multiplatform.kotlite.model.SourcePosition

class DuplicateIdentifierException(position: SourcePosition, val name: String, val classifier: IdentifierClassifier) :
    SemanticException(position, "The ${classifier.toString().lowercase()} `$name` has been declared repeatedly")

enum class IdentifierClassifier {
    Property, Function, Class, TypeAlias, TypeResolution
}
