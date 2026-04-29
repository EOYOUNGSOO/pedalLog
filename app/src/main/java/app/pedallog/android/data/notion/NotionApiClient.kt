package app.pedallog.android.data.notion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotionApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://api.notion.com/v1"
        private const val TAG = "NotionApiClient"
        private const val NOTION_VERSION = "2022-06-28"
        private const val MAX_RETRY = 3
        private const val RETRY_BASE_MS = 1000L
    }

    private val apiRoot: String = DEFAULT_BASE_URL

    constructor(
        okHttpClient: OkHttpClient,
        baseUrl: String
    ) : this(okHttpClient) {
        this._apiRoot = baseUrl.trimEnd('/')
    }

    private var _apiRoot: String = apiRoot

    suspend fun createFileUploadSession(
        fileName: String,
        token: String
    ): Result<String> = withContext(Dispatchers.IO) {
        retryWithBackoff(MAX_RETRY) {
            val body = JSONObject()
                .put("name", fileName)
                .put("content_type", "image/png")
                .toString()
                .toRequestBody(JSON_MEDIA)

            val request = Request.Builder()
                .url("$_apiRoot/file_uploads")
                .notionHeaders(token)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val json = response.parseOrThrow()
            logDebug("파일 업로드 세션 생성: ${json.getString("id")}")
            json.getString("id")
        }
    }

    suspend fun sendFileUpload(
        fileUploadId: String,
        imageFile: File,
        token: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        retryWithBackoff(MAX_RETRY) {
            val multipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    imageFile.name,
                    imageFile.asRequestBody(PNG_MEDIA)
                )
                .build()

            val request = Request.Builder()
                .url("$_apiRoot/file_uploads/$fileUploadId/send")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Notion-Version", NOTION_VERSION)
                .post(multipart)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val json = response.parseOrThrow()
            val status = json.optString("status")
            if (status != "uploaded") {
                throw NotionApiException(200, "파일 업로드 상태 이상: $status")
            }
            logDebug("파일 업로드 완료: $fileUploadId ($status)")
            Unit
        }
    }

    suspend fun createRidingPage(
        databaseId: String,
        token: String,
        properties: NotionRidingProperties
    ): Result<String> = withContext(Dispatchers.IO) {
        retryWithBackoff(MAX_RETRY) {
            val body = buildPageJson(databaseId, properties)
                .toString()
                .toRequestBody(JSON_MEDIA)

            val request = Request.Builder()
                .url("$_apiRoot/pages")
                .notionHeaders(token)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val json = response.parseOrThrow()
            val pageId = json.getString("id")
            logDebug("페이지 생성 완료: $pageId")
            pageId
        }
    }

    suspend fun appendImageBlock(
        pageId: String,
        token: String,
        fileUploadId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        retryWithBackoff(MAX_RETRY) {
            val body = JSONObject()
                .put(
                    "children",
                    JSONArray().put(
                        JSONObject()
                            .put("object", "block")
                            .put("type", "image")
                            .put(
                                "image",
                                JSONObject()
                                    .put("type", "file_upload")
                                    .put(
                                        "file_upload",
                                        JSONObject().put("id", fileUploadId)
                                    )
                            )
                    )
                )
                .toString()
                .toRequestBody(JSON_MEDIA)

            val request = Request.Builder()
                .url("$_apiRoot/blocks/$pageId/children")
                .notionHeaders(token)
                .patch(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.parseOrThrow()
            logDebug("이미지 블록 첨부 완료: pageId=$pageId")
            Unit
        }
    }

    suspend fun validateToken(token: String): Result<String> =
        withContext(Dispatchers.IO) {
            retryWithBackoff(1) {
                val request = Request.Builder()
                    .url("$_apiRoot/users/me")
                    .notionHeaders(token)
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val json = response.parseOrThrow()
                json.optJSONObject("bot")
                    ?.optJSONObject("owner")
                    ?.optJSONObject("user")
                    ?.optString("name", "Notion Bot")
                    ?: "연결 성공"
            }
        }

    suspend fun validateDatabase(
        databaseId: String,
        token: String
    ): Result<String> = withContext(Dispatchers.IO) {
        retryWithBackoff(1) {
            val request = Request.Builder()
                .url("$_apiRoot/databases/$databaseId")
                .notionHeaders(token)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val json = response.parseOrThrow()
            json.optJSONArray("title")
                ?.optJSONObject(0)
                ?.optJSONObject("text")
                ?.optString("content", "라이딩 기록부")
                ?: "라이딩 기록부"
        }
    }

    private suspend fun <T> retryWithBackoff(
        maxRetry: Int,
        block: suspend () -> T
    ): Result<T> {
        var lastException: Exception? = null
        repeat(maxRetry) { attempt ->
            try {
                return Result.success(block())
            } catch (e: NotionRateLimitException) {
                val waitMs = RETRY_BASE_MS * (attempt + 1) * 3
                logWarn("Rate Limit — ${waitMs}ms 대기 후 재시도")
                delay(waitMs)
                lastException = e
            } catch (e: IOException) {
                val waitMs = RETRY_BASE_MS * (1 shl attempt)
                logWarn("네트워크 오류 — ${waitMs}ms 대기 후 재시도")
                delay(waitMs)
                lastException = e
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
        return Result.failure(lastException ?: Exception("알 수 없는 오류"))
    }

    private fun Request.Builder.notionHeaders(token: String): Request.Builder =
        this
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Notion-Version", NOTION_VERSION)
            .addHeader("Content-Type", "application/json")

    private fun Response.parseOrThrow(): JSONObject {
        val bodyString = body?.string() ?: throw IOException("빈 응답")
        val json = JSONObject(bodyString)
        logDebug("응답 코드: $code")
        when (code) {
            200, 201 -> return json
            400 -> {
                val msg = json.optString("message", "잘못된 요청")
                throw NotionBadRequestException(
                    "Notion DB 속성명을 확인해주세요.\n$msg"
                )
            }
            401 -> throw NotionUnauthorizedException(
                "Notion Token이 올바르지 않습니다.\n설정에서 토큰을 확인해주세요."
            )
            404 -> throw NotionNotFoundException(
                "Notion Database를 찾을 수 없습니다.\nDatabase ID를 확인해주세요."
            )
            429 -> throw NotionRateLimitException(
                "Notion API 요청 한도 초과.\n잠시 후 자동 재시도합니다."
            )
            else -> {
                val message = json.optString("message", "알 수 없는 오류")
                throw NotionApiException(code, message)
            }
        }
    }

    private fun buildPageJson(
        databaseId: String,
        p: NotionRidingProperties
    ): JSONObject {
        val props = JSONObject().apply {
            put("라이딩명", titleProp(p.title))
            put("날짜", dateProp(p.date))
            put("거리 (km)", numberProp(p.distanceKm))
            put("시간 (분)", numberProp(p.durationMin))
            put("평균속도 (km/h)", numberProp(p.avgSpeedKmh))
            put("파일 형식", selectProp(p.sourceFormat))
            put("연동 앱", selectProp(p.sourceApp))

            p.maxSpeedKmh?.let { put("최고속도 (km/h)", numberProp(it)) }
            p.avgCadence?.let { put("케이던스 (rpm)", numberProp(it.toDouble())) }
            p.elevationUp?.let { put("상승고도 (m)", numberProp(it)) }
            p.calories?.let { put("소비칼로리 (kcal)", numberProp(it.toDouble())) }
            p.avgHeartRate?.let { put("평균심박수 (bpm)", numberProp(it.toDouble())) }
            p.maxHeartRate?.let { put("최고심박수 (bpm)", numberProp(it.toDouble())) }
            p.avgPower?.let { put("파워 (W)", numberProp(it.toDouble())) }
            p.departure?.let { put("출발지", richTextProp(it)) }
            p.waypoints?.let { put("경유지", richTextProp(it)) }
            p.destination?.let { put("목적지", richTextProp(it)) }
            p.bikeType?.let { put("자전거 종류", selectProp(it)) }
            p.memo?.let { put("비고", richTextProp(it)) }
        }
        return JSONObject()
            .put(
                "parent",
                JSONObject()
                    .put("type", "database_id")
                    .put("database_id", databaseId)
            )
            .put("properties", props)
    }

    private fun titleProp(text: String): JSONObject =
        JSONObject()
            .put(
                "title",
                JSONArray().put(
                    JSONObject().put("text", JSONObject().put("content", text))
                )
            )

    private fun richTextProp(text: String): JSONObject =
        JSONObject()
            .put(
                "rich_text",
                JSONArray().put(
                    JSONObject().put("text", JSONObject().put("content", text))
                )
            )

    private fun numberProp(value: Double): JSONObject =
        JSONObject().put("number", Math.round(value * 10) / 10.0)

    private fun dateProp(isoDate: String): JSONObject =
        JSONObject()
            .put("date", JSONObject().put("start", isoDate))

    private fun selectProp(name: String): JSONObject =
        JSONObject()
            .put("select", JSONObject().put("name", name))

    private fun logDebug(message: String) {
        runCatching {
            android.util.Log.d(TAG, message)
        }
    }

    private fun logWarn(message: String) {
        runCatching {
            android.util.Log.w(TAG, message)
        }
    }
}

class NotionApiException(val code: Int, message: String) :
    Exception("Notion API 오류 ($code): $message")

class NotionBadRequestException(message: String) : Exception(message)

class NotionUnauthorizedException(message: String) : Exception(message)

class NotionNotFoundException(message: String) : Exception(message)

class NotionRateLimitException(message: String) : Exception(message)

private val JSON_MEDIA = "application/json".toMediaType()
private val PNG_MEDIA = "image/png".toMediaType()
