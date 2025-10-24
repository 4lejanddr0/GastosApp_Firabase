package com.tuempresa.gastos.data.repo

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ---------- Estado ----------
    fun isLoggedIn(): Boolean = auth.currentUser != null
    fun signOut() { auth.signOut() }

    // ---------- Google Sign-In ----------
    fun googleClient(activity: androidx.activity.ComponentActivity, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun firebaseAuthWithGoogle(idToken: String, cb: (Result<Unit>) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // (Opcional) guardar/actualizar en Firestore
                    val u = auth.currentUser
                    if (u != null) {
                        val doc = mapOf(
                            "uid" to u.uid,
                            "displayName" to (u.displayName ?: ""),
                            "email" to (u.email ?: ""),
                            "provider" to "google.com",
                            "updatedAt" to Timestamp.now()
                        )
                        db.collection("users").document(u.uid).set(doc)
                    }
                    cb(Result.success(Unit))
                } else cb(Result.failure(task.exception ?: Exception("Error con Google Sign-In")))
            }
    }

    // ---------- Email / Password ----------
    fun signInWithEmail(email: String, password: String, cb: (Result<Unit>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) cb(Result.success(Unit))
                else cb(Result.failure(task.exception ?: Exception("Error al iniciar sesión")))
            }
    }

    /**
     * Registro con nombre: crea el usuario, actualiza displayName y (opcional) guarda base en Firestore.
     */
    fun registerWithEmail(email: String, password: String, displayName: String, cb: (Result<Unit>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    cb(Result.failure(task.exception ?: Exception("Error al registrarte")))
                    return@addOnCompleteListener
                }
                val user = auth.currentUser
                if (user == null) {
                    cb(Result.failure(Exception("Usuario nulo después de registro")))
                    return@addOnCompleteListener
                }
                // Actualizar nombre visible en Firebase Auth
                val profile = userProfileChangeRequest { this.displayName = displayName }
                user.updateProfile(profile)
                    .addOnCompleteListener { upd ->
                        if (!upd.isSuccessful) {
                            cb(Result.failure(upd.exception ?: Exception("No se pudo guardar el nombre")))
                            return@addOnCompleteListener
                        }
                        // (Opcional) guardar en Firestore
                        val doc = mapOf(
                            "uid" to user.uid,
                            "displayName" to displayName,
                            "email" to (user.email ?: ""),
                            "provider" to "password",
                            "createdAt" to Timestamp.now()
                        )
                        db.collection("users").document(user.uid).set(doc)
                        cb(Result.success(Unit))
                    }
            }
    }

    // Sobrecarga sin nombre (por compatibilidad con código viejo, si algo la usa)
    fun registerWithEmail(email: String, password: String, cb: (Result<Unit>) -> Unit) {
        registerWithEmail(email, password, displayName = "", cb = cb)
    }
}
