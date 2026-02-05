package com.supportticketapp.presentation

import android.content.Context
import android.content.SharedPreferences
import com.supportticketapp.presentation.auth.UserRole

object UserPreferences {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_CUSTOMER_PHONE = "customer_phone"
    private const val KEY_FCM_TOKEN = "fcm_token"
    private const val KEY_CURRENT_USER_ROLE = "current_user_role"
    private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"
    private const val KEY_DARK_MODE_PREFERENCE = "dark_mode_preference"
    private const val PREF_SYSTEM = "system"
    private const val PREF_LIGHT = "light"
    private const val PREF_DARK = "dark"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()
    }

    fun getCustomerPhone(context: Context): String {
        return getPrefs(context).getString(KEY_CUSTOMER_PHONE, "").orEmpty()
    }

    fun setCustomerPhone(context: Context, phone: String) {
        getPrefs(context).edit()
            .putString(KEY_CUSTOMER_PHONE, phone)
            .apply()
    }

    fun setFcmToken(context: Context, token: String) {
        getPrefs(context).edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
    }

    fun getFcmToken(context: Context): String {
        return getPrefs(context).getString(KEY_FCM_TOKEN, "").orEmpty()
    }

    fun getCurrentUserRole(context: Context): UserRole {
        val roleName = getPrefs(context).getString(KEY_CURRENT_USER_ROLE, null)
        return try {
            UserRole.valueOf(roleName ?: "NONE")
        } catch (e: IllegalArgumentException) {
            UserRole.NONE
        }
    }

    fun setCurrentUserRole(context: Context, role: UserRole) {
        getPrefs(context).edit()
            .putString(KEY_CURRENT_USER_ROLE, role.name)
            .apply()
    }

    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    /** @return "system" (seguir configuración del celular), "light" o "dark" */
    fun getDarkModePreference(context: Context): String {
        val prefs = getPrefs(context)
        if (prefs.contains(KEY_DARK_MODE_PREFERENCE)) {
            return prefs.getString(KEY_DARK_MODE_PREFERENCE, PREF_SYSTEM).orEmpty()
                .takeIf { it in listOf(PREF_SYSTEM, PREF_LIGHT, PREF_DARK) } ?: PREF_SYSTEM
        }
        // Migrar preferencia antigua (boolean) a tres estados
        val oldDark = prefs.getBoolean(KEY_DARK_MODE_ENABLED, false)
        return if (oldDark) PREF_DARK else PREF_SYSTEM
    }

    fun setDarkModePreference(context: Context, value: String) {
        getPrefs(context).edit()
            .putString(KEY_DARK_MODE_PREFERENCE, value)
            .remove(KEY_DARK_MODE_ENABLED)
            .apply()
    }

    fun isDarkModeEnabled(context: Context): Boolean {
        return getDarkModePreference(context) == PREF_DARK
    }

    fun setDarkModeEnabled(context: Context, enabled: Boolean) {
        setDarkModePreference(context, if (enabled) PREF_DARK else PREF_SYSTEM)
    }
}
