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
    /** UID actual o null (NO lanzar excepción aquí). */
    private fun currentUid(): String? = auth.currentUser?.uid

    /** /users/{uid}/expenses */
    private fun col(uid: String) = db.collection("users")
        .document(uid)
        .collection("expenses")

    /** Crear/actualizar un gasto. Si no hay id, lo crea. Devuelve el id. */
    suspend fun add(expense: Expense): String {
        val uid = currentUid() ?: throw IllegalStateException("No hay sesión activa")
        return if (expense.id.isBlank()) {
            val ref = col(uid).add(expense).await()
            ref.id
        } else {
            col(uid).document(expense.id).set(expense).await()
            expense.id
        }
    }

    suspend fun delete(id: String) {
        val uid = currentUid() ?: throw IllegalStateException("No hay sesión activa")
        col(uid).document(id).delete().await()
    }

    /** Flujo por mes (0=enero..11=diciembre). Si no hay uid, emite vacío y termina sin error. */
    fun byMonthFlow(year: Int, month0: Int): Flow<List<Expense>> = callbackFlow {
        val uid = currentUid()
        if (uid == null) {
            trySend(emptyList())
            // No cierres con excepción: simplemente cierra el canal sin error.
            awaitClose { /* noop */ }
            return@callbackFlow
        }

        val start = Timestamp(GregorianCalendar(year, month0, 1).time)
        val end = Timestamp(GregorianCalendar(year, month0 + 1, 1).time)

        val reg = db.collection("users").document(uid).collection("expenses")
            .whereGreaterThanOrEqualTo("date", start)
            .whereLessThan("date", end)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                val list = snap?.documents.orEmpty().mapNotNull { d ->
                    d.toObject(Expense::class.java)?.copy(id = d.id)
                }
                trySend(list)
            }

        awaitClose { reg.remove() }
    }
}
