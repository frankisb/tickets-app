package com.supportticketapp.presentation.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.supportticketapp.R

class TicketStatusFragment : Fragment() {

    private var snapshotListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_ticket_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ticketId = requireArguments().getString(ARG_TICKET_ID).orEmpty()
        if (ticketId.isBlank()) {
            parentFragmentManager.popBackStack()
            return
        }

        val tvTicketId = view.findViewById<TextView>(R.id.tvTicketId)
        val tvTicketStatus = view.findViewById<TextView>(R.id.tvTicketStatus)
        val tvLastMessage = view.findViewById<TextView>(R.id.tvLastMessage)
        val rvImages = view.findViewById<RecyclerView>(R.id.rvImages)

        rvImages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        tvTicketId.text = "Ticket ID: $ticketId"

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("tickets").document(ticketId)

        snapshotListener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
            if (view == null) return@addSnapshotListener

            val status = snapshot.getString("status").orEmpty()
            val lastMessage = snapshot.getString("lastMessage").orEmpty()

            tvTicketStatus.text = "Estado: ${if (status.isBlank()) "IN_PROGRESS" else status}"
            tvLastMessage.text = if (lastMessage.isBlank()) {
                "Último mensaje"
            } else {
                lastMessage
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
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        snapshotListener = null
    }

    companion object {
        private const val ARG_TICKET_ID = "ticket_id"

        fun newInstance(ticketId: String): TicketStatusFragment {
            return TicketStatusFragment().apply {
                arguments = Bundle().apply { putString(ARG_TICKET_ID, ticketId) }
            }
        }
    }
}
