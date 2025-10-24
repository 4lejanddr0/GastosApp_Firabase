package com.tuempresa.gastos.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun money(d: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es","SV")).format(d)

fun monthKey(year: Int, monthZeroBased: Int): String = "%04d-%02d".format(year, monthZeroBased+1)

val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale("es","SV"))