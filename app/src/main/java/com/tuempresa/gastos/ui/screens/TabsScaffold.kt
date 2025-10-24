@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tuempresa.gastos.ui.screens

import androidx.compose.foundation.layout.padding // <-- IMPORT
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun TabsScaffold(
    onLogout: () -> Unit,
    addTab: @Composable () -> Unit,
    historyTab: @Composable () -> Unit
) {
    var idx by remember { mutableStateOf(0) }
    val items = listOf("Agregar", "Historial")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gastos personales") },
                actions = { TextButton(onClick = onLogout) { Text("Salir") } }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = idx == i,
                        onClick = { idx = i },
                        icon = {},
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { inner ->
        Surface(Modifier.padding(inner)) {
            if (idx == 0) addTab() else historyTab() // estos son @Composable
        }
    }
}
