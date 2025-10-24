package com.tuempresa.gastos.data.model

import com.google.firebase.Timestamp
import java.util.UUID

enum class Category { ALIMENTACION, TRANSPORTE, HOGAR, SALUD, ENTRETENIMIENTO, OTROS }

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val amount: Double = 0.0,
    val category: Category = Category.OTROS,
    val date: Timestamp = Timestamp.now(),
    val note: String? = null
)