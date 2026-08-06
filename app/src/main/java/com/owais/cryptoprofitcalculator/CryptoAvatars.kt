package com.owais.cryptoprofitcalculator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class AvatarItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

object CryptoAvatars {
    val list = listOf(
        AvatarItem(
            id = "crypto_bull",
            title = "Bullish Node",
            icon = Icons.Default.TrendingUp,
            gradientColors = listOf(Color(0xFF00E676), Color(0xFF1565C0)) // Neon Green to Deep Blue
        ),
        AvatarItem(
            id = "crypto_vault",
            title = "Quantum Vault",
            icon = Icons.Default.Lock,
            gradientColors = listOf(Color(0xFFFFD700), Color(0xFFFF6D00)) // Gold to Neon Orange
        ),
        AvatarItem(
            id = "crypto_cyber",
            title = "Cyber Trader",
            icon = Icons.Default.Psychology,
            gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF)) // Cyan to Neon Purple
        ),
        AvatarItem(
            id = "crypto_diamond",
            title = "Diamond AI",
            icon = Icons.Default.Diamond,
            gradientColors = listOf(Color(0xFFE040FB), Color(0xFF00B0FF)) // Magenta to Bright Cyan
        ),
        AvatarItem(
            id = "crypto_rocket",
            title = "Alpha Moon",
            icon = Icons.Default.RocketLaunch,
            gradientColors = listOf(Color(0xFFFF1744), Color(0xFFFFEA00)) // Crimson to Yellow
        ),
        AvatarItem(
            id = "crypto_shield",
            title = "Secure Node",
            icon = Icons.Default.Security,
            gradientColors = listOf(Color(0xFF2979FF), Color(0xFF00E676)) // Blue to Green
        )
    )

    fun getAvatar(id: String): AvatarItem {
        return list.find { it.id == id } ?: list[0]
    }
}