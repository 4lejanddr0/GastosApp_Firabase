package com.tuempresa.gastos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.tuempresa.gastos.data.model.Expense
import com.tuempresa.gastos.ui.viewmodel.ExpenseViewModel
import com.tuempresa.gastos.util.money
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(vm: ExpenseViewModel) {
    val state by vm.state.collectAsState()
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(Modifier.fillMaxSize()) {
        // Selector de mes
        MonthSelector(
            year = state.year,
            month0 = state.month0,
            onChange = { y, m -> vm.changeMonth(y, m) }
        )

        Text(
            text = "Total del mes: ${money(state.total)}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        HorizontalDivider()

        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.items, key = { it.id }) { e ->
                ExpenseItem(
                    e = e,
                    sdf = sdf,
                    onDelete = { vm.delete(e.id) }
                )
            }
        }
    }
}

@Composable
private fun ExpenseItem(
    e: Expense,
    sdf: SimpleDateFormat,
    onDelete: () -> Unit
) {
    // Soporta Date o Timestamp sin reflección
    val dateText = remember(e.date) {
        val date: Date = when (val d = e.date) {
            is Timestamp -> d.toDate()
            is Date -> d
            else -> Date()
        }
        sdf.format(date)
    }

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(e.name, style = MaterialTheme.typography.titleMedium)
            Text("${e.category} • $dateText")
            Text(money(e.amount), style = MaterialTheme.typography.titleLarge)
            e.note?.takeIf { it.isNotBlank() }?.let { Text("Nota: $it") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDelete) { Text("Eliminar") }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    year: Int,
    month0: Int,
    onChange: (Int, Int) -> Unit
) {
    val localeEs = remember { Locale("es") }
    val monthLabel = remember(year, month0) {
        val cal = Calendar.getInstance().apply { set(year, month0, 1) }
        SimpleDateFormat("MMMM yyyy", localeEs)
            .format(cal.time)
            .replaceFirstChar { it.titlecase(localeEs) }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = {
            val cal = Calendar.getInstance().apply {
                set(year, month0, 1)
                add(Calendar.MONTH, -1)
            }
            onChange(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }) { Text("◀ Mes anterior") }

        Text(monthLabel, style = MaterialTheme.typography.titleMedium)

        TextButton(onClick = {
            val cal = Calendar.getInstance().apply {
                set(year, month0, 1)
                add(Calendar.MONTH, 1)
            }
            onChange(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }) { Text("Mes siguiente ▶") }
    }
}
