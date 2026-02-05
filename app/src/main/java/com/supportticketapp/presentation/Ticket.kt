package com.supportticketapp.presentation

data class Ticket(
    val id: String = "",
    val businessName: String = "",
    val phone: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val status: TicketStatus = TicketStatus.IN_PROGRESS,
    val createdAt: Long = 0L,
    val lastMessage: String = ""
)
