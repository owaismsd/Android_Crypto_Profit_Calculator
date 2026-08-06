package com.owais.cryptoprofitcalculator

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PriceCache {

    private const val PREFS_NAME = "price_cache_prefs"
    private const val KEY_COINS = "cached_coins"
    private const val KEY_TIMESTAMP = "cached_timestamp"

    fun saveCoins(context: Context, coins: List<CoinPrice>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(coins)
        prefs.edit()
            .putString(KEY_COINS, json)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun loadCoins(context: Context): List<CoinPrice> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_COINS, null) ?: return emptyList()
        val type = object : TypeToken<List<CoinPrice>>() {}.type
        return try {
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getLastUpdatedTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_TIMESTAMP, 0L)
    }
}