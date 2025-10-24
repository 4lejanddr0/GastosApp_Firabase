package com.tuempresa.gastos

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.tuempresa.gastos.data.model.Category
import com.tuempresa.gastos.ui.screens.AddExpenseScreen
import com.tuempresa.gastos.ui.screens.HistoryScreen
import com.tuempresa.gastos.ui.screens.LoginScreen
import com.tuempresa.gastos.ui.screens.RegisterScreen
import com.tuempresa.gastos.ui.screens.TabsScaffold
import com.tuempresa.gastos.ui.theme.GastosAppTheme
import com.tuempresa.gastos.ui.viewmodel.AuthViewModel
import com.tuempresa.gastos.ui.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) pendingGoogleTokenHandler?.invoke(idToken)
            else Toast.makeText(this, "No se obtuvo el token de Google", Toast.LENGTH_SHORT).show()
        } catch (_: ApiException) {
            Toast.makeText(this, "Google Sign-In cancelado o falló", Toast.LENGTH_SHORT).show()
        } catch (_: Throwable) {
            Toast.makeText(this, "Error al procesar Google Sign-In", Toast.LENGTH_SHORT).show()
        }
    }
    private var pendingGoogleTokenHandler: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val webClientId = runCatching { getString(R.string.default_web_client_id) }.getOrNull()
        val googleClient = webClientId?.takeIf { it.isNotBlank() }?.let {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(it).requestEmail().build()
            GoogleSignIn.getClient(this, gso)
        }

        setContent {
            GastosAppTheme {
                val nav = rememberNavController()
                val authVm: AuthViewModel = viewModel()
                val authState by authVm.state.collectAsStateWithLifecycle()

                val categories = runCatching { Category.entries.toList() }
                    .getOrElse { Category.values().toList() }

                pendingGoogleTokenHandler = { token -> authVm.signInWithGoogleIdToken(token) }

                val start = if (authState.isLogged) "home" else "login"

                NavHost(navController = nav, startDestination = start) {

                    // LOGIN
                    composable("login") {
                        LoginScreen(
                            error = authState.error,
                            loading = authState.loading,
                            onEmailSignIn = { e, p -> authVm.signInEmail(e, p) },
                            onEmailSignUp = { _, _ -> nav.navigate("register") }, // abre register screen
                            onGoogleClick = {
                                if (googleClient != null) googleLauncher.launch(googleClient.signInIntent)
                                else Toast.makeText(this@MainActivity, "Falta Client ID de Google", Toast.LENGTH_LONG).show()
                            }
                        )
                        if (!authState.loading && authState.error.isNullOrBlank() && authState.isLogged) {
                            nav.navigate("home") { popUpTo(0) }
                        }
                    }

                    // REGISTER
                    composable("register") {
                        RegisterScreen(
                            loading = authState.loading,
                            error = authState.error,
                            onBack = { nav.popBackStack() },
                            onRegister = { email, pass, displayName ->
                                authVm.signUpEmail(email, pass) // si quieres, crea otro método signUp(displayName)
                            }
                        )
                        if (!authState.loading && authState.error.isNullOrBlank() && authState.isLogged) {
                            nav.navigate("home") { popUpTo(0) }
                        }
                    }

                    // HOME TABS (Agregar + Historial)
                    composable("home") {
                        val expenseVm: ExpenseViewModel = viewModel()
                        TabsScaffold(
                            onLogout = {
                                authVm.signOut()
                                googleClient?.signOut()
                                Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                                nav.navigate("login") { popUpTo(0) }
                            },
                            addTab = {
                                AddExpenseScreen(
                                    expenseVm = expenseVm,
                                    categories = categories
                                )
                            },
                            historyTab = {
                                HistoryScreen(
                                    expenseVm = expenseVm
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
