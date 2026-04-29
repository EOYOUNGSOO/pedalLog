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
            val databasePropertyNames = fetchDatabasePropertyNames(databaseId, token)
                .getOrElse {
                    logWarn("DB 속성 조회 실패 — 기본 매핑으로 진행: ${it.message}")
                    emptySet()
                }

            val body = buildPageJson(
                databaseId = databaseId,
                p = properties,
                databasePropertyNames = databasePropertyNames
            )
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

    /**
     * Notion 페이지를 아카이브(휴지통) 처리합니다.
     * Notion API는 완전 삭제를 지원하지 않으므로 archived=true 로 처리합니다.
     */
    suspend fun archivePage(
        pageId: String,
        token: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        retryWithBackoff(MAX_RETRY) {
            val body = JSONObject()
                .put("archived", true)
                .toString()
                .toRequestBody(JSON_MEDIA)

            val request = Request.Builder()
                .url("$_apiRoot/pages/$pageId")
                .notionHeaders(token)
                .patch(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.parseOrThrow()
            logDebug("페이지 아카이브 완료: $pageId")
            Unit
        }
    }

    /**
     * 기존 Notion 페이지의 속성을 업데이트합니다 (수정 후 재전송용).
     */
    suspend fun updateRidingPage(
        pageId: String,
        token: String,
        properties: NotionRidingProperties
    ): Result<Unit> = withContext(Dispatchers.IO) {
        retryWithBackoff(MAX_RETRY) {
            val body = JSONObject()
                .put("properties", buildPageJson("", properties).getJSONObject("properties"))
                .toString()
                .toRequestBody(JSON_MEDIA)

            val request = Request.Builder()
                .url("$_apiRoot/pages/$pageId")
                .notionHeaders(token)
                .patch(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.parseOrThrow()
            logDebug("페이지 업데이트 완료: $pageId")
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
        p: NotionRidingProperties,
        databasePropertyNames: Set<String> = emptySet()
    ): JSONObject {
        val props = JSONObject()

        fun key(vararg aliases: String): String? {
            if (databasePropertyNames.isEmpty()) return aliases.firstOrNull()
            return aliases.firstOrNull { it in databasePropertyNames }
        }

        fun putMapped(vararg aliases: String, value: JSONObject) {
            val resolved = key(*aliases) ?: aliases.firstOrNull()
            if (resolved != null) {
                props.put(resolved, value)
            }
        }

        props.apply {
            putMapped("라이딩명", "제목", value = titleProp(p.title))
            putMapped("날짜", "라이딩일자", value = dateProp(p.date))
            putMapped("라이딩시작시간", "시작시간", value = richTextProp(p.startTimeStr))
            putMapped("라이딩종료시간", "종료시간", value = richTextProp(p.endTimeStr))
            putMapped("거리 (km)", "거리", value = numberProp(p.distanceKm))
            putMapped("시간 (분)", "시간", value = numberProp(p.durationMin))
            putMapped("평균속도 (km/h)", "평균속도", value = numberProp(p.avgSpeedKmh))
            putMapped("파일 형식", "파일형식", value = selectProp(p.sourceFormat))
            putMapped("연동 앱", "연동앱", value = selectProp(p.sourceApp))

            p.maxSpeedKmh?.let { putMapped("최고속도 (km/h)", "최고속도", value = numberProp(it)) }
            p.avgCadence?.let { putMapped("케이던스 (rpm)", "케이던스", value = numberProp(it.toDouble())) }
            p.elevationUp?.let { putMapped("상승고도 (m)", "상승고도", value = numberProp(it)) }
            p.calories?.let { putMapped("소비칼로리", "소비칼로리 (kcal)", value = numberProp(it.toDouble())) }
            p.avgHeartRate?.let { putMapped("평균심박수 (bpm)", "평균심박수", value = numberProp(it.toDouble())) }
            p.maxHeartRate?.let { putMapped("최고심박수 (bpm)", "최고심박수", value = numberProp(it.toDouble())) }
            p.avgPower?.let { putMapped("파워 (W)", "파워", value = numberProp(it.toDouble())) }
            p.departure?.let { putMapped("출발지", value = richTextProp(it)) }
            p.waypoints?.let { putMapped("경유지", value = richTextProp(it)) }
            p.destination?.let { putMapped("목적지", value = richTextProp(it)) }
            p.bikeType?.let { putMapped("자전거 종류", "자전거종류", value = selectProp(it)) }
            p.memo?.let { putMapped("비고", "메모", value = richTextProp(it)) }
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

    private suspend fun fetchDatabasePropertyNames(
        databaseId: String,
        token: String
    ): Result<Set<String>> = retryWithBackoff(1) {
        val request = Request.Builder()
            .url("$_apiRoot/databases/$databaseId")
            .notionHeaders(token)
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        val json = response.parseOrThrow()
        val props = json.optJSONObject("properties") ?: JSONObject()
        props.keys().asSequence().toSet()
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
