package com.owais.cryptoprofitcalculator

import retrofit2.http.GET
import retrofit2.http.Query

data class CoinPrice(
    val id: String,
    val symbol: String,
    val name: String,
    val current_price: Double,
    val image: String
)

interface CoinGeckoApi {
    @GET("coins/markets")
    suspend fun getTopCoins(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): List<CoinPrice>
}