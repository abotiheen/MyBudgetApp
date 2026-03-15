package com.example.mybudgetapp.data

import kotlin.math.abs
import kotlin.math.roundToLong
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

fun formatCurrencyIraqiDinar(amount: Double): String {
    val decimalFormat = DecimalFormat("#,##0.###")
    return decimalFormat.format(amount)
}

fun formatCompactCurrencyIraqiDinar(amount: Double): String {
    val absoluteAmount = abs(amount)
    if (absoluteAmount < 1_000) {
        return amount.roundToLong().toString()
    }

    val (scaledValue, pattern, suffix) = when {
        absoluteAmount >= 1_000_000 -> Triple(amount / 1_000_000.0, "0.###", "m")
        else -> Triple(amount / 1_000.0, "0.##", "k")
    }

    return DecimalFormat(pattern).format(scaledValue) + suffix
}

fun String.capitalized(): String {
    return this.substring(0, 1).uppercase(Locale.ROOT) + this.substring(1).lowercase(Locale.ROOT)
    }
