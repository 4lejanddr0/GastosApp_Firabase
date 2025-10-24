package com.tuempresa.gastos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // <-- IMPORT QUE FALTABA
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tuempresa.gastos.data.model.Expense
import com.tuempresa.gastos.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    expenseVm: ExpenseViewModel
) {
    val ui by expenseVm.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Total del mes: ${"%.2f".format(ui.total)}", style = MaterialTheme.typography.titleMedium)
        Divider(Modifier.padding(vertical = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ui.items, key = { it.id }) { exp ->
                ExpenseItem(exp)
            }
        }
    }
}

@Composable
private fun ExpenseItem(expense: Expense) {
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(expense.name, style = MaterialTheme.typography.titleMedium)
            Text(expense.category?.name ?: "Sin categoría", style = MaterialTheme.typography.bodyMedium)
            Text(fmt.format(expense.date.toDate()), style = MaterialTheme.typography.bodySmall)
            if (!expense.note.isNullOrBlank()) Text(expense.note!!, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            Text("$${"%.2f".format(expense.amount)}", style = MaterialTheme.typography.titleMedium)
        }
    }
}
