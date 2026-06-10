package com.mool.core.ui

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

fun Double.toFixed(decimals: Int): String {
    val abs = abs(this)
    val factor = 10.0.pow(decimals)
    val whole = (abs * factor).roundToLong()
    val intPart = whole / factor.toLong()
    val fracPart = (whole % factor.toLong()).toString().padStart(decimals, '0')
    val sign = if (this < 0) "-" else ""
    return "$sign$intPart.$fracPart"
}
