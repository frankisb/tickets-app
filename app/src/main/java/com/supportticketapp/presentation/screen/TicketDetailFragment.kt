package com.supportticketapp.presentation.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.supportticketapp.R
import com.supportticketapp.presentation.TicketStatus
import com.supportticketapp.presentation.UserPreferences
import com.supportticketapp.presentation.fcm.FcmSender
import kotlinx.coroutines.launch

class TicketDetailFragment : Fragment() {

    private var snapshotListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_ticket_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ticketId = requireArguments().getString(ARG_TICKET_ID).orEmpty()
        if (ticketId.isBlank()) {
            parentFragmentManager.popBackStack()
            return
        }

        val tvBusinessName = view.findViewById<TextView>(R.id.tvBusinessName)
        val tvPhone = view.findViewById<TextView>(R.id.tvPhone)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvCurrentStatus = view.findViewById<TextView>(R.id.tvCurrentStatus)
        val tvLastMessage = view.findViewById<TextView>(R.id.tvLastMessage)
        val imgTicket = view.findViewById<ImageView>(R.id.imgTicket)
        val rvImages = view.findViewById<RecyclerView>(R.id.rvImages)
        val etResponse = view.findViewById<EditText>(R.id.etResponse)
        val btnSendResponse = view.findViewById<Button>(R.id.btnSendResponse)

        val btnInProgress = view.findViewById<Button>(R.id.btnInProgress)
        val btnEscalated = view.findViewById<Button>(R.id.btnEscalated)
        val btnCallSoon = view.findViewById<Button>(R.id.btnCallSoon)

        imgTicket.visibility = View.GONE

        rvImages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("tickets").document(ticketId)

        // Helper para obtener token FCM del cliente por teléfono (placeholder)
        fun getFcmTokenForPhone(phone: String): String {
            // Aquí podrías leer desde una colección en Firestore: customers/{phone}/fcmToken
            // Por ahora, devuelve vacío si no guardas tokens por teléfono
            return ""
        }

        fun highlightStatus(selected: TicketStatus) {
            btnInProgress.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            btnEscalated.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            btnCallSoon.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))

            when (selected) {
                TicketStatus.IN_PROGRESS -> btnInProgress.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light, null))
                TicketStatus.ESCALATED_LEVEL_2 -> btnEscalated.setBackgroundColor(resources.getColor(android.R.color.holo_orange_light, null))
                TicketStatus.WILL_CALL_SOON -> btnCallSoon.setBackgroundColor(resources.getColor(android.R.color.holo_green_light, null))
                else -> {}
            }
        }

        snapshotListener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
            if (view == null) return@addSnapshotListener

            tvBusinessName.text = snapshot.getString("businessName").orEmpty()
            tvPhone.text = snapshot.getString("phone").orEmpty()
            tvDescription.text = snapshot.getString("description").orEmpty()

            val lastMessage = snapshot.getString("lastMessage")
            tvLastMessage.text = if (lastMessage.isNullOrBlank()) {
                "Último mensaje de soporte: (aún no hay mensajes)"
            } else {
                "Último mensaje de soporte:\n$lastMessage"
            }

            val imageUrls = snapshot.get("imageUrls") as? List<String> ?: emptyList()
            if (imageUrls.isNotEmpty()) {
                rvImages.visibility = View.VISIBLE
                rvImages.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                        val img = ImageView(parent.context)
                        img.scaleType = ImageView.ScaleType.CENTER_CROP
                        img.setPadding(4, 4, 4, 4)
                        val size = (resources.displayMetrics.density * 120).toInt()
                        img.layoutParams = RecyclerView.LayoutParams(size, size)
                        return object : RecyclerView.ViewHolder(img) {}
                    }

                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        Glide.with(requireContext())
                            .load(imageUrls[position])
                            .into(holder.itemView as ImageView)
                    }

                    override fun getItemCount(): Int = imageUrls.size
                }
            } else {
                rvImages.visibility = View.GONE
            }

            val statusRaw = snapshot.getString("status") ?: TicketStatus.IN_PROGRESS.name
            val status = try {
                TicketStatus.valueOf(statusRaw)
            } catch (e: IllegalArgumentException) {
                TicketStatus.IN_PROGRESS
            }
            tvCurrentStatus.text = "Estado actual: ${status.name.replace("_", " ")}"
            highlightStatus(status)
        }

        btnSendResponse.setOnClickListener {
            val response = etResponse.text.toString().trim()
            if (response.isBlank()) {
                Toast.makeText(requireContext(), "Escribe una respuesta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    docRef.update("lastMessage", response)
                    Toast.makeText(requireContext(), "Respuesta enviada", Toast.LENGTH_SHORT).show()
                    etResponse.setText("")

                    // Enviar notificación push al cliente
                    val customerPhone = tvPhone.text.toString()
                    val fcmToken = getFcmTokenForPhone(customerPhone)
                    if (fcmToken.isNotEmpty()) {
                        FcmSender.sendNotification(
                            toToken = fcmToken,
                            title = "Nueva respuesta de soporte",
                            body = "Revisa tu ticket $ticketId"
                        )
                    }

                    // Volver a la lista de tickets sin cerrar la app
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, WorkingTicketFragment())
                        .commit()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al enviar respuesta", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun updateStatus(status: TicketStatus) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    docRef.update("status", status.name)
                    Toast.makeText(requireContext(), "Estado actualizado", Toast.LENGTH_SHORT).show()

                    // Enviar notificación push al cliente
                    val customerPhone = tvPhone.text.toString()
                    val fcmToken = getFcmTokenForPhone(customerPhone)
                    if (fcmToken.isNotEmpty()) {
                        FcmSender.sendNotification(
                            toToken = fcmToken,
                            title = "Estado de ticket actualizado",
                            body = "Tu ticket $ticketId ahora está: ${status.name.replace("_", " ")}"
                        )
                    }
                    // No cerrar automaticamente; permite seguir operando
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnInProgress.setOnClickListener { updateStatus(TicketStatus.IN_PROGRESS) }
        btnEscalated.setOnClickListener { updateStatus(TicketStatus.ESCALATED_LEVEL_2) }
        btnCallSoon.setOnClickListener { updateStatus(TicketStatus.WILL_CALL_SOON) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        snapshotListener = null
    }

    companion object {
        private const val ARG_TICKET_ID = "ticket_id"

        fun newInstance(ticketId: String): TicketDetailFragment {
            return TicketDetailFragment().apply {
                arguments = Bundle().apply { putString(ARG_TICKET_ID, ticketId) }
            }
        }
    }
}
