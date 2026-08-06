package com.owais.cryptoprofitcalculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Currency(val code: String, val symbol: String, val rateMultiplier: Double)

object CurrencyState {
    var currentCurrency by mutableStateOf(Currency("USD", "$", 1.0))

    val availableCurrencies = listOf(
        Currency("USD", "$", 1.0),
        Currency("PKR", "Rs ", 278.5),
        Currency("INR", "₹", 83.2),
        Currency("EUR", "€", 0.92)
    )
}