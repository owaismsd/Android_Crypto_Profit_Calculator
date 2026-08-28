package com.owais.cryptoprofitcalculator

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var currentUserEmail by mutableStateOf<String?>(null)
        private set

    var isEmailVerified by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var showReauthDialog by mutableStateOf(false)
        private set

    init {
        updateUserInfo()
    }

    private fun updateUserInfo() {
        val user = auth.currentUser
        currentUserEmail = user?.email
        isEmailVerified = user?.isEmailVerified ?: false
    }

    fun dismissReauthDialog() {
        showReauthDialog = false
    }

    fun getGoogleSignInClient(activity: Activity, webClientId: String) =
        GoogleSignIn.getClient(
            activity,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        )

    fun signUpWithEmail(email: String, password: String) {
        if (!validateEmail(email)) {
            errorMessage = "Please enter a valid email address."
            return
        }
        if (!validatePassword(password)) {
            errorMessage = "Password must be at least 8 characters long and contain both letters and numbers."
            return
        }
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                sendVerificationEmail()
                updateUserInfo()
            } catch (e: Exception) {
                errorMessage = getFriendlyErrorMessage(e)
            } finally {
                isLoading = false
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (!validateEmail(email)) {
            errorMessage = "Please enter a valid email address."
            return
        }
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                updateUserInfo()
            } catch (e: Exception) {
                errorMessage = getFriendlyErrorMessage(e)
            } finally {
                isLoading = false
            }
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                updateUserInfo()
            } catch (e: Exception) {
                errorMessage = getFriendlyErrorMessage(e)
            } finally {
                isLoading = false
            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            try {
                auth.currentUser?.sendEmailVerification()?.await()
            } catch (e: Exception) {
                errorMessage = getFriendlyErrorMessage(e)
            }
        }
    }

    fun reloadUser() {
        viewModelScope.launch {
            try {
                auth.currentUser?.reload()?.await()
                updateUserInfo()
            } catch (e: Exception) {
                errorMessage = getFriendlyErrorMessage(e)
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validatePassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$".toRegex()
        return password.matches(passwordRegex)
    }

    fun signOut() {
        auth.signOut()
        updateUserInfo()
    }

    fun deleteAccount(password: String? = null, onClearData: () -> Unit, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false, "No active user found.")
            return
        }

        val isGoogleUser = user.providerData.any { it.providerId == "google.com" }
        if (isGoogleUser) {
            auth.signOut()
            currentUserEmail = null
            onComplete(false, "For your security, Google requires a fresh login. Please sign in with Google again, then delete your account.")
            return
        }

        if (!password.isNullOrBlank()) {
            val email = user.email ?: ""
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    performAccountDeletion(user, onClearData, onComplete)
                } else {
                    onComplete(false, getFriendlyErrorMessage(reauthTask.exception))
                }
            }
        } else {
            performAccountDeletion(user, onClearData, onComplete)
        }
    }

    private fun performAccountDeletion(user: FirebaseUser, onClearData: () -> Unit, onComplete: (Boolean, String?) -> Unit) {
        user.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onClearData()
                currentUserEmail = null
                onComplete(true, "Account successfully deleted.")
            } else {
                val errorMsg = task.exception?.message ?: ""
                if (errorMsg.contains("requires-recent-login", ignoreCase = true)) {
                    showReauthDialog = true
                    onComplete(false, "REQUIRES_RECENT_LOGIN")
                } else {
                    onComplete(false, getFriendlyErrorMessage(task.exception))
                }
            }
        }
    }

    // --- FRIENDLY ERROR TRANSLATOR ---
    private fun getFriendlyErrorMessage(exception: Exception?): String {
        val message = exception?.localizedMessage ?: exception?.message ?: ""
        return when {
            message.contains("credential is incorrect", ignoreCase = true) ||
                    message.contains("malformed or has expired", ignoreCase = true) ||
                    message.contains("INVALID_CREDENTIAL", ignoreCase = true) ->
                "Incorrect password or session expired. Please check your details and try again."

            message.contains("no user record", ignoreCase = true) ||
                    message.contains("USER_NOT_FOUND", ignoreCase = true) ->
                "No account found with this email address."

            message.contains("already in use", ignoreCase = true) ||
                    message.contains("EMAIL_EXISTS", ignoreCase = true) ->
                "An account with this email already exists. Try signing in instead."

            message.contains("password is invalid", ignoreCase = true) ||
                    message.contains("WEAK_PASSWORD", ignoreCase = true) ->
                "Password should be at least 6 characters long."

            message.contains("network error", ignoreCase = true) ||
                    message.contains("A network error", ignoreCase = true) ->
                "Please check your internet connection and try again."

            else -> "Something went wrong. Please try again."
        }
    }
}