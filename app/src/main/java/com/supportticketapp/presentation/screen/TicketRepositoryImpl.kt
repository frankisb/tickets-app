package com.supportticketapp.presentation.screen

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.supportticketapp.presentation.TicketRepository
import com.supportticketapp.presentation.Ticket
import com.supportticketapp.presentation.TicketStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TicketRepositoryImpl : TicketRepository {

    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun createTicket(ticket: Ticket) {
        val docRef = if (ticket.id.isNotBlank()) {
            firestore.collection("tickets").document(ticket.id)
        } else {
            firestore.collection("tickets").document()
        }
        val ticketWithId = ticket.copy(id = docRef.id)

        val data = hashMapOf(
            "id" to ticketWithId.id,
            "businessName" to ticketWithId.businessName,
            "phone" to ticketWithId.phone,
            "description" to ticketWithId.description,
            "imageUrl" to ticketWithId.imageUrl,
            "status" to ticketWithId.status.name,
            "createdAt" to ticketWithId.createdAt
        )

        suspendCancellableCoroutine { cont ->
            docRef.set(data)
                .addOnSuccessListener {
                    if (cont.isActive) cont.resume(Unit)
                }
                .addOnFailureListener { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
        }
    }

    override fun observeTickets(): Flow<List<Ticket>> = callbackFlow {
        val listener = firestore.collection("tickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TicketRepositoryImpl", "observeTickets error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val businessName = doc.getString("businessName") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val description = doc.getString("description") ?: ""
                    val imageUrl = doc.getString("imageUrl")
                    val statusRaw = doc.getString("status") ?: TicketStatus.IN_PROGRESS.name
                    val status = runCatching { TicketStatus.valueOf(statusRaw) }
                        .getOrDefault(TicketStatus.IN_PROGRESS)
                    val createdAt = doc.getLong("createdAt") ?: 0L

                    Ticket(
                        id = id,
                        businessName = businessName,
                        phone = phone,
                        description = description,
                        imageUrl = imageUrl,
                        status = status,
                        createdAt = createdAt
                    )
                } ?: emptyList()

                trySend(tickets)
            }

        awaitClose { listener.remove() }
    }

    fun observeTicketsByPhone(phone: String): Flow<List<Ticket>> = callbackFlow {
        val listener = firestore.collection("tickets")
            .whereEqualTo("phone", phone)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TicketRepositoryImpl", "observeTicketsByPhone error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val businessName = doc.getString("businessName") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val description = doc.getString("description") ?: ""
                    val imageUrl = doc.getString("imageUrl")
                    val statusRaw = doc.getString("status") ?: TicketStatus.IN_PROGRESS.name
                    val status = runCatching { TicketStatus.valueOf(statusRaw) }
                        .getOrDefault(TicketStatus.IN_PROGRESS)
                    val createdAt = doc.getLong("createdAt") ?: 0L
                    val lastMessage = doc.getString("lastMessage") ?: ""

                    Ticket(
                        id = id,
                        businessName = businessName,
                        phone = phone,
                        description = description,
                        imageUrl = imageUrl,
                        status = status,
                        createdAt = createdAt,
                        lastMessage = lastMessage
                    )
                } ?: emptyList()

                trySend(tickets)
            }

        awaitClose { listener.remove() }
    }
}
