package com.owais.cryptoprofitcalculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun VerifyEmailScreen(
    authViewModel: AuthViewModel
) {
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val highlightColor = Color(0xFFFFD531)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verify Your Email",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = buildAnnotatedString {
                append("We've sent a verification email to ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                    append(authViewModel.currentUserEmail ?: "your email")
                }
                append(".\n\n")
                withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                    append("Please ")
                }
                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = highlightColor)) {
                    append("check your Gmail or Spam folder")
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                    append(" and click the link to continue.")
                }
            },
            fontSize = 16.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (authViewModel.isLoading) {
            CircularProgressIndicator(color = primaryColor)
        } else {
            Button(
                onClick = { authViewModel.reloadUser() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(
                    text = "I've Verified",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { authViewModel.sendVerificationEmail() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
            ) {
                Text(
                    text = "Resend Verification Email",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { authViewModel.signOut() }
            ) {
                Text(
                    text = "Sign Out",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        authViewModel.errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = errorColor, fontSize = 14.sp)
        }
    }
}
