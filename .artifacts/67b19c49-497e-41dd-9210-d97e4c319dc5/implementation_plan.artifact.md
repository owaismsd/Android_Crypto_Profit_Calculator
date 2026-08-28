# Security Hardening Plan

This plan outlines the steps to harden the security of the Crypto Profit Calculator app using industry-standard practices for Firebase and Android.

## User Review Required

> [!IMPORTANT]
> **Firebase Console & Google Cloud Console Actions:** Several steps require manual configuration in the web consoles which I cannot perform directly. I will provide detailed instructions for these.
> **Play Integrity API:** App Check using Play Integrity requires the app to be published on the Google Play Store (or using internal sharing) to work fully in production.

## Proposed Changes

### 1. Firebase App Check (Play Integrity)
We will integrate Firebase App Check to ensure only your official app can access Firebase services.

#### [MODIFY] [build.gradle.kts](file:///E:/Android_Crypto_Profit_Calculator/app/build.gradle.kts)
- Add the `firebase-appcheck-playintegrity` dependency.

#### [MODIFY] [MainActivity.kt](file:///E:/Android_Crypto_Profit_Calculator/app/src/main/java/com/owais/cryptoprofitcalculator/MainActivity.kt)
- Initialize Firebase App Check in `onCreate`.

### 2. Email Verification
We will enforce email verification to ensure users are using real email addresses.

#### [MODIFY] [AuthViewModel.kt](file:///E:/Android_Crypto_Profit_Calculator/app/src/main/java/com/owais/cryptoprofitcalculator/AuthViewModel.kt)
- Add a function to send verification emails.
- Update `currentUserEmail` to include a verification status check.
- Add a `isEmailVerified` state.

#### [MODIFY] [MainActivity.kt](file:///E:/Android_Crypto_Profit_Calculator/app/src/main/java/com/owais/cryptoprofitcalculator/MainActivity.kt)
- Update the UI flow to show a "Verification Pending" state if the user is signed in but not verified.

### 3. Client-Side Input Validation
We will add robust validation for email and password fields to prevent malformed requests.

#### [MODIFY] [AuthViewModel.kt](file:///E:/Android_Crypto_Profit_Calculator/app/src/main/java/com/owais/cryptoprofitcalculator/AuthViewModel.kt)
- Implement `validateEmail(email: String)` and `validatePassword(password: String)` logic.
- Integrate these checks into `signUpWithEmail` and `signInWithEmail`.

### 4. R8 Code Obfuscation
Enable R8 to protect the app's code from reverse engineering.

#### [MODIFY] [build.gradle.kts](file:///E:/Android_Crypto_Profit_Calculator/app/build.gradle.kts)
- Set `isMinifyEnabled = true` and `isShrinkResources = true` in the release build type.

### 5. Firestore Security Rules (Documentation)
Since Firestore is not yet fully implemented in the code (it's using SharedPreferences), I will provide the optimized security rules you should paste into your Firebase Console.

## Verification Plan

### Automated Tests
- Build the app to ensure no dependency conflicts.
- I will verify the logic in `AuthViewModel` for validation and verification triggers.

### Manual Verification
- **Console Steps:** You will need to enable Play Integrity in the Firebase Console and restrict your API keys in the Google Cloud Console.
- **App Testing:** Try signing up with an invalid email or short password to verify client-side validation.
- **App Testing:** Verify that you receive an email after signup and cannot access history until verified.
