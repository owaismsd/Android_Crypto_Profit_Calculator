package com.owais.cryptoprofitcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.owais.cryptoprofitcalculator.ui.theme.CryptoProfitCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AvatarPrefs.init(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase App Check
        Firebase.initialize(context = this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        val priceCheckRequest = androidx.work.PeriodicWorkRequestBuilder<PriceCheckWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        ).build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "price_check_work",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            priceCheckRequest
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        // --- NEW LINE ADDED HERE ---
        // Load the saved theme from storage before drawing the UI
        ThemeState.init(this)

        setContent {
            CryptoProfitCalculatorTheme(
                darkTheme = ThemeState.isDarkTheme ?: isSystemInDarkTheme()
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val authViewModel: AuthViewModel = viewModel()

                    if (authViewModel.currentUserEmail != null) {
                        if (authViewModel.isEmailVerified) {
                            val calculatorViewModel: CalculatorViewModel = viewModel()
                            var selectedTab by remember { mutableStateOf(0) }

                            Scaffold(
                                bottomBar = {
                                    NavigationBar {
                                        NavigationBarItem(
                                            selected = selectedTab == 0,
                                            onClick = { selectedTab = 0 },
                                            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                            label = { Text("Home") }
                                        )
                                        NavigationBarItem(
                                            selected = selectedTab == 1,
                                            onClick = { selectedTab = 1 },
                                            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                                            label = { Text("Settings") }
                                        )
                                    }
                                }
                            ) { tabPadding ->
                                Box(modifier = Modifier.padding(tabPadding)) {
                                    if (selectedTab == 0) {
                                        CalculatorScreen(calculatorViewModel = calculatorViewModel)
                                    } else {
                                        SettingsScreen(
                                            authViewModel = authViewModel,
                                            calculatorViewModel = calculatorViewModel
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                VerifyEmailScreen(authViewModel = authViewModel)
                            }
                        }
                    } else {
                        Box(modifier = Modifier.padding(innerPadding)) {
                            LoginScreen(
                                authViewModel = authViewModel,
                                webClientId = BuildConfig.WEB_CLIENT_ID
                            )
                        }
                    }
                }
            }
        }
    }
}