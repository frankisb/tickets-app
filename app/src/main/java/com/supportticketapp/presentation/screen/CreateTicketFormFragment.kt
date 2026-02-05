package com.supportticketapp.presentation.screen

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.material.switchmaterial.SwitchMaterial
import com.supportticketapp.R
import com.supportticketapp.presentation.Ticket
import com.supportticketapp.presentation.TicketStatus
import com.supportticketapp.presentation.UserPreferences
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CreateTicketFormFragment : Fragment() {

    private val photoUris = mutableListOf<Uri>()
    private var pendingPhotoUri: Uri? = null

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara requerido", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingPhotoUri
        if (success && uri != null) {
            photoUris.add(uri)
            pendingPhotoUri = null

            view?.findViewById<RecyclerView>(R.id.rvPhotos)
                ?.adapter
                ?.let { adapter ->
                    if (adapter is PhotoPreviewAdapter) {
                        adapter.submitList(photoUris.toList())
                    }
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_create_ticket_form, container, false)

        val etBusinessName = view.findViewById<EditText>(R.id.etBusinessName)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnScanQr = view.findViewById<Button>(R.id.btnScanQr)
        val btnTakePhoto = view.findViewById<Button>(R.id.btnTakePhoto)
        val rvPhotos = view.findViewById<RecyclerView>(R.id.rvPhotos)
        val swNotifications = view.findViewById<SwitchMaterial>(R.id.swNotifications)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        lateinit var photoAdapter: PhotoPreviewAdapter
        photoAdapter = PhotoPreviewAdapter { uri ->
            photoUris.remove(uri)
            photoAdapter.submitList(photoUris.toList())
        }
        rvPhotos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvPhotos.adapter = photoAdapter
        photoAdapter.submitList(photoUris.toList())

        swNotifications.isChecked = UserPreferences.isNotificationsEnabled(requireContext())
        swNotifications.setOnCheckedChangeListener { _, isChecked ->
            UserPreferences.setNotificationsEnabled(requireContext(), isChecked)
        }

        parentFragmentManager.setFragmentResultListener(
            BarcodeScannerFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val rawValue = bundle.getString(BarcodeScannerFragment.BUNDLE_KEY_RAW_VALUE).orEmpty()
            if (rawValue.isNotBlank()) {
                val current = etDescription.text?.toString().orEmpty()
                etDescription.setText(
                    if (current.isBlank()) rawValue else "$current\n$rawValue"
                )
            }
        }

        btnScanQr.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BarcodeScannerFragment())
                .addToBackStack(null)
                .commit()
        }

        btnTakePhoto.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            } else {
                launchCamera()
            }
        }

        btnSave.setOnClickListener {
            val businessName = etBusinessName.text.toString()
            val phone = etPhone.text.toString()
            val description = etDescription.text.toString()

            if (businessName.isEmpty() || phone.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                btnSave.isEnabled = false

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        Log.d("CreateTicketFormFragment", "Sending ticket. photosSelected=${photoUris.size}")
                        val db = FirebaseFirestore.getInstance()
                        val storage = FirebaseStorage.getInstance()
                        Log.d(
                            "CreateTicketFormFragment",
                            "FirebaseStorage initialized. bucket=${storage.reference.bucket}"
                        )

                        val docRef = db.collection("tickets").document()
                        val ticketId = docRef.id

                        val ticket = Ticket(
                            id = ticketId,
                            businessName = businessName,
                            phone = phone,
                            description = description,
                            status = TicketStatus.IN_PROGRESS,
                            createdAt = System.currentTimeMillis()
                        )

                        // crear documento de ticket primero
                        TicketRepositoryImpl().createTicket(ticket)

                        var photosUploadedOk = true
                        if (photoUris.isNotEmpty()) {
                            try {
                                val downloadUrls = mutableListOf<String>()
                                photoUris.forEachIndexed { index, uri ->
                                    val ref = storage.reference
                                        .child("tickets")
                                        .child(ticketId)
                                        .child("photo_${System.currentTimeMillis()}_$index.jpg")

                                    suspendCancellableCoroutine { cont ->
                                        ref.putFile(uri)
                                            .addOnSuccessListener {
                                                ref.downloadUrl
                                                    .addOnSuccessListener { downloadUri ->
                                                        if (cont.isActive) cont.resume(downloadUri.toString())
                                                    }
                                                    .addOnFailureListener { e ->
                                                        if (cont.isActive) cont.resumeWithException(e)
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                if (cont.isActive) cont.resumeWithException(e)
                                            }
                                    }.let { url ->
                                        downloadUrls.add(url)
                                    }
                                }

                                Log.d(
                                    "CreateTicketFormFragment",
                                    "Photo upload finished. photosSelected=${photoUris.size} urls=${downloadUrls.size}"
                                )

                                if (downloadUrls.isEmpty()) {
                                    photosUploadedOk = false
                                }

                                if (downloadUrls.isNotEmpty()) {
                                    suspendCancellableCoroutine { cont ->
                                        docRef.update("imageUrls", downloadUrls)
                                            .addOnSuccessListener {
                                                if (cont.isActive) cont.resume(Unit)
                                            }
                                            .addOnFailureListener { e ->
                                                if (cont.isActive) cont.resumeWithException(e)
                                            }
                                    }
                                }
                            } catch (e: Exception) {
                                photosUploadedOk = false
                                Log.e("CreateTicketFormFragment", "Ticket created but photo upload/update failed", e)
                            }
                        }

                        if (photosUploadedOk) {
                            Toast.makeText(requireContext(), "Ticket enviado", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Ticket enviado, pero no se pudieron subir las fotos", Toast.LENGTH_LONG).show()
                        }

                        // Guardar teléfono para vista de cliente
                        UserPreferences.setCustomerPhone(requireContext(), phone)

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, CustomerTicketsFragment.newInstance())
                            .commit()
                    } catch (e: Exception) {
                        btnSave.isEnabled = true
                        Log.e("CreateTicketFormFragment", "Error sending ticket", e)
                        val msg = e.message ?: e.javaClass.simpleName
                        Toast.makeText(requireContext(), "Error al guardar el ticket: $msg", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        btnCancel.setOnClickListener {
            // Volver a la pantalla principal
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WorkingTicketFragment())
                .commit()
        }

        return view
    }

    private fun launchCamera() {
        val context = requireContext()
        val photoFile = File(context.cacheDir, "ticket_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        pendingPhotoUri = uri
        takePicture.launch(uri)
    }
}
