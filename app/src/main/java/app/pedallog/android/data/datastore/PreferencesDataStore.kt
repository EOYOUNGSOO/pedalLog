package app.pedallog.android.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val NOTION_TOKEN = stringPreferencesKey("notion_token")
        val NOTION_DB_ID = stringPreferencesKey("notion_db_id")
        val ADS_REMOVED = booleanPreferencesKey("ads_removed")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    val notionToken: Flow<String?> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { it[NOTION_TOKEN] }

    val notionDbId: Flow<String?> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { it[NOTION_DB_ID] }

    val adsRemoved: Flow<Boolean> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { it[ADS_REMOVED] ?: false }

    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { it[FIRST_LAUNCH] ?: true }

    suspend fun saveNotionToken(token: String) {
        dataStore.edit { it[NOTION_TOKEN] = token.trim() }
    }

    suspend fun saveNotionDbId(dbId: String) {
        dataStore.edit { it[NOTION_DB_ID] = dbId.trim() }
    }

    suspend fun setAdsRemoved(removed: Boolean) {
        dataStore.edit { it[ADS_REMOVED] = removed }
    }

    suspend fun setFirstLaunchDone() {
        dataStore.edit { it[FIRST_LAUNCH] = false }
    }

    suspend fun clearNotionSettings() {
        dataStore.edit {
            it.remove(NOTION_TOKEN)
            it.remove(NOTION_DB_ID)
        }
    }

    fun isValidToken(token: String): Boolean {
        val trimmed = token.trim()
        val isLegacy = trimmed.startsWith("secret_")
        val isNew = trimmed.startsWith("ntn_")
        return (isLegacy || isNew) && trimmed.length >= 20
    }

    fun isValidDbId(dbId: String): Boolean {
        val cleaned = dbId
            .substringAfterLast("/")
            .substringBefore("?")
            .replace("-", "")
        return cleaned.length == 32
    }

    fun extractDbIdFromUrl(url: String): String {
        return url
            .substringAfterLast("/")
            .substringBefore("?")
            .let {
                if (it.length == 36) it
                else it.replace("-", "").let { raw ->
                    if (raw.length == 32) {
                        "${raw.substring(0, 8)}-${raw.substring(8, 12)}-" +
                            "${raw.substring(12, 16)}-${raw.substring(16, 20)}-" +
                            raw.substring(20)
                    } else {
                        it
                    }
                }
            }
    }
}
