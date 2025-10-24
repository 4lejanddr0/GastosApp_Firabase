@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tuempresa.gastos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tuempresa.gastos.data.model.Category as ExpenseCategory
import com.tuempresa.gastos.data.model.Expense
import com.tuempresa.gastos.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    expenseVm: ExpenseViewModel,
    categories: List<ExpenseCategory>,
    onLogout: (() -> Unit)? = null
) {
    val ui by expenseVm.state.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    // ---- Estado del formulario ----
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory: ExpenseCategory? by remember { mutableStateOf(null) }
    var note by remember { mutableStateOf("") }
    var pickedDate by remember { mutableStateOf(Date()) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val canSave = name.isNotBlank() && amount > 0.0 && selectedCategory != null

    // Mostrar errores del VM
    LaunchedEffect(ui.error) {
        val e = ui.error
        if (!e.isNullOrBlank()) {
            snack.showSnackbar(e)
        }
    }

    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gastos personales") },
                actions = {
                    if (onLogout != null) {
                        TextButton(onClick = onLogout) { Text("Salir") }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snack) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del gasto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Monto
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Categoría (Dropdown seguro con Category?) ---
            var catExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = catExpanded,
                onExpandedChange = { catExpanded = !catExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Selecciona categoría",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = "Categoría"
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = catExpanded,
                    onDismissRequest = { catExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategory = cat
                                catExpanded = false
                            }
                        )
                    }
                }
            }

            // Fecha (solo visual; reemplaza por tu DatePicker si lo tenías)
            OutlinedTextField(
                value = dateFmt.format(pickedDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha") },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            // Nota
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Botón Guardar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    enabled = canSave && !ui.loading,
                    onClick = {
                        val cat = selectedCategory
                        if (cat == null) {
                            scope.launch { snack.showSnackbar("Selecciona una categoría") }
                            return@Button
                        }
                        expenseVm.addExpense(
                            name = name,
                            amount = amount,
                            cat = cat,                 // <- ya no nulo
                            date = pickedDate,
                            note = note.takeIf { it.isNotBlank() }
                        )
                        // Limpieza visual
                        name = ""
                        amountText = ""
                        note = ""
                        selectedCategory = null
                        Toast.makeText(ctx, "Gasto guardado", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Guardar")
                }
            }

            Divider()

            // Total
            Text(
                text = "Total del mes: ${"%.2f".format(ui.total)}",
                style = MaterialTheme.typography.titleMedium
            )

            // Lista / Historial
            if (ui.loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = true)
                ) {
                    items(ui.items, key = { it.id }) { exp ->
                        ExpenseRow(
                            expense = exp,
                            onDelete = { expenseVm.delete(exp.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    onDelete: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.name, style = MaterialTheme.typography.titleMedium)
                // Uso SEGURO de category? (puede venir nulo)
                Text(
                    text = expense.category?.name ?: "Sin categoría",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateFmt.format(expense.date.toDate()),
                    style = MaterialTheme.typography.bodySmall
                )
                if (!expense.note.isNullOrBlank()) {
                    Text(expense.note!!, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${"%.2f".format(expense.amount)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDelete) { Text("Eliminar") }
            }
        }
    }
}
