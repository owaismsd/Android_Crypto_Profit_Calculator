package com.owais.cryptoprofitcalculator

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("CryptoCalcPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    val historyList = mutableStateListOf<CalculationHistory>()
    val coinList = mutableStateListOf<CoinPrice>()

    var showResetSuccessDialog by mutableStateOf(false)
        private set

    init {
        loadHistory()
        fetchCoins()
    }

    private fun loadHistory() {
        val json = sharedPreferences.getString("history_list_key", null)
        if (json != null) {
            val type = object : TypeToken<List<CalculationHistory>>() {}.type
            val savedList: List<CalculationHistory> = gson.fromJson(json, type)
            historyList.clear()
            historyList.addAll(savedList)
        }
    }

    private fun persistHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val json = gson.toJson(historyList.toList())
            sharedPreferences.edit().putString("history_list_key", json).apply()
        }
    }

    fun saveCalculation(history: CalculationHistory) {
        historyList.add(0, history)
        persistHistory()
    }

    fun removeHistoryItem(history: CalculationHistory) {
        historyList.remove(history)
        persistHistory()
    }

    fun resetHistory() {
        historyList.clear()
        sharedPreferences.edit().remove("history_list_key").apply()
        showResetSuccessDialog = true
    }

    fun dismissResetDialog() {
        showResetSuccessDialog = false
    }

    private fun fetchCoins() {
        viewModelScope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.coingecko.com/api/v3/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(CoinGeckoApi::class.java)
                val coins = api.getTopCoins()

                coinList.clear()
                coinList.addAll(coins)
            } catch (_: Exception) {}
        }
    }
}