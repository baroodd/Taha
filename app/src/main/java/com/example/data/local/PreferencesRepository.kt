package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.CurrencyOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "bunyan_settings")

class PreferencesRepository(private val context: Context) {

    private val currencyKey = stringPreferencesKey("preferred_currency")

    val preferredCurrency: Flow<CurrencyOption> = context.dataStore.data.map { prefs ->
        val code = prefs[currencyKey] ?: CurrencyOption.SAR.code
        CurrencyOption.entries.firstOrNull { it.code == code } ?: CurrencyOption.SAR
    }

    suspend fun setPreferredCurrency(currency: CurrencyOption) {
        context.dataStore.edit { prefs ->
            prefs[currencyKey] = currency.code
        }
    }
}
