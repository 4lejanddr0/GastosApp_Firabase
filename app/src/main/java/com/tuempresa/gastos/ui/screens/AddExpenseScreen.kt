package com.tuempresa.gastos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tuempresa.gastos.data.model.Category
import com.tuempresa.gastos.ui.viewmodel.ExpenseViewModel
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(vm: ExpenseViewModel) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf(Category.ALIMENTACION) }
    var note by remember { mutableStateOf("") }

    var openDate by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(Date()) }

    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nuevo gasto", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del gasto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.replace(',', '.') },
            label = { Text("Monto") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // --- Categoría (DropdownMenu clásico) ---
        Box {
            OutlinedTextField(
                value = cat.name.lowercase().replaceFirstChar { it.titlecase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                trailingIcon = { IconButton(onClick = { menuOpen = !menuOpen }) { Icon(Icons.Default.ArrowDropDown, null) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { menuOpen = true } // abre al tocar el campo
            )

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                Category.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name.lowercase().replaceFirstChar { it.titlecase() }) },
                        onClick = {
                            cat = option
                            menuOpen = false
                        }
                    )
                }
            }
        }

        // --- Fecha ---
        OutlinedButton(onClick = { openDate = true }) {
            val fmt = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            Text("Fecha: ${fmt.format(date)}")
        }
        if (openDate) {
            DatePickerDialog(
                onDismissRequest = { openDate = false },
                confirmButton = {
                    val state = rememberDatePickerState(initialSelectedDateMillis = date.time)
                    TextButton(onClick = {
                        state.selectedDateMillis?.let { date = Date(it) }
                        openDate = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { openDate = false }) { Text("Cancelar") }
                }
            ) {
                // Estado debe estar dentro del contenido del diálogo para recomponer bien
                val state = rememberDatePickerState(initialSelectedDateMillis = date.time)
                DatePicker(state = state)
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Nota (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        val enabled = name.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true

        Button(
            enabled = enabled,
            onClick = {
                vm.addExpense(name, amount.toDouble(), cat, date, note.ifBlank { null })
                name = ""
                amount = ""
                note = ""
            }
        ) {
            Text("Guardar")
        }
    }
}
