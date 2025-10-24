package com.tuempresa.gastos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.alpha
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.tuempresa.gastos.data.model.Category
import com.tuempresa.gastos.data.model.Expense
import com.tuempresa.gastos.data.repo.ExpenseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val nombre = user?.displayName ?: user?.email ?: "Usuario"

    var selectedTab by remember { mutableStateOf(0) } // 0 = Agregar, 1 = Historial
    val tabs = listOf("Agregar", "Historial")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gastos personales") },
                actions = { TextButton(onClick = onLogout) { Text("Salir") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Bienvenido, $nombre 👋",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> AddExpenseSection()
                1 -> HistorySection()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseSection() {
    val repo = remember { ExpenseRepository() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.values().first()) }
    var note by remember { mutableStateOf("") }

    // estado del menú y tamaño del campo para anclar ancho
    var menuOpen by remember { mutableStateOf(false) }
    val categories = remember { Category.values().toList() }
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Nuevo gasto", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del gasto") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // ---- Selector de categoría con overlay clickable + menú anclado ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f) // asegura que el menú quede arriba
        ) {
            OutlinedTextField(
                value = category.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords -> fieldSize = coords.size.toSize() }
            )
            // Capa invisible que SÍ captura el toque, aunque el TextField sea readOnly
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .clickable { menuOpen = true }
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                modifier = Modifier.width(with(density) { fieldSize.width.toDp() })
            ) {
                categories.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.name) },
                        onClick = {
                            category = c
                            menuOpen = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Nota (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        val canSave = name.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0.0

        Button(
            enabled = canSave,
            onClick = {
                val expense = Expense(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    category = category,
                    date = Timestamp.now(),
                    note = note.ifBlank { null }
                )
                scope.launch {
                    repo.add(expense)
                    name = ""; amount = ""; note = ""
                }
            }
        ) { Text("Guardar") }
    }
}


@Composable
private fun HistorySection() {
    val repo = remember { ExpenseRepository() }
    val scope = rememberCoroutineScope()

    // Mes actual
    var cal by remember { mutableStateOf(Calendar.getInstance()) }
    val year = cal.get(Calendar.YEAR)
    val month0 = cal.get(Calendar.MONTH)

    val expensesFlow = remember(year, month0) { repo.byMonthFlow(year, month0) }
    val expenses by expensesFlow.collectAsState(initial = emptyList())

    val total = expenses.sumOf { it.amount }
    val monthName = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
        .format(cal.time)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {
                cal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            }) { Text("◀ Mes anterior") }

            Text(monthName, style = MaterialTheme.typography.titleMedium)

            TextButton(onClick = {
                cal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, +1) }
            }) { Text("Mes siguiente ▶") }
        }

        Spacer(Modifier.height(8.dp))
        Text("Total del mes: $${"%.2f".format(total)}", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(expenses, key = { it.id }) { e ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(e.name, style = MaterialTheme.typography.titleSmall)
                        Text("${e.category.name} • ${fmt.format(e.date.toDate())}")
                        Text("$${"%.2f".format(e.amount)}", style = MaterialTheme.typography.bodyLarge)
                        if (!e.note.isNullOrBlank()) {
                            Text("Nota: ${e.note}")
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { scope.launch { repo.delete(e.id) } }) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
