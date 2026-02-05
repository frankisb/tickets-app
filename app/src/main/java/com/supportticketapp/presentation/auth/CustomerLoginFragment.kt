package com.supportticketapp.presentation.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.supportticketapp.R
import com.supportticketapp.presentation.screen.CustomerTicketsFragment
import com.supportticketapp.presentation.UserPreferences
import com.supportticketapp.presentation.auth.AuthManager
import com.supportticketapp.presentation.auth.UserRole
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class CustomerLoginFragment : Fragment() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signInLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Solo mostrar cuentas previamente usadas para iniciar sesión en tu app
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    AuthManager.firebaseAuthWithGoogle(idToken) { success ->
                        if (success) {
                            AuthManager.setCurrentRole(requireContext(), UserRole.CUSTOMER)
                            Toast.makeText(requireContext(), "Bienvenido cliente", Toast.LENGTH_SHORT).show()

                            // Obtener y guardar token FCM
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    UserPreferences.setFcmToken(requireContext(), token)
                                    // Opcional: guardar token en Firestore bajo el usuario
                                }
                            }

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, CustomerTicketsFragment.newInstance())
                                .commit()
                        } else {
                            view?.findViewById<TextView>(R.id.tvError)?.text = "Error al autenticar con Google"
                        }
                    }
                } else {
                    view?.findViewById<TextView>(R.id.tvError)?.text = "No se obtuvo token de Google"
                }
            } catch (e: ApiException) {
                Log.e("CustomerLoginFragment", "Google sign-in failed", e)
                view?.findViewById<TextView>(R.id.tvError)?.text = "Error al iniciar sesión con Google"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_customer_login, container, false)

        val tvError = view.findViewById<TextView>(R.id.tvError)

        val btnSignIn = view.findViewById<SignInButton>(R.id.btnSignInGoogle)
        btnSignIn.setSize(SignInButton.SIZE_WIDE)
        btnSignIn.setColorScheme(SignInButton.COLOR_AUTO)

        btnSignIn.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        signInLauncher.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        Log.e("CustomerLoginFragment", "Couldn't start One Tap UI", e)
                        tvError.text = "No se pudo iniciar el flujo de Google"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CustomerLoginFragment", "OneTap failed", e)
                    tvError.text = "Error al iniciar sesión con Google"
                    view.findViewById<TextView>(R.id.tvError).text = "Error al preparar inicio de sesión"
                }
        }

        return view
    }

    companion object {
        fun newInstance() = CustomerLoginFragment()
    }
}
