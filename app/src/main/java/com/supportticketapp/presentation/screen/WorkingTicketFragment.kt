package com.supportticketapp.presentation.screen

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.supportticketapp.R
import com.supportticketapp.presentation.MainActivity
import com.supportticketapp.presentation.NotificationHelper
import com.supportticketapp.presentation.UserPreferences
import com.supportticketapp.presentation.auth.AuthManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WorkingTicketFragment : Fragment() {

    private val requestPostNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val lastKnownStatusById = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_working_ticket, container, false)

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        val rvTickets = view.findViewById<RecyclerView>(R.id.rvTickets)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        val adapter = TicketAdapter { ticket ->
            if (ticket.id.isBlank()) return@TicketAdapter

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TicketDetailFragment.newInstance(ticket.id))
                .addToBackStack(null)
                .commit()
        }
        rvTickets.layoutManager = LinearLayoutManager(requireContext())
        rvTickets.adapter = adapter

        val repository = TicketRepositoryImpl()

        val notificationsEnabled = UserPreferences.isNotificationsEnabled(requireContext())

        if (notificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Ensure notification channel is created
        NotificationHelper.ensureChannel(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            repository.observeTickets().collect { tickets ->
                if (notificationsEnabled) {
                    tickets.forEach { t ->
                        val id = t.id
                        if (id.isBlank()) return@forEach

                        val newStatus = t.status.name
                        val oldStatus = lastKnownStatusById[id]

                        if (oldStatus != null && oldStatus != newStatus) {
                            Log.d("WorkingTicket", "Cambio detectado - Ticket: $id, De: $oldStatus a: $newStatus")
                            NotificationHelper.showTicketStatusChanged(requireContext(), id, newStatus)
                        }

                        lastKnownStatusById[id] = newStatus
                    }
                }

                adapter.submitList(tickets)
                tvEmpty.visibility = if (tickets.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        btnLogout.setOnClickListener {
            AuthManager.logout(requireContext())
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, com.supportticketapp.presentation.auth.LoginChooserFragment.newInstance())
                .commit()
        }

        return view
    }
}
