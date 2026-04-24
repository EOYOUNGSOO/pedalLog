package app.pedallog.android.data.notion

import app.pedallog.android.data.datastore.PreferencesDataStore
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotionApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val preferencesDataStore: PreferencesDataStore
) {
    companion object {
        private const val BASE_URL = "https://api.notion.com/v1"
        private const val NOTION_VERSION = "2022-06-28"
    }

    suspend fun uploadFile(imageFile: File, token: String): String {
        TODO("Task #10 implementation")
    }

    suspend fun createRidingPage(
        databaseId: String,
        token: String,
        properties: Map<String, Any>
    ): String {
        TODO("Task #10 implementation")
    }

    suspend fun attachImageBlock(
        pageId: String,
        token: String,
        fileUploadId: String
    ) {
        TODO("Task #10 implementation")
    }
}
