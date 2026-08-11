package com.example.ui.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.data.auth.AuthManager
import com.example.data.auth.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSkipOrGuest: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun performGoogleSignIn() {
        scope.launch {
            isLoading = true
            errorMessage = null
            val firebaseAuth = AuthManager.getFirebaseAuth(context)

            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("90784e08-9737-4e08.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(request = request, context = context)
                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    if (firebaseAuth != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                        firebaseAuth.signInWithCredential(firebaseCredential)
                            .addOnSuccessListener { authResult ->
                                isLoading = false
                                val fUser = authResult.user
                                val profile = UserProfile(
                                    uid = fUser?.uid ?: credential.id,
                                    displayName = fUser?.displayName ?: credential.displayName ?: credential.id,
                                    email = fUser?.email ?: credential.id,
                                    photoUrl = fUser?.photoUrl?.toString() ?: credential.profilePictureUri?.toString(),
                                    authProvider = "Google Account"
                                )
                                AuthManager.setUserSession(context, profile)
                                Toast.makeText(context, "Signed in as ${profile.displayName}!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = "Firebase Auth error: ${e.localizedMessage}"
                            }
                    } else {
                        isLoading = false
                        val profile = UserProfile(
                            uid = credential.id,
                            displayName = credential.displayName ?: credential.id.substringBefore("@"),
                            email = credential.id,
                            photoUrl = credential.profilePictureUri?.toString(),
                            authProvider = "Google Account"
                        )
                        AuthManager.setUserSession(context, profile)
                        Toast.makeText(context, "Signed in as ${profile.displayName}!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    }
                } else {
                    isLoading = false
                    errorMessage = "Google Credential not recognized"
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.localizedMessage ?: "Google Sign-In canceled or failed"
            }
        }
    }

    fun performEmailSignIn() {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both email and password"
            return
        }
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            errorMessage = "Please enter a valid email address"
            return
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }

        isLoading = true
        errorMessage = null
        val firebaseAuth = AuthManager.getFirebaseAuth(context)

        if (firebaseAuth != null) {
            firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password)
                .addOnSuccessListener { authResult ->
                    isLoading = false
                    val fUser = authResult.user
                    val profile = UserProfile(
                        uid = fUser?.uid ?: "user_${trimmedEmail.hashCode()}",
                        displayName = fUser?.displayName ?: trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = trimmedEmail,
                        photoUrl = fUser?.photoUrl?.toString(),
                        authProvider = "Firebase Email/Password"
                    )
                    AuthManager.setUserSession(context, profile)
                    Toast.makeText(context, "Welcome back, ${profile.displayName}!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                }
                .addOnFailureListener {
                    firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, password)
                        .addOnSuccessListener { createResult ->
                            isLoading = false
                            val fUser = createResult.user
                            val profile = UserProfile(
                                uid = fUser?.uid ?: "user_${trimmedEmail.hashCode()}",
                                displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                                email = trimmedEmail,
                                photoUrl = null,
                                authProvider = "Firebase Email/Password"
                            )
                            AuthManager.setUserSession(context, profile)
                            Toast.makeText(context, "Account created & signed in!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        }
                        .addOnFailureListener { err ->
                            isLoading = false
                            errorMessage = err.localizedMessage ?: "Authentication failed"
                        }
                }
        } else {
            isLoading = false
            val profile = UserProfile(
                uid = "user_${Math.abs(trimmedEmail.hashCode())}",
                displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = trimmedEmail,
                photoUrl = null,
                authProvider = "Email / Password"
            )
            AuthManager.setUserSession(context, profile)
            Toast.makeText(context, "Signed in with email!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    fun performGuestSignIn() {
        val guestProfile = UserProfile(
            uid = "guest_${System.currentTimeMillis()}",
            displayName = "Guest User",
            email = "guest@auroramusic.app",
            photoUrl = null,
            isAnonymous = true,
            authProvider = "Guest Mode"
        )
        AuthManager.setUserSession(context, guestProfile)
        onSkipOrGuest()
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("login_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        modifier = modifier.testTag("login_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Logo Badge
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .border(2.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = "App Logo",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Welcome to Music Stream",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Sign in with Google or Email to sync your favorite tracks, playlists & audio preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Error Banner
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Primary Google Sign-In Button
            Button(
                onClick = { performGoogleSignIn() },
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("btn_google_sign_in")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "Sign in with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Divider Or
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "OR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Email, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input")
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input")
            )

            // Email/Password Action Button
            OutlinedButton(
                onClick = { performEmailSignIn() },
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_email_sign_in")
            ) {
                Text(
                    text = "Continue with Email",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Guest / Skip Option
            TextButton(
                onClick = { performGuestSignIn() },
                modifier = Modifier.testTag("btn_guest_sign_in")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Continue as Guest",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
