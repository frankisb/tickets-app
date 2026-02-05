package com.supportticketapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.supportticketapp.R
import com.supportticketapp.presentation.MainActivity
import com.supportticketapp.presentation.auth.AuthManager
import com.supportticketapp.presentation.auth.UserRole
import com.supportticketapp.presentation.UserPreferences

class LoginChooserFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_login_chooser, container, false)

        val btnSupportLogin = view.findViewById<Button>(R.id.btnSupportLogin)
        val btnCustomerLogin = view.findViewById<Button>(R.id.btnCustomerLogin)

        btnSupportLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SupportLoginFragment())
                .addToBackStack(null)
                .commit()
        }

        btnCustomerLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CustomerLoginFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    companion object {
        fun newInstance() = LoginChooserFragment()
    }
}
