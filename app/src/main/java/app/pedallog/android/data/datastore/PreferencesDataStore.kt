package app.pedallog.android.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    }

    val notionToken: Flow<String?> = dataStore.data.map { it[NOTION_TOKEN] }
    val notionDbId: Flow<String?> = dataStore.data.map { it[NOTION_DB_ID] }
    val adsRemoved: Flow<Boolean> = dataStore.data.map { it[ADS_REMOVED] ?: false }

    suspend fun saveNotionToken(token: String) {
        dataStore.edit { it[NOTION_TOKEN] = token }
    }

    suspend fun saveNotionDbId(dbId: String) {
        dataStore.edit { it[NOTION_DB_ID] = dbId }
    }

    suspend fun setAdsRemoved(removed: Boolean) {
        dataStore.edit { it[ADS_REMOVED] = removed }
    }
}
