package com.owais.cryptoprofitcalculator.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors (Login Screen reference: 1.jpeg)
// 60% Background, 30% Surface/Cards, 10% Accent
val LightBackground = Color(0xFFF5F5F7) // 60%
val LightSurface = Color(0xFFFFFFFF)    // 30%
val PrimaryYellow = Color(0xFFFFB703)   // 10% Accent
val TextDark = Color(0xFF1E1E1E)

// Dark Theme Colors (Calculator reference: image_dd5c40.png)
// 60% Background, 30% Surface/Cards, 10% Accent
val DarkBackground = Color(0xFF0F0F0F)  // 60%
val DarkSurface = Color(0xFF1C1C1E)     // 30%
val NeonGreen = Color(0xFF00E676)       // 10% Accent (Profit/Positive)
val RedLoss = Color(0xFFFF3B30)         // Accent (Loss/Negative)
val TextLight = Color(0xFFF5F5F5)

// App Legacy Colors (Keeping these in case they are referenced elsewhere currently)
val AppBackground = LightBackground
val AppContainer = LightSurface
val AppYellow = PrimaryYellow
val AppGreen = NeonGreen
val AppWhite = TextLight
val AppTextSecondary = Color(0xFF9E9E9E)