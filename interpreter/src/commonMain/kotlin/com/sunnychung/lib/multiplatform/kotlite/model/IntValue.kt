package com.sunnychung.lib.multiplatform.kotlite.model

data class IntValue(override val value: Int) : NumberValue<Int> {
    override fun type() = IntType()
}
