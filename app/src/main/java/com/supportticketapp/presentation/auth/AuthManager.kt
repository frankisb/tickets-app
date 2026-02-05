package com.supportticketapp.presentation.auth

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.supportticketapp.presentation.UserPreferences
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

enum class UserRole { NONE, SUPPORT, CUSTOMER }

object AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Correos de soporte hardcodeados (puedes mover esto a Firestore o custom claims después)
    private val SUPPORT_EMAILS = setOf("soporte@demo.com")

    fun getCurrentRole(context: Context): UserRole {
        val pref = UserPreferences.getCurrentUserRole(context)
        if (pref != UserRole.NONE) return pref
        // Si no hay rol guardado pero existe usuario Firebase, determinar rol por email o proveedor
        val user = auth.currentUser
        if (user != null) {
            return if (SUPPORT_EMAILS.contains(user.email)) UserRole.SUPPORT else UserRole.CUSTOMER
        }
        return UserRole.NONE
    }

    fun setCurrentRole(context: Context, role: UserRole) {
        UserPreferences.setCurrentUserRole(context, role)
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun logout(context: Context) {
        auth.signOut()
        UserPreferences.setCurrentUserRole(context, UserRole.NONE)
    }

    // Login de soporte via Firebase Auth (email/contraseña)
    suspend fun loginSupport(email: String, password: String): Boolean =
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.d("AuthManager", "Login Firebase soporte exitoso para $email")
                    cont.resume(true)
                }
                .addOnFailureListener { e ->
                    Log.e("AuthManager", "Error login Firebase soporte para $email", e)
                    cont.resume(false)
                }
        }

    // Login cliente con Google
    fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}


