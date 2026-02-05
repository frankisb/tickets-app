package com.supportticketapp.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.supportticketapp.R
import com.supportticketapp.presentation.auth.AuthManager
import com.supportticketapp.presentation.auth.LoginChooserFragment
import com.supportticketapp.presentation.auth.UserRole
import com.supportticketapp.presentation.screen.CustomerTicketsFragment
import com.supportticketapp.presentation.screen.WorkingTicketFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // modo oscuro segun configuracion del sistema
        applyDarkMode()
        
        setContentView(R.layout.activity_main)

        // inicializa el canal de notificaciones
        NotificationHelper.ensureChannel(this)

        if (savedInstanceState == null) {
            when (AuthManager.getCurrentRole(this)) {
                UserRole.SUPPORT -> {
                    loadFragment(WorkingTicketFragment())
                }
                UserRole.CUSTOMER -> {
                    loadFragment(CustomerTicketsFragment.newInstance())
                }
                UserRole.NONE -> {
                    loadFragment(LoginChooserFragment.newInstance())
                }
            }
        }
    }

    private fun applyDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }
}
