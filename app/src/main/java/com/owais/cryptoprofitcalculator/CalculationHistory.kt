package com.owais.cryptoprofitcalculator

data class CalculationHistory(
    val coinName: String = "",
    val amountInvested: Double = 0.0,
    val currentPrice: Double = 0.0,
    val targetPrice: Double = 0.0,
    val futureValue: Double = 0.0,
    val profit: Double = 0.0,
    val profitPercent: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)