package com.example.budgetboss.data

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

data class AuthenticatedAccount(
    val remoteId: String,
    val email: String,
)

class FirebaseAuthDataSource(
    private val context: Context,
) {
    fun isConfigured(): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    suspend fun register(email: String, password: String): ActionResult<AuthenticatedAccount> {
        if (!isConfigured()) {
            return ActionResult(errorMessage = MISSING_FIREBASE_CONFIG_MESSAGE)
        }

        return runCatching {
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .await()
                .toAccount(email)
        }.fold(
            onSuccess = { ActionResult(value = it) },
            onFailure = { ActionResult(errorMessage = it.localizedMessage ?: "Could not create your account.") },
        )
    }

    suspend fun login(email: String, password: String): ActionResult<AuthenticatedAccount> {
        if (!isConfigured()) {
            return ActionResult(errorMessage = MISSING_FIREBASE_CONFIG_MESSAGE)
        }

        return runCatching {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .await()
                .toAccount(email)
        }.fold(
            onSuccess = { ActionResult(value = it) },
            onFailure = { ActionResult(errorMessage = it.localizedMessage ?: "Could not sign you in.") },
        )
    }

    fun logout() {
        if (isConfigured()) {
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun AuthResult.toAccount(fallbackEmail: String): AuthenticatedAccount {
        val firebaseUser = user ?: error("Firebase did not return an authenticated user.")
        return AuthenticatedAccount(
            remoteId = firebaseUser.uid,
            email = firebaseUser.email ?: fallbackEmail,
        )
    }

    private suspend fun <T> Task<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
            addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
            addOnCanceledListener {
                continuation.cancel()
            }
        }

    private companion object {
        const val MISSING_FIREBASE_CONFIG_MESSAGE =
            "Firebase is not configured yet. Add app/google-services.json before signing in."
    }
}

