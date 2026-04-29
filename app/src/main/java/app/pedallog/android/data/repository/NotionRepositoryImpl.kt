package app.pedallog.android.data.repository

import android.util.Log
import app.pedallog.android.data.datastore.PreferencesDataStore
import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.notion.NotionApiClient
import app.pedallog.android.data.notion.NotionRidingProperties
import app.pedallog.android.domain.repository.NotionRepository
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotionRepositoryImpl @Inject constructor(
    private val apiClient: NotionApiClient,
    private val dataStore: PreferencesDataStore,
    private val sessionDao: RidingSessionDao
) : NotionRepository {

    override suspend fun registerRiding(
        sessionId: Long,
        properties: NotionRidingProperties,
        routeImageFile: File?
    ): Result<String> {
        val token = dataStore.notionToken.first()
            ?: return Result.failure(
                Exception("Notion Token이 설정되지 않았습니다.\n설정 화면에서 토큰을 입력해주세요.")
            )

        val dbId = dataStore.notionDbId.first()
            ?: return Result.failure(
                Exception("Notion Database ID가 설정되지 않았습니다.\n설정 화면에서 DB ID를 입력해주세요.")
            )

        return try {
            val fileUploadId: String? = if (routeImageFile?.exists() == true) {
                val uploadId = apiClient.createFileUploadSession(
                    fileName = routeImageFile.name,
                    token = token
                ).getOrElse {
                    Log.w("NotionRepo", "업로드 세션 생성 실패 — 이미지 없이 진행: ${it.message}")
                    null
                }
                if (uploadId != null) {
                    apiClient.sendFileUpload(
                        fileUploadId = uploadId,
                        imageFile = routeImageFile,
                        token = token
                    ).getOrElse {
                        Log.w("NotionRepo", "파일 전송 실패 — 이미지 없이 진행: ${it.message}")
                        null
                    }
                }
                uploadId
            } else {
                null
            }

            val notionPageId = apiClient.createRidingPage(
                databaseId = dbId,
                token = token,
                properties = properties
            ).getOrElse { return Result.failure(it) }

            if (fileUploadId != null) {
                apiClient.appendImageBlock(
                    pageId = notionPageId,
                    token = token,
                    fileUploadId = fileUploadId
                ).getOrElse {
                    Log.w("NotionRepo", "이미지 블록 첨부 실패 (무시): ${it.message}")
                }
            }

            sessionDao.updateNotionResult(
                id = sessionId,
                pageId = notionPageId,
                registeredAt = System.currentTimeMillis()
            )

            Log.d("NotionRepo", "Notion 등록 완료: $notionPageId")
            Result.success(notionPageId)
        } catch (e: Exception) {
            Log.e("NotionRepo", "Notion 등록 실패: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun validateConnection(): Result<String> {
        val token = dataStore.notionToken.first()
            ?: return Result.failure(Exception("Token 미설정"))
        val dbId = dataStore.notionDbId.first()
            ?: return Result.failure(Exception("Database ID 미설정"))

        apiClient.validateToken(token).getOrElse { return Result.failure(it) }

        val dbResult = apiClient.validateDatabase(dbId, token)
        if (dbResult.isFailure) return dbResult

        val dbTitle = dbResult.getOrNull() ?: "라이딩 기록부"
        return Result.success("연결 성공 — DB: $dbTitle")
    }
}
