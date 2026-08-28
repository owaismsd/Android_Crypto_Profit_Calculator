package com.owais.cryptoprofitcalculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.owais.cryptoprofitcalculator.ui.theme.RedLoss

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    calculatorViewModel: CalculatorViewModel
) {
    var passwordInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    val isDark = ThemeState.isDarkTheme ?: isSystemInDarkTheme()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    val activeAvatar = CryptoAvatars.getAvatar(AvatarPrefs.currentAvatarId)

    if (showPrivacyPolicy) {
        PrivacyPolicyScreen(onBack = { showPrivacyPolicy = false })
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
            text = stringResource(id = R.string.account),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- CLICKABLE DYNAMIC CRYPTO AVATAR ---
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(colors = activeAvatar.gradientColors)
                )
                .clickable { showAvatarDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activeAvatar.icon,
                contentDescription = "Profile Avatar",
                modifier = Modifier.size(50.dp),
                tint = Color.White
            )
            // Edit hint badge overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Change",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = authViewModel.currentUserEmail ?: stringResource(id = R.string.not_logged_in),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = stringResource(id = R.string.dark_mode),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isDark,
                onCheckedChange = { ThemeState.saveTheme(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = primaryColor,
                    checkedTrackColor = surfaceColor,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = surfaceColor
                )
            )
        }

        SettingsRow(
            icon = Icons.Default.Translate,
            title = stringResource(id = R.string.language_setting),
            textColor = textColor,
            onClick = { showLanguageDialog = true }
        )

        SettingsRow(
            icon = Icons.Default.Delete,
            title = stringResource(id = R.string.reset_history),
            textColor = textColor,
            onClick = {
                calculatorViewModel.resetHistory()
                android.widget.Toast.makeText(
                    context,
                    "Calculation history successfully reset.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )

        SettingsRow(
            icon = Icons.Default.Info,
            title = stringResource(id = R.string.privacy_policy),
            textColor = textColor,
            onClick = { showPrivacyPolicy = true }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = surfaceColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        SettingsRow(
            icon = Icons.Default.ExitToApp,
            title = stringResource(id = R.string.sign_out),
            textColor = textColor,
            isDestructive = true,
            onClick = { authViewModel.signOut() }
        )

        SettingsRow(
            icon = Icons.Default.Warning,
            title = stringResource(id = R.string.delete_account),
            textColor = textColor,
            isDestructive = true,
            onClick = {
                authViewModel.deleteAccount(onClearData = { calculatorViewModel.resetHistory() }) { success, message ->
                    if (success) {
                        android.widget.Toast.makeText(
                            context,
                            message ?: "Account successfully deleted.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } else if (message != "REQUIRES_RECENT_LOGIN") {
                        android.widget.Toast.makeText(
                            context,
                            message ?: "Error deleting account.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }

    // --- CRYPTO IDENTITY SELECTOR DIALOG ---
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            containerColor = surfaceColor,
            title = {
                Text(
                    text = "Select Crypto Identity",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose your high-performance trading badge:",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val rows = CryptoAvatars.list.chunked(2)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowItems.forEach { avatar ->
                                val isSelected = avatar.id == AvatarPrefs.currentAvatarId
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            AvatarPrefs.saveAvatar(context, avatar.id)
                                            showAvatarDialog = false
                                        }
                                        .padding(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(65.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(colors = avatar.gradientColors)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = avatar.icon,
                                            contentDescription = avatar.title,
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = avatar.title,
                                        color = if (isSelected) primaryColor else textColor,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text("Close", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Password Re-authentication Dialog for Account Deletion
    if (authViewModel.showReauthDialog) {
        AlertDialog(
            onDismissRequest = { authViewModel.dismissReauthDialog() },
            containerColor = surfaceColor,
            title = { Text("Confirm Password", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("For security reasons, please enter your password to confirm account deletion.", color = textColor, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password", color = Color.Gray) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.deleteAccount(password = passwordInput, onClearData = { calculatorViewModel.resetHistory() }) { success, message ->
                            if (success) {
                                authViewModel.dismissReauthDialog()
                                passwordInput = ""
                                android.widget.Toast.makeText(
                                    context,
                                    message ?: "Account successfully deleted.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } else if (message != "REQUIRES_RECENT_LOGIN") {
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Confirm & Delete", color = errorColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { authViewModel.dismissReauthDialog() }) {
                    Text("Cancel", color = textColor)
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = surfaceColor,
            title = {
                Text(
                    text = "Select Language",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "English",
                        color = textColor,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                changeLanguage(context, "en")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    Text(
                        text = "العربية",
                        color = textColor,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                changeLanguage(context, "ar")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    Text(
                        text = "اردو",
                        color = textColor,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                changeLanguage(context, "ur")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    Text(
                        text = "हिन्दी",
                        color = textColor,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                changeLanguage(context, "hi")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    Text(
                        text = "中文",
                        color = textColor,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                changeLanguage(context, "zh")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel", color = primaryColor)
                }
            }
        )
    }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    textColor: Color,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isDestructive) RedLoss else textColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = if (isDestructive) RedLoss else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun changeLanguage(context: android.content.Context, languageTag: String) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
        localeManager.applicationLocales = android.os.LocaleList.forLanguageTags(languageTag)
    } else {
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.forLanguageTags(languageTag)
        )
    }
}