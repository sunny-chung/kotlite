package com.sunnychung.lib.multiplatform.kotlite.model

sealed interface RuntimeValue {
    fun type(): DataType

    fun convertToString(): String
}
//data class RuntimeValue(val type: DataType, val value: Any?)

data object UnitValue : RuntimeValue {
    override fun type() = UnitType()

    override fun convertToString() = "Unit"
}
data object NullValue : RuntimeValue {
    override fun type() = NullType

    override fun convertToString() = "null"
}

data class BooleanValue(val value: Boolean) : RuntimeValue {
    override fun type() = BooleanType()

    override fun convertToString() = value.toString()
}

data class StringValue(override val value: String) : ComparableRuntimeValue<String> {
    override fun type() = StringType()

    override fun convertToString() = value
}

data class CharValue(override val value: Char) : ComparableRuntimeValue<Char> {
    override fun type() = CharType()

    override fun convertToString() = value.toString()
}

class LambdaValue(val value: LambdaLiteralNode, private val resolvedType: FunctionType, val symbolRefs: SymbolTable) : RuntimeValue {
    override fun type() = resolvedType

    override fun convertToString() = "Lambda()"
}
