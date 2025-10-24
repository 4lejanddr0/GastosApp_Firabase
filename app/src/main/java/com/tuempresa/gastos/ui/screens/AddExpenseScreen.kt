@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tuempresa.gastos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tuempresa.gastos.data.model.Category
import com.tuempresa.gastos.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch // <-- IMPORT NECESARIO
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddExpenseScreen(
    expenseVm: ExpenseViewModel,
    categories: List<Category>
) {
    val ui by expenseVm.state.collectAsState()
    val ctx = LocalContext.current
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory: Category? by remember { mutableStateOf(null) }
    var note by remember { mutableStateOf("") }

    // DatePicker
    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var pickedMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val pickedDate = remember(pickedMillis) { Date(pickedMillis) }
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = { TextButton(onClick = { showPicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancelar") } }
        ) {
            val state = rememberDatePickerState(initialSelectedDateMillis = pickedMillis)
            DatePicker(state = state, showModeToggle = true)
            LaunchedEffect(state.selectedDateMillis) {
                state.selectedDateMillis?.let { pickedMillis = it }
            }
        }
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val canSave = name.isNotBlank() && amount > 0 && selectedCategory != null

    Scaffold(snackbarHost = { SnackbarHost(snack) }) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del gasto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Categoría
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Selecciona categoría",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name) },
                            onClick = { selectedCategory = c; expanded = false }
                        )
                    }
                }
            }

            // Fecha
            OutlinedTextField(
                value = dateFmt.format(pickedDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha") },
                trailingIcon = {
                    IconButton(onClick = { showPicker = true }) {
                        Icon(Icons.Default.Event, contentDescription = "Elegir fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Nota
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

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
                            cat = cat,
                            date = pickedDate,
                            note = note.takeIf { it.isNotBlank() }
                        )
                        name = ""; amountText = ""; note = ""; selectedCategory = null
                        Toast.makeText(ctx, "Gasto guardado", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Guardar") }
            }
        }
    }
}
