package com.owais.cryptoprofitcalculator

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.owais.cryptoprofitcalculator.ui.theme.*

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    webClientId: String
) {
    val context = LocalContext.current
    val activity = context as Activity
    val keyboardController = LocalSoftwareKeyboardController.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    // Automatically hide keyboard when loading starts
    LaunchedEffect(authViewModel.isLoading) {
        if (authViewModel.isLoading) {
            keyboardController?.hide()
        }
    }

    val googleSignInClient = remember {
        authViewModel.getGoogleSignInClient(activity, webClientId)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                authViewModel.signInWithGoogleIdToken(idToken)
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Google Sign In Failed. Error Code: ${e.statusCode}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = if (isSignUpMode) stringResource(id = R.string.create_account) else stringResource(id = R.string.welcome_back),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.email), color = Color.Gray) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                unfocusedContainerColor = surfaceColor,
                focusedContainerColor = surfaceColor,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = primaryColor
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password), color = Color.Gray) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                unfocusedContainerColor = surfaceColor,
                focusedContainerColor = surfaceColor,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = primaryColor
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (isSignUpMode) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordRequirements(password = password)
        }

        Spacer(modifier = Modifier.height(12.dp))

        authViewModel.errorMessage?.let {
            Text(text = it, color = errorColor, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (authViewModel.isLoading) {
            // --- UNIQUE CRYPTO CANDLESTICK LOADING ANIMATION ---
            CryptoCandleLoadingAnimation(primaryColor = primaryColor)
        } else {
            Button(
                onClick = {
                    keyboardController?.hide()
                    if (isSignUpMode) authViewModel.signUpWithEmail(email, password)
                    else authViewModel.signInWithEmail(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = if (isSignUpMode) stringResource(id = R.string.sign_up) else stringResource(id = R.string.sign_in),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    keyboardController?.hide()
                    launcher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.continue_with_google),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isSignUpMode) stringResource(id = R.string.already_have_account) else stringResource(id = R.string.dont_have_account),
                    color = Color(0xFFFFD531),
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { isSignUpMode = !isSignUpMode }
                )
            }
        }
    }
}

// --- CUSTOM CRYPTO CANDLE ANIMATION COMPOSABLE ---
@Composable
fun CryptoCandleLoadingAnimation(primaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "candleLoader")

    val h1 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h1")
    val h2 by infiniteTransition.animateFloat(1f, 0.2f, infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h2")
    val h3 by infiniteTransition.animateFloat(0.4f, 0.9f, infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h3")
    val h4 by infiniteTransition.animateFloat(0.7f, 0.2f, infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h4")
    val h5 by infiniteTransition.animateFloat(0.2f, 0.8f, infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h5")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.height(45.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val heights = listOf(h1, h2, h3, h4, h5)
            heights.forEach { heightFactor ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(6.dp)
                        .height(40.dp * heightFactor)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(primaryColor, primaryColor.copy(alpha = 0.3f))
                            )
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Processing...",
            color = primaryColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PasswordRequirements(password: String) {
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        RequirementItem(text = "At least 8 characters", isMet = hasMinLength)
        RequirementItem(text = "Contains uppercase letter", isMet = hasUppercase)
        RequirementItem(text = "Contains a number", isMet = hasNumber)
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (isMet) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isMet) Color(0xFF4CAF50) else Color.Gray,
            fontWeight = if (isMet) FontWeight.Bold else FontWeight.Normal
        )
    }
}
