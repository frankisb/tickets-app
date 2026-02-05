package com.supportticketapp.presentation.screen

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.supportticketapp.R
import com.supportticketapp.presentation.DebugLog
import com.supportticketapp.presentation.MainActivity
import com.supportticketapp.presentation.NotificationHelper
import com.supportticketapp.presentation.UserPreferences
import com.supportticketapp.presentation.auth.AuthManager
import com.supportticketapp.presentation.screen.CreateTicketFormFragment
import com.supportticketapp.presentation.screen.TicketAdapter
import com.supportticketapp.presentation.screen.TicketRepositoryImpl
import com.supportticketapp.presentation.screen.TicketStatusFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CustomerTicketsFragment : Fragment() {

    private val lastKnownLastMessageById = mutableMapOf<String, String>()

    private val requestPostNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_customer_tickets, container, false)

        val btnNewTicket = view.findViewById<Button>(R.id.btnNewTicket)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val rvTickets = view.findViewById<RecyclerView>(R.id.rvTickets)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        val adapter = TicketAdapter { ticket ->
            if (ticket.id.isBlank()) return@TicketAdapter

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TicketStatusFragment.newInstance(ticket.id))
                .addToBackStack(null)
                .commit()
        }
        rvTickets.layoutManager = LinearLayoutManager(requireContext())
        rvTickets.adapter = adapter

        val repository = TicketRepositoryImpl()
        val phone = UserPreferences.getCustomerPhone(requireContext())
        val notificationsEnabled = UserPreferences.isNotificationsEnabled(requireContext())
        if (notificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        NotificationHelper.ensureChannel(requireContext())

        // Utilizar el ciclo de vida de la Activity para que el observador siga activo al navegar a detalle (H1)
        activity?.lifecycleScope?.launch {
            if (phone.isNotBlank()) {
                repository.observeTicketsByPhone(phone).collect { tickets ->
                    // #region log de agente
                    activity?.applicationContext?.let { appCtx ->
                        DebugLog.ingest(appCtx, "CustomerTicketsFragment.collect", "tickets recibidos", mapOf("count" to tickets.size, "notificationsEnabled" to notificationsEnabled, "phone" to phone.take(3) + "***"), "H1")
                    }
                    // #endregion
                    if (notificationsEnabled) {
                        tickets.forEach { t ->
                            if (t.id.isBlank()) return@forEach

                            val newMessage = t.lastMessage ?: ""
                            val oldMessage = lastKnownLastMessageById[t.id]
                            val wouldShow = oldMessage != null && oldMessage != newMessage && newMessage.isNotBlank()
                            if (wouldShow) {
                                // #region log de agente
                                activity?.applicationContext?.let { appCtx ->
                                    DebugLog.ingest(appCtx, "CustomerTicketsFragment.check", "nuevo mensaje detectado", mapOf("ticketId" to t.id, "oldMsgLen" to (oldMessage?.length ?: -1), "newMsgLen" to newMessage.length), "H4")
                                }
                                // #endregion
                                val appCtx = activity?.applicationContext
                                if (appCtx != null) {
                                    // #region log de agente
                                    DebugLog.ingest(appCtx, "CustomerTicketsFragment.show", "mostrando notificación", mapOf("ticketId" to t.id), "H4")
                                    // #endregion
                                    NotificationHelper.showNotification(
                                        appCtx,
                                        "Nueva respuesta de soporte",
                                        "Revisa tu ticket ${t.id}"
                                    )
                                }
                            }
                            lastKnownLastMessageById[t.id] = newMessage
                        }
                    }
                    // Actualizar UI solo si el fragment sigue visible (evitar crash si estamos en otro fragment)
                    if (this@CustomerTicketsFragment.view != null) {
                        adapter.submitList(tickets)
                        tvEmpty.visibility = if (tickets.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            } else {
                adapter.submitList(emptyList())
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "No se configuró un teléfono de cliente"
            }
        }

        btnNewTicket.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateTicketFormFragment())
                .addToBackStack(null)
                .commit()
        }

        btnLogout.setOnClickListener {
            AuthManager.logout(requireContext())
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, com.supportticketapp.presentation.auth.LoginChooserFragment.newInstance())
                .commit()
        }

        return view
    }

    companion object {
        fun newInstance() = CustomerTicketsFragment()
    }
}
