package com.tuempresa.gastos.data.repo

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tuempresa.gastos.data.model.Expense
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.GregorianCalendar

class ExpenseRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    /** UID del usuario actual o error si no hay sesión */
    private fun requireUid(): String =
        auth.currentUser?.uid ?: error("Usuario no autenticado (UID nulo)")

    /** Referencia a /users/{uid}/expenses */
    private fun col() = db.collection("users")
        .document(requireUid())
        .collection("expenses")

    /** Crear/actualizar un gasto */
    suspend fun add(expense: Expense) {
        col().document(expense.id).set(expense).await()
    }

    /** Eliminar un gasto */
    suspend fun delete(id: String) {
        col().document(id).delete().await()
    }

    /** Flujo de gastos por mes (month0: 0=enero, 11=diciembre) */
    fun byMonthFlow(year: Int, month0: Int): Flow<List<Expense>> = callbackFlow {
        // Si no hay usuario logueado, devolvemos vacío y cerramos el flow
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close(IllegalStateException("Usuario no autenticado"))
            return@callbackFlow
        }

        val start = Timestamp(GregorianCalendar(year, month0, 1).time)
        val end = Timestamp(GregorianCalendar(year, month0 + 1, 1).time)

        val registration = db.collection("users")
            .document(uid)
            .collection("expenses")
            .whereGreaterThanOrEqualTo("date", start)
            .whereLessThan("date", end)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    trySend(snap.documents.mapNotNull { it.toObject(Expense::class.java) })
                }
            }

        awaitClose { registration.remove() }
    }
}
