package com.example.data.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean = false,
    val authProvider: String = "Firebase Auth"
)

object AuthManager {
    private const val TAG = "AuthManager"
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_UID = "user_uid"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PHOTO = "user_photo"
    private const val KEY_LOGGED_IN = "is_logged_in"

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    fun init(context: Context) {
        val firebaseAuth = getFirebaseAuth(context)
        if (firebaseAuth != null && firebaseAuth.currentUser != null) {
            val fUser = firebaseAuth.currentUser!!
            _currentUser.value = UserProfile(
                uid = fUser.uid,
                displayName = fUser.displayName ?: fUser.email?.substringBefore("@"),
                email = fUser.email,
                photoUrl = fUser.photoUrl?.toString(),
                isAnonymous = fUser.isAnonymous,
                authProvider = if (fUser.isAnonymous) "Guest" else "Google / Firebase"
            )
        } else {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (prefs.getBoolean(KEY_LOGGED_IN, false)) {
                _currentUser.value = UserProfile(
                    uid = prefs.getString(KEY_UID, "user_local_123") ?: "user_local_123",
                    displayName = prefs.getString(KEY_NAME, "User"),
                    email = prefs.getString(KEY_EMAIL, "user@example.com"),
                    photoUrl = prefs.getString(KEY_PHOTO, null),
                    authProvider = "Google / Firebase"
                )
            }
        }
    }

    fun getFirebaseAuth(context: Context): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                null
            } else {
                FirebaseAuth.getInstance()
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth unavailable: ${e.message}")
            null
        }
    }

    fun setUserSession(context: Context, profile: UserProfile) {
        _currentUser.value = profile
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_UID, profile.uid)
            .putString(KEY_NAME, profile.displayName)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_PHOTO, profile.photoUrl)
            .apply()
    }

    fun signOut(context: Context) {
        try {
            getFirebaseAuth(context)?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Error during signOut: ${e.message}")
        }
        _currentUser.value = null
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
