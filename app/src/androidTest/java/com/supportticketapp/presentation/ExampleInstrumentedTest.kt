package com.supportticketapp.presentation

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba instrumentada que se ejecuta en un dispositivo Android.
 *
 * Verifica que el nombre del paquete de la aplicación sea correcto.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.supportticketapp", appContext.packageName)
    }

    @Test
    fun testUserPreferencesInstrumented() {
        // Context de la aplicación bajo prueba
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Limpiar preferencias antes de la prueba
        UserPreferences.clear(appContext)
        
        // Probar guardar y obtener preferencias de notificaciones
        UserPreferences.setNotificationsEnabled(appContext, true)
        val notificationsEnabled = UserPreferences.isNotificationsEnabled(appContext)
        assertEquals(true, notificationsEnabled)
        
        // Probar guardar y obtener teléfono de cliente
        val testPhone = "1234567890"
        UserPreferences.setCustomerPhone(appContext, testPhone)
        val savedPhone = UserPreferences.getCustomerPhone(appContext)
        assertEquals(testPhone, savedPhone)
        
        // Probar guardar y obtener token FCM
        val testToken = "test_fcm_token_123"
        UserPreferences.setFcmToken(appContext, testToken)
        val savedToken = UserPreferences.getFcmToken(appContext)
        assertEquals(testToken, savedToken)
        
        // Limpiar después de la prueba
        UserPreferences.clear(appContext)
    }
}
