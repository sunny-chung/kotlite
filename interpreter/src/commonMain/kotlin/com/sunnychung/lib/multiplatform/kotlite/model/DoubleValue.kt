package com.sunnychung.lib.multiplatform.kotlite.model

data class DoubleValue(override val value: Double) : NumberValue<Double> {
    override fun type() = DoubleType()
}
