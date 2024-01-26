package com.sunnychung.lib.multiplatform.kotlite.error

class DuplicateIdentifierException(val name: String, val classifier: IdentifierClassifier) :
    SemanticException("The ${classifier.toString().lowercase()} `$name` has been declared repeatedly")

enum class IdentifierClassifier {
    Property, Function, Class, TypeAlias, TypeResolution
}
