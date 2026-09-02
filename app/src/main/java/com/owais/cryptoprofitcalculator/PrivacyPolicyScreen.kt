package com.owais.cryptoprofitcalculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        },
        containerColor = bgColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Privacy Policy for Crypto Profit Calculator",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "Last updated: August 2026",
                    fontSize = 14.sp,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Crypto Profit Calculator (\"we,\" \"our,\" or \"the app\") is committed to protecting your privacy. This Privacy Policy explains what information we collect, how we use it, and your rights regarding your data.",
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SectionHeader("Information We Collect")
                Text(
                    text = "Account Information: When you sign up, we collect your email address (for email/password accounts) or basic profile information provided by Google (for Google Sign-In), via Firebase Authentication.\n" +
                            "Calculation History: We store the crypto profit calculations you perform (coin name, amounts, prices, and results) linked to your account, so you can view your past calculations.\n" +
                            "Usage Data: We may collect anonymous usage and diagnostic data through Firebase Analytics to help us improve the app.",
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SectionHeader("How We Use Your Information")
                Text(
                    text = "- To provide and maintain your account and calculation history\n" +
                            "- To improve app performance and features\n" +
                            "- To display advertisements through third-party ad networks",
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SectionHeader("Third-Party Services")
                Text(
                    text = "This app uses the following third-party services, which have their own privacy policies:\n" +
                            "- Firebase (Google) — authentication and data storage. See Google's Privacy Policy at https://policies.google.com/privacy\n" +
                            "- CoinGecko API — for live cryptocurrency price data. No personal data is sent to CoinGecko.\n" +
                            "- Google AdMob (if/when ads are integrated) — for displaying advertisements. See Google's Privacy Policy above.",
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SectionHeader("Data Storage and Security")
                Text(
                    text = "Your data is stored securely using Firebase's infrastructure with security rules ensuring only you can access your own account data. We do not sell your personal information to third parties.",
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SectionHeader("Your Rights")
                Text(
                    text = "You can delete your account and all associated data at any time using the \"Delete Account\" option in the app's Settings screen. This permanently removes your account and calculation history from our systems.",
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SectionHeader("Children's Privacy")
                Text(
                    text = "This app is not directed at children under 13, and we do not knowingly collect personal information from children under 13.",
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SectionHeader("Changes to This Policy")
                Text(
                    text = "We may update this Privacy Policy from time to time. Changes will be posted on this page with an updated revision date.",
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SectionHeader("Contact Us")
                Text(
                    text = "If you have questions about this Privacy Policy, please contact us at: owaisahmad.maseed@gmail.com",
                    color = textColor,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
