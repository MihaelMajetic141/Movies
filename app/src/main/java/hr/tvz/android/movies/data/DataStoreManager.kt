package hr.tvz.android.movies.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore(name = "user_data")

class DataStoreManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val NAME_KEY = stringPreferencesKey("name")
        private val PICTURE_KEY = stringPreferencesKey("picture")
    }
    suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        email: String,
        name: String,
        picture: String,
    ) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[EMAIL_KEY] = email
            preferences[NAME_KEY] = name
            preferences[PICTURE_KEY] = picture
        }
    }

    val accessToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[ACCESS_TOKEN] ?: "" }

    val refreshToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[REFRESH_TOKEN] ?: "" }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[EMAIL_KEY] ?: "" }

    val userName: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[NAME_KEY] ?: "" }

    val userPicture: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PICTURE_KEY] ?: "" }

    suspend fun clearData() {
        context.dataStore.edit { it.clear() }
    }

}