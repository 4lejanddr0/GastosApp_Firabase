package com.tuempresa.gastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.tuempresa.gastos.data.model.Category
import com.tuempresa.gastos.data.model.Expense
import com.tuempresa.gastos.data.repo.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class ExpenseUiState(
    val items: List<Expense> = emptyList(),
    val total: Double = 0.0,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month0: Int = Calendar.getInstance().get(Calendar.MONTH),
    val loading: Boolean = false,
    val error: String? = null
)

class ExpenseViewModel(
    private val repo: ExpenseRepository = ExpenseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseUiState())
    val state: StateFlow<ExpenseUiState> = _state.asStateFlow()

    init {
        observeMonth(_state.value.year, _state.value.month0)
    }

    fun changeMonth(year: Int, month0: Int) {
        _state.value = _state.value.copy(year = year, month0 = month0)
        observeMonth(year, month0)
    }

    private fun observeMonth(year: Int, month0: Int) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        repo.byMonthFlow(year, month0).collectLatest { list ->
            val total = list.sumOf { it.amount }
            _state.value = _state.value.copy(items = list, total = total, loading = false)
        }
    }

    /**
     * Agrega un gasto para el usuario logueado.
     * REQUIERE reglas tipo:
     * match /users/{userId}/expenses/{docId} {
     *   allow read, write: if request.auth != null && request.auth.uid == userId;
     * }
     */
    fun addExpense(
        name: String,
        amount: Double,
        cat: Category,
        date: Date,
        note: String?
    ) = viewModelScope.launch {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _state.value = _state.value.copy(error = "Debes iniciar sesión para guardar gastos.")
            return@launch
        }

        runCatching {
            val expense = Expense(
                name = name.trim(),
                amount = amount,
                category = cat,
                date = Timestamp(date),
                note = note?.takeIf { it.isNotBlank() },
                userId = uid // <-- clave para que pasen las reglas
            )
            repo.add(expense)
        }.onFailure { e ->
            _state.value = _state.value.copy(error = e.message ?: "Error al guardar el gasto")
        }
    }

    fun delete(id: String) = viewModelScope.launch {
        runCatching { repo.delete(id) }
            .onFailure { e ->
                _state.value = _state.value.copy(error = e.message ?: "Error al eliminar")
            }
    }
}
