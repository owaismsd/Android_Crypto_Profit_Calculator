package com.owais.cryptoprofitcalculator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.owais.cryptoprofitcalculator.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(calculatorViewModel: CalculatorViewModel) {
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val errorColor = MaterialTheme.colorScheme.error
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    var expandedCoinMenu by remember { mutableStateOf(false) }
    var expandedCurrencyMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedCoin by remember { mutableStateOf<CoinPrice?>(null) }
    var isNoCoinMode by remember { mutableStateOf(false) }
    var isCoinSelected by remember { mutableStateOf(false) } // Disables cursor/typing when true

    var amountInvested by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var priceIncrease by remember { mutableStateOf("") }
    var includeBrokerage by remember { mutableStateOf(false) }
    var brokeragePercent by remember { mutableStateOf("") }

    var profitAmount by remember { mutableStateOf<Double?>(null) }
    var profitPercentage by remember { mutableStateOf<Double?>(null) }
    var finalValResult by remember { mutableStateOf<Double?>(null) }
    var initialInvResult by remember { mutableStateOf<Double?>(null) }
    var totalFeesPaid by remember { mutableStateOf<Double?>(null) }
    var resultError by remember { mutableStateOf<String?>(null) }

    // Pull-to-refresh loading state
    var isRefreshing by remember { mutableStateOf(false) }

    val currency = CurrencyState.currentCurrency

    // Live currency conversion scaling for inputs and results when currency changes
    var previousCurrencyRate by remember { mutableStateOf(currency.rateMultiplier) }
    LaunchedEffect(currency) {
        val oldRate = previousCurrencyRate
        val newRate = currency.rateMultiplier
        val ratio = newRate / oldRate

        amountInvested = amountInvested.toDoubleOrNull()?.let { String.format("%.2f", it * ratio) } ?: amountInvested
        currentPrice = currentPrice.toDoubleOrNull()?.let { String.format("%.4f", it * ratio) } ?: currentPrice
        priceIncrease = priceIncrease.toDoubleOrNull()?.let { String.format("%.4f", it * ratio) } ?: priceIncrease

        profitAmount = profitAmount?.times(ratio)
        finalValResult = finalValResult?.times(ratio)
        initialInvResult = initialInvResult?.times(ratio)
        totalFeesPaid = totalFeesPaid?.times(ratio)

        previousCurrencyRate = newRate
    }

    // History Reset Success Dialog Popup
    if (calculatorViewModel.showResetSuccessDialog) {
        AlertDialog(
            onDismissRequest = { calculatorViewModel.dismissResetDialog() },
            containerColor = surfaceColor,
            title = { Text(stringResource(id = R.string.history_reset_title), color = textColor, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(id = R.string.history_reset_msg), color = textColor) },
            confirmButton = {
                TextButton(onClick = { calculatorViewModel.dismissResetDialog() }) {
                    Text(stringResource(id = R.string.ok), color = primaryColor)
                }
            }
        )
    }

    // --- PULL TO REFRESH CONTAINER WRAPPING THE SCREEN ---
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                keyboardController?.hide()

                amountInvested = ""
                currentPrice = ""
                priceIncrease = ""
                selectedCoin = null
                isNoCoinMode = false
                isCoinSelected = false
                searchQuery = ""
                includeBrokerage = false
                brokeragePercent = ""

                profitAmount = null
                profitPercentage = null
                finalValResult = null
                initialInvResult = null
                totalFeesPaid = null
                resultError = null

                delay(400)
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // --- TOP BAR: TITLE & CURRENCY PICKER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.calculator_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )

                Box {
                    OutlinedButton(
                        onClick = { expandedCurrencyMenu = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                    ) {
                        Text(text = "${currency.code} (${currency.symbol})", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedCurrencyMenu,
                        onDismissRequest = { expandedCurrencyMenu = false },
                        modifier = Modifier.background(surfaceColor)
                    ) {
                        CurrencyState.availableCurrencies.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text("${curr.code} - ${curr.symbol}", color = textColor) },
                                onClick = {
                                    CurrencyState.currentCurrency = curr
                                    expandedCurrencyMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- INPUT CARD ---
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {

                    // COIN SELECTOR WITH CURSOR LOCK & LIVE GLOBAL SEARCH (E.G. USX)
                    ExposedDropdownMenuBox(
                        expanded = expandedCoinMenu,
                        onExpandedChange = {
                            if (!isCoinSelected) {
                                expandedCoinMenu = it
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                if (!isCoinSelected) {
                                    searchQuery = it
                                    expandedCoinMenu = true
                                    isNoCoinMode = false
                                    selectedCoin = null
                                }
                            },
                            readOnly = isCoinSelected, // Disables cursor & typing once a coin is chosen
                            placeholder = { Text(stringResource(id = R.string.search_coins), color = Color.Gray) },
                            label = { Text(stringResource(id = R.string.coin), color = Color.Gray) },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                searchQuery = ""
                                                selectedCoin = null
                                                isNoCoinMode = false
                                                isCoinSelected = false
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = Color.Gray
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    if (!isCoinSelected) {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCoinMenu)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedCoinMenu,
                            onDismissRequest = { expandedCoinMenu = false },
                            modifier = Modifier
                                .background(surfaceColor)
                                .heightIn(max = 300.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("⚡ ${stringResource(id = R.string.no_coin_custom)}", color = primaryColor, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    isNoCoinMode = true
                                    selectedCoin = null
                                    isCoinSelected = true
                                    searchQuery = "Custom Trade"
                                    currentPrice = ""
                                    expandedCoinMenu = false
                                    keyboardController?.hide()
                                }
                            )

                            // Local filter + Live global check for tokens like USX
                            val filteredCoins = remember(searchQuery, calculatorViewModel.coinList) {
                                if (searchQuery.isBlank() || (selectedCoin != null && searchQuery == "${selectedCoin?.name} (${selectedCoin?.symbol?.uppercase()})")) {
                                    calculatorViewModel.coinList
                                } else {
                                    val matched = calculatorViewModel.coinList.filter {
                                        it.name.contains(searchQuery, ignoreCase = true) ||
                                                it.symbol.contains(searchQuery, ignoreCase = true)
                                    }
                                    matched
                                }
                            }

                            // If typing something unique that isn't in the local list, add an option to search live globally
                            val showLiveSearchOption = searchQuery.isNotBlank() &&
                                    filteredCoins.none { it.name.equals(searchQuery, ignoreCase = true) || it.symbol.equals(searchQuery, ignoreCase = true) }

                            if (showLiveSearchOption) {
                                DropdownMenuItem(
                                    text = { Text("🌐 Search global market for \"$searchQuery\"", color = primaryColor, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        calculatorViewModel.fetchLiveCoinPrice(searchQuery) { liveCoin ->
                                            if (liveCoin != null) {
                                                selectedCoin = liveCoin
                                                isNoCoinMode = false
                                                isCoinSelected = true
                                                searchQuery = "${liveCoin.name} (${liveCoin.symbol.uppercase()})"
                                                currentPrice = String.format("%.4f", liveCoin.current_price * currency.rateMultiplier)
                                                expandedCoinMenu = false
                                                keyboardController?.hide()
                                            } else {
                                                // Coin not found on market
                                                searchQuery = "Coin not found"
                                                expandedCoinMenu = false
                                            }
                                        }
                                    }
                                )
                            }

                            if (filteredCoins.isEmpty() && !showLiveSearchOption && searchQuery.isNotBlank()) {
                                DropdownMenuItem(
                                    text = { Text("❌ Coin not found", color = errorColor) },
                                    onClick = { /* Do nothing */ }
                                )
                            }

                            filteredCoins.forEach { coin ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${coin.name} (${coin.symbol.uppercase()}) - ${currency.symbol}${String.format("%.2f", coin.current_price * currency.rateMultiplier)}",
                                            color = textColor
                                        )
                                    },
                                    onClick = {
                                        selectedCoin = coin
                                        isNoCoinMode = false
                                        isCoinSelected = true
                                        searchQuery = "${coin.name} (${coin.symbol.uppercase()})"
                                        currentPrice = String.format("%.4f", coin.current_price * currency.rateMultiplier)
                                        expandedCoinMenu = false
                                        keyboardController?.hide()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountInvested,
                        onValueChange = { amountInvested = it },
                        label = { Text("${stringResource(id = R.string.amount_invested)} (${currency.symbol})", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor, unfocusedTextColor = textColor,
                            focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = currentPrice,
                            onValueChange = { currentPrice = it },
                            label = { Text("Buy Price (${currency.symbol})", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor, unfocusedTextColor = textColor,
                                focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = priceIncrease,
                            onValueChange = { priceIncrease = it },
                            label = { Text("Target Rise (${currency.symbol})", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor, unfocusedTextColor = textColor,
                                focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(id = R.string.include_brokerage), color = textColor, fontSize = 15.sp)
                        Switch(
                            checked = includeBrokerage,
                            onCheckedChange = { includeBrokerage = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor, checkedTrackColor = Color.DarkGray)
                        )
                    }

                    if (includeBrokerage) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = brokeragePercent,
                            onValueChange = { brokeragePercent = it },
                            label = { Text(stringResource(id = R.string.brokerage_percent), color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor, unfocusedTextColor = textColor,
                                focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- CALCULATE BUTTON ---
            Button(
                onClick = {
                    keyboardController?.hide()
                    resultError = null
                    val amount = amountInvested.toDoubleOrNull()
                    val price = currentPrice.toDoubleOrNull()
                    val targetRise = priceIncrease.toDoubleOrNull()
                    val brokerageRate = if (includeBrokerage) brokeragePercent.toDoubleOrNull() ?: 0.0 else 0.0

                    if (amount == null || price == null || targetRise == null) {
                        resultError = "Please fill all fields with valid numbers."
                    } else if (amount <= 0 || price <= 0) {
                        resultError = "Amounts must be greater than zero."
                    } else {
                        val unitsOwned = amount / price
                        val futureValue = unitsOwned * targetRise
                        val feeCost = (amount + futureValue) * (brokerageRate / 100)
                        val netProfit = futureValue - amount - feeCost
                        val profitPercent = (netProfit / amount) * 100

                        profitAmount = netProfit
                        profitPercentage = profitPercent
                        finalValResult = futureValue
                        initialInvResult = amount
                        totalFeesPaid = feeCost

                        calculatorViewModel.saveCalculation(
                            CalculationHistory(
                                coinName = if (isNoCoinMode || searchQuery.isBlank()) "Custom Trade" else searchQuery,
                                amountInvested = amount,
                                currentPrice = price,
                                targetPrice = targetRise,
                                futureValue = futureValue,
                                profit = netProfit,
                                profitPercent = profitPercent
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = secondary, contentColor = bgColor)
            ) {
                Text(stringResource(id = R.string.calculate), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            resultError?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = errorColor, fontSize = 14.sp)
            }

            // --- ENGAGING RESULT CARD + DETAILED BREAKDOWN & ANIMATED GRAPH ---
            if (profitAmount != null && profitPercentage != null && finalValResult != null && initialInvResult != null && totalFeesPaid != null) {
                val isProfit = profitAmount!! >= 0
                val displayColor = if (isProfit) NeonGreen else errorColor
                val sign = if (isProfit) "+" else ""

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(id = R.string.net_profit_loss).uppercase(), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "$sign${currency.symbol}${String.format("%.2f", profitAmount)}",
                                    color = displayColor,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(displayColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$sign${String.format("%.2f", profitPercentage)}%",
                                        color = displayColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            AnimatedMiniTrendGraph(isProfit = isProfit, lineColor = displayColor)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Detailed breakdown rows
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Initial investment:", color = Color.Gray, fontSize = 13.sp)
                            Text("${currency.symbol}${String.format("%.2f", initialInvResult)}", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Final value:", color = Color.Gray, fontSize = 13.sp)
                            Text("${currency.symbol}${String.format("%.2f", finalValResult)}", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(id = R.string.total_fees_paid), color = Color.Gray, fontSize = 13.sp)
                            Text("${currency.symbol}${String.format("%.2f", totalFeesPaid)}", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- HISTORY SECTION WITH LIGHTNING-FAST INSTANT DELETE ---
            if (calculatorViewModel.historyList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(id = R.string.history),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                calculatorViewModel.historyList.toList().forEach { entry ->
                    val isProfit = entry.profit >= 0
                    val displayColor = if (isProfit) NeonGreen else errorColor
                    val sign = if (isProfit) "+" else ""

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.coinName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Inv: ${currency.symbol}${String.format("%.2f", entry.amountInvested * currency.rateMultiplier)}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }

                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = "$sign${currency.symbol}${String.format("%.2f", entry.profit * currency.rateMultiplier)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = displayColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$sign${String.format("%.2f", entry.profitPercent)}%",
                                    fontSize = 13.sp,
                                    color = displayColor
                                )
                            }

                            IconButton(
                                onClick = { calculatorViewModel.removeHistoryItem(entry) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Gray.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/// --- ANIMATED MINI TREND GRAPH COMPOSABLE ---
@Composable
fun AnimatedMiniTrendGraph(isProfit: Boolean, lineColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "graphAnimation")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    Canvas(modifier = Modifier.width(90.dp).height(45.dp)) {
        val width = size.width
        val height = size.height

        val startY = if (isProfit) height * 0.8f else height * 0.2f
        val midY = height * 0.5f
        val endY = if (isProfit) height * 0.15f else height * 0.85f

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, startY)
            lineTo(width * 0.4f, midY)
            lineTo(width * progress, endY * progress + startY * (1 - progress))
        }

        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )
    }
}