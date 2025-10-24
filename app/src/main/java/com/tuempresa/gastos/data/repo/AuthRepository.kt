package com.tuempresa.gastos.data.repo

import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun isLoggedIn(): Boolean = auth.currentUser != null
    fun currentUid(): String? = auth.currentUser?.uid
    fun signOut() { auth.signOut() }

    fun googleClient(activity: ComponentActivity, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    // ---------- helper: upsert del doc de usuario ----------
    private suspend fun upsertUserDoc(provider: String) {
        val u = auth.currentUser ?: return
        val doc = mapOf(
            "uid" to u.uid,
            "displayName" to (u.displayName ?: ""),
            "email" to (u.email ?: ""),
            "provider" to provider,
            "updatedAt" to Timestamp.now()
        )
        db.collection("users").document(u.uid).set(doc, SetOptions.merge()).await()
    }

    // ---------- Google ----------
    suspend fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        upsertUserDoc("google.com")
    }

    fun firebaseAuthWithGoogle(idToken: String, cb: (Result<Unit>) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val u = auth.currentUser
                    if (u != null) {
                        val doc = mapOf(
                            "uid" to u.uid,
                            "displayName" to (u.displayName ?: ""),
                            "email" to (u.email ?: ""),
                            "provider" to "google.com",
                            "updatedAt" to Timestamp.now()
                        )
                        db.collection("users").document(u.uid).set(doc, SetOptions.merge())
                    }
                    cb(Result.success(Unit))
                } else cb(Result.failure(task.exception ?: Exception("Error con Google Sign-In")))
            }
    }

    // ---------- Email / Password ----------
    suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
        upsertUserDoc("password")
    }

    fun signInWithEmail(email: String, password: String, cb: (Result<Unit>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val u = auth.currentUser
                    if (u != null) {
                        val doc = mapOf(
                            "uid" to u.uid,
                            "displayName" to (u.displayName ?: ""),
                            "email" to (u.email ?: ""),
                            "provider" to "password",
                            "updatedAt" to Timestamp.now()
                        )
                        db.collection("users").document(u.uid).set(doc, SetOptions.merge())
                    }
                    cb(Result.success(Unit))
                } else cb(Result.failure(task.exception ?: Exception("Error al iniciar sesión")))
            }
    }

    suspend fun registerWithEmail(email: String, password: String, displayName: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
        val user = auth.currentUser ?: throw IllegalStateException("Usuario nulo después de registro")
        val profile = userProfileChangeRequest { this.displayName = displayName }
        user.updateProfile(profile).await()
        upsertUserDoc("password")
        val doc = mapOf("createdAt" to Timestamp.now())
        db.collection("users").document(user.uid).set(doc, SetOptions.merge()).await()
    }

    fun registerWithEmail(email: String, password: String, displayName: String, cb: (Result<Unit>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) { cb(Result.failure(task.exception ?: Exception("Error al registrarte"))); return@addOnCompleteListener }
                val user = auth.currentUser ?: run {
                    cb(Result.failure(Exception("Usuario nulo después de registro"))); return@addOnCompleteListener
                }
                val profile = userProfileChangeRequest { this.displayName = displayName }
                user.updateProfile(profile)
                    .addOnCompleteListener { upd ->
                        if (!upd.isSuccessful) { cb(Result.failure(upd.exception ?: Exception("No se pudo guardar el nombre"))); return@addOnCompleteListener }
                        val base = mapOf(
                            "uid" to user.uid,
                            "displayName" to displayName,
                            "email" to (user.email ?: ""),
                            "provider" to "password",
                            "createdAt" to Timestamp.now(),
                            "updatedAt" to Timestamp.now()
                        )
                        db.collection("users").document(user.uid).set(base, SetOptions.merge())
                        cb(Result.success(Unit))
                    }
            }
    }
}
