package com.tuempresa.gastos.data.model

import com.google.firebase.Timestamp
import java.util.UUID

enum class Category { ALIMENTACION, TRANSPORTE, HOGAR, SALUD, ENTRETENIMIENTO, OTROS }

data class Expense(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val category: Category? = null,
    val date: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val note: String? = null
)