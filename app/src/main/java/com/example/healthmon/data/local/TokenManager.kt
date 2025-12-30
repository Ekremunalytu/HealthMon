package com.example.healthmon.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type")
        private val USER_ID_KEY = stringPreferencesKey("user_id") // General User ID
        private val PATIENT_ID_KEY = stringPreferencesKey("patient_id")
        private val CAREGIVER_ID_KEY = stringPreferencesKey("caregiver_id")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }
    
    val userType: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_TYPE_KEY]
    }
    
    val patientId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PATIENT_ID_KEY]
    }
    
    val caregiverId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[CAREGIVER_ID_KEY]
    }

    suspend fun saveAuthData(
        token: String,
        userType: String,
        userId: String?,
        patientId: String?,
        caregiverId: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_TYPE_KEY] = userType
            userId?.let { prefs[USER_ID_KEY] = it }
            patientId?.let { prefs[PATIENT_ID_KEY] = it }
            caregiverId?.let { prefs[CAREGIVER_ID_KEY] = it }
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
