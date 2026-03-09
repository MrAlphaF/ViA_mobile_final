package com.group3.financialapplication.data

import android.content.Context
import android.content.Intent
import android.appwidget.AppWidgetManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.group3.financialapplication.widget.BudgetWidgetReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

data class AppSettings(
    val monthlyLimit: Double,
    val currency: String,
    val notificationsEnabled: Boolean
)

class SettingsManager(private val context: Context) {

    companion object {
        val MONTHLY_LIMIT         = doublePreferencesKey("monthly_limit")
        val CURRENCY              = stringPreferencesKey("currency")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val CURRENCIES = listOf("€", "$", "£", "¥", "CHF")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            monthlyLimit         = prefs[MONTHLY_LIMIT] ?: 1000.0,
            currency             = prefs[CURRENCY] ?: "€",
            notificationsEnabled = prefs[NOTIFICATIONS_ENABLED] ?: true
        )
    }

    suspend fun setMonthlyLimit(limit: Double) {
        context.settingsDataStore.edit { it[MONTHLY_LIMIT] = limit }
        pingWidget()
    }

    suspend fun setCurrency(currency: String) {
        context.settingsDataStore.edit { it[CURRENCY] = currency }
        pingWidget()
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    private fun pingWidget() {
        val intent = Intent(context, BudgetWidgetReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        context.sendBroadcast(intent)
    }
}