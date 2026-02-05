package com.supportticketapp.presentation.screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.supportticketapp.R
import com.supportticketapp.presentation.Ticket

class TicketAdapter(
    private val onClick: (Ticket) -> Unit
) : ListAdapter<Ticket, TicketAdapter.TicketViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBusinessName: TextView = itemView.findViewById(R.id.tvBusinessName)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)

        fun bind(ticket: Ticket) {
            tvBusinessName.text = ticket.businessName
            tvDescription.text = ticket.description

            itemView.setOnClickListener {
                onClick(ticket)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Ticket>() {
            override fun areItemsTheSame(oldItem: Ticket, newItem: Ticket): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Ticket, newItem: Ticket): Boolean {
                return oldItem == newItem
            }
        }
    }
}
