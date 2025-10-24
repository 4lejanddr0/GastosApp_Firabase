@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tuempresa.gastos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val snack = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atrás") } }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") },
                singleLine = true, visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onRegister(email, pass, name) },
                enabled = !loading && name.isNotBlank() && email.isNotBlank() && pass.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Registrarme") }

            LaunchedEffect(error) {
                if (!error.isNullOrBlank()) snack.showSnackbar(error)
            }
        }
    }
}
