package com.supportticketapp.presentation

import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    suspend fun createTicket(ticket: Ticket)
    fun observeTickets(): Flow<List<Ticket>>
}
