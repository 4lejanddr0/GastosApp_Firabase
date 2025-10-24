package com.tuempresa.gastos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.tuempresa.gastos.data.repo.AuthRepository
import com.tuempresa.gastos.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {

    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val nav = rememberNavController()
                val startDest = if (authRepo.isLoggedIn()) "home" else "login"

                NavHost(navController = nav, startDestination = startDest) {
                    composable("login") {
                        LoginScreen(
                            authRepo = authRepo,
                            onLoggedIn = {
                                nav.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoRegister = { nav.navigate("register") }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            authRepo = authRepo,
                            onRegistered = {
                                nav.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoLogin = {
                                nav.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("home") {
                        HomeScreen(
                            onLogout = {
                                authRepo.signOut()
                                nav.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(
    authRepo: AuthRepository,
    onLoggedIn: () -> Unit,
    onGoRegister: () -> Unit
) {
    val context = LocalContext.current
    val webClientId = remember { context.getString(R.string.default_web_client_id) }
    val googleClient = remember(webClientId) {
        authRepo.googleClient(context as ComponentActivity, webClientId)
    }

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Google launcher
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data: Intent? = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        loading = true
        error = null
        try {
            val account = task.getResult(Exception::class.java)
            val idToken = account.idToken ?: error("No se obtuvo idToken")
            authRepo.firebaseAuthWithGoogle(idToken) { r ->
                loading = false
                r.onSuccess { onLoggedIn() }
                    .onFailure { error = it.localizedMessage ?: "Error con Google" }
            }
        } catch (e: Exception) {
            loading = false
            error = e.localizedMessage ?: "Cancelado"
        }
    }

    AuthScaffold(
        title = "Iniciar sesión",
        primaryActionText = "Entrar",
        secondaryActionText = "Crear cuenta",
        onSecondary = onGoRegister,
        email = email,
        onEmailChange = { email = it },
        pass = pass,
        onPassChange = { pass = it },
        loading = loading,
        error = error,
        onPrimaryClick = {
            loading = true
            error = null
            authRepo.signInWithEmail(email.trim(), pass) { result ->
                loading = false
                result.onSuccess { onLoggedIn() }
                    .onFailure { error = it.localizedMessage ?: "Error al iniciar sesión" }
            }
        },
        onGoogleClick = { googleLauncher.launch(googleClient.signInIntent) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreen(
    authRepo: AuthRepository,
    onRegistered: () -> Unit,
    onGoLogin: () -> Unit
) {
    val context = LocalContext.current
    val webClientId = remember { context.getString(R.string.default_web_client_id) }
    val googleClient = remember(webClientId) {
        authRepo.googleClient(context as ComponentActivity, webClientId)
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Google launcher (sigue igual)
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data: Intent? = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        loading = true
        error = null
        try {
            val account = task.getResult(Exception::class.java)
            val idToken = account.idToken ?: error("No se obtuvo idToken")
            authRepo.firebaseAuthWithGoogle(idToken) { r ->
                loading = false
                r.onSuccess { onRegistered() }
                    .onFailure { error = it.localizedMessage ?: "Error con Google" }
            }
        } catch (e: Exception) {
            loading = false
            error = e.localizedMessage ?: "Cancelado"
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña (mín. 6)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Button(
                enabled = !loading && name.isNotBlank() && email.isNotBlank() && pass.length >= 6,
                onClick = {
                    loading = true
                    error = null
                    authRepo.registerWithEmail(email.trim(), pass, name.trim()) { result ->
                        loading = false
                        result.onSuccess { onRegistered() }
                            .onFailure { error = it.localizedMessage ?: "Error al registrarte" }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Registrarme") }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                enabled = !loading,
                onClick = { googleLauncher.launch(googleClient.signInIntent) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Registrarme con Google") }

            TextButton(onClick = onGoLogin) { Text("Ya tengo cuenta") }

            if (loading) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator()
            }
            if (!error.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthScaffold(
    title: String,
    primaryActionText: String,
    secondaryActionText: String,
    onSecondary: () -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    pass: String,
    onPassChange: (String) -> Unit,
    loading: Boolean,
    error: String?,
    onPrimaryClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = onPassChange,
                label = { Text("Contraseña (mín. 6)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Button(
                enabled = !loading,
                onClick = onPrimaryClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text(primaryActionText) }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                enabled = !loading,
                onClick = onGoogleClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Continuar con Google") }

            TextButton(onClick = onSecondary) { Text(secondaryActionText) }

            if (loading) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator()
            }
            if (!error.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
