package com.supportticketapp.presentation.auth

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
import com.supportticketapp.R
import com.supportticketapp.presentation.screen.WorkingTicketFragment
import com.supportticketapp.presentation.UserPreferences
import com.supportticketapp.presentation.auth.AuthManager
import com.supportticketapp.presentation.auth.UserRole
import kotlinx.coroutines.launch

class SupportLoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_support_login, container, false)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val ivTogglePassword = view.findViewById<ImageView>(R.id.ivTogglePassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvError = view.findViewById<TextView>(R.id.tvError)

        var isPasswordVisible = false

        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
            }
            // Mover cursor al final
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                tvError.text = "Completa todos los campos"
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            tvError.text = ""

            viewLifecycleOwner.lifecycleScope.launch {
                val success = AuthManager.loginSupport(email, password)
                btnLogin.isEnabled = true
                if (success) {
                    AuthManager.setCurrentRole(requireContext(), UserRole.SUPPORT)
                    Toast.makeText(requireContext(), "Bienvenido soporte", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, WorkingTicketFragment())
                        .commit()
                } else {
                    tvError.text = "Credenciales incorrectas"
                }
            }
        }

        return view
    }

    companion object {
        fun newInstance() = SupportLoginFragment()
    }
}
