package com.owais.cryptoprofitcalculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class Currency(val code: String, val symbol: String, var rateMultiplier: Double)

object CurrencyState {
    var currentCurrency by mutableStateOf(Currency("USD", "$", 1.0))

    var availableCurrencies by mutableStateOf(
        listOf(
            Currency("USD", "$", 1.0),
            Currency("PKR", "Rs ", 278.5), // Fallback default
            Currency("INR", "₹", 83.2),   // Fallback default
            Currency("EUR", "€", 0.92)    // Fallback default
        )
    )
        private set

    // Fetch live currency exchange rates from a free public API endpoint
    suspend fun fetchLiveExchangeRates() {
        withContext(Dispatchers.IO) {
            try {
                // Free endpoint tracking USD-based global exchange rates
                val url = URL("https://open.er-api.com/v6/latest/USD")
                val response = url.readText()
                val json = JSONObject(response)
                val rates = json.getJSONObject("rates")

                val pkrRate = rates.optDouble("PKR", 278.5)
                val inrRate = rates.optDouble("INR", 83.2)
                val eurRate = rates.optDouble("EUR", 0.92)

                withContext(Dispatchers.Main) {
                    availableCurrencies = listOf(
                        Currency("USD", "$", 1.0),
                        Currency("PKR", "Rs ", pkrRate),
                        Currency("INR", "₹", inrRate),
                        Currency("EUR", "€", eurRate)
                    )
                    // Keep active currency selection synced with the newly fetched rate
                    val updatedCurrent = availableCurrencies.find { it.code == currentCurrency.code }
                    if (updatedCurrent != null) {
                        currentCurrency = updatedCurrent
                    }
                }
            } catch (_: Exception) {
                // Fallback silently to static rates if offline
            }
        }
    }
}