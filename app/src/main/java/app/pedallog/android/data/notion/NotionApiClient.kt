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
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class NotionApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @Named("notion_api_base") private val baseUrl: String
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://api.notion.com/v1"

        private const val NOTION_VERSION = "2022-06-28"
        private const val MAX_RETRY = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    private val apiRoot: String = baseUrl.trimEnd('/')

    suspend fun uploadFile(
        imageFile: File,
        token: String
    ): Result<String> = withContext(Dispatchers.IO) {
        retryWithBackoff(MAX_RETRY) {
            val createBody = JSONObject()
                .put("name", imageFile.name)
                .put("content_type", "image/png")
                .toString()
                .toRequestBody(JSON_MEDIA)

            val createRequest = Request.Builder()
                .url("$apiRoot/file_uploads")
                .addNotionJsonHeaders(token)
                .post(createBody)
                .build()

            val createResponse = okHttpClient.newCall(createRequest).execute()
            val createJson = createResponse.parseBodyOrThrow()
            val fileUploadId = createJson.getString("id")

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    imageFile.name,
                    imageFile.asRequestBody(PNG_MEDIA)
                )
                .build()

            val uploadRequest = Request.Builder()
                .url("$apiRoot/file_uploads/$fileUploadId/send")
                .addNotionAuthHeaders(token)
                .post(multipartBody)
                .build()

            val uploadResponse = okHttpClient.newCall(uploadRequest).execute()
            uploadResponse.parseBodyOrThrow()

            fileUploadId
        }
    }

    suspend fun createRidingPage(
        databaseId: String,
        token: String,
        properties: NotionRidingProperties
    ): Result<String> = withContext(Dispatchers.IO) {
        retryWithBackoff(MAX_RETRY) {
            val body = buildPageBody(databaseId, properties)
                .toString()
                .toRequestBody(JSON_MEDIA)

            val request = Request.Builder()
                .url("$apiRoot/pages")
                .addNotionJsonHeaders(token)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val json = response.parseBodyOrThrow()
            json.getString("id")
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
                .url("$apiRoot/blocks/$pageId/children")
                .addNotionJsonHeaders(token)
                .patch(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.parseBodyOrThrow()
            Unit
        }
    }

    suspend fun validateToken(token: String): Result<String> =
        withContext(Dispatchers.IO) {
            retryWithBackoff(1) {
                val request = Request.Builder()
                    .url("$apiRoot/users/me")
                    .addNotionJsonHeaders(token)
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val json = response.parseBodyOrThrow()
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
                .url("$apiRoot/databases/$databaseId")
                .addNotionJsonHeaders(token)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val json = response.parseBodyOrThrow()
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
                val waitMs = RETRY_DELAY_MS * (attempt + 1) * 3
                delay(waitMs)
                lastException = e
            } catch (e: IOException) {
                val waitMs = RETRY_DELAY_MS * (1 shl attempt)
                delay(waitMs)
                lastException = e
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
        return Result.failure(lastException ?: Exception("Unknown error"))
    }

    private fun Request.Builder.addNotionAuthHeaders(token: String): Request.Builder =
        this
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Notion-Version", NOTION_VERSION)

    private fun Request.Builder.addNotionJsonHeaders(token: String): Request.Builder =
        addNotionAuthHeaders(token)
            .addHeader("Content-Type", "application/json")

    private fun Response.parseBodyOrThrow(): JSONObject {
        val bodyString = body?.use { it.string() }.orEmpty()
        val json = try {
            if (bodyString.isBlank()) JSONObject() else JSONObject(bodyString)
        } catch (_: Exception) {
            JSONObject().put("message", bodyString)
        }

        when (code) {
            200, 201 -> return json
            401 -> throw NotionUnauthorizedException(
                "Notion Token이 올바르지 않습니다. 설정에서 토큰을 확인해주세요."
            )
            404 -> throw NotionNotFoundException(
                "Notion Database를 찾을 수 없습니다. Database ID를 확인해주세요."
            )
            429 -> throw NotionRateLimitException(
                "Notion API 요청 한도 초과. 잠시 후 자동 재시도합니다."
            )
            else -> {
                val message = json.optString("message", "알 수 없는 오류")
                throw NotionApiException(code, message)
            }
        }
    }

    private fun buildPageBody(
        databaseId: String,
        p: NotionRidingProperties
    ): JSONObject {
        val props = JSONObject()

        props.put("라이딩명", titleProp(p.title))
        props.put("날짜", dateProp(p.date))
        props.put("거리 (km)", numberProp(p.distanceKm))
        props.put("시간 (분)", numberProp(p.durationMin))
        props.put("평균속도 (km/h)", numberProp(p.avgSpeedKmh))
        p.maxSpeedKmh?.let { props.put("최고속도 (km/h)", numberProp(it)) }
        p.avgCadence?.let { props.put("케이던스 (rpm)", numberProp(it.toDouble())) }
        p.elevationUp?.let { props.put("상승고도 (m)", numberProp(it)) }
        p.calories?.let { props.put("소비칼로리 (kcal)", numberProp(it.toDouble())) }
        p.avgHeartRate?.let { props.put("평균심박수 (bpm)", numberProp(it.toDouble())) }
        p.maxHeartRate?.let { props.put("최고심박수 (bpm)", numberProp(it.toDouble())) }
        p.avgPower?.let { props.put("파워 (W)", numberProp(it.toDouble())) }
        p.departure?.let { props.put("출발지", richTextProp(it)) }
        p.waypoints?.let { props.put("경유지", richTextProp(it)) }
        p.destination?.let { props.put("목적지", richTextProp(it)) }
        p.bikeType?.let { props.put("자전거 종류", selectProp(it)) }
        props.put("파일 형식", selectProp(p.sourceFormat))
        props.put("연동 앱", selectProp(p.sourceApp))
        p.memo?.let { props.put("비고", richTextProp(it)) }

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
        JSONObject().put("number", value)

    private fun dateProp(isoDate: String): JSONObject =
        JSONObject()
            .put("date", JSONObject().put("start", isoDate))

    private fun selectProp(name: String): JSONObject =
        JSONObject()
            .put("select", JSONObject().put("name", name))
}

class NotionApiException(val code: Int, message: String) :
    Exception("Notion API 오류 ($code): $message")

class NotionUnauthorizedException(message: String) : Exception(message)

class NotionNotFoundException(message: String) : Exception(message)

class NotionRateLimitException(message: String) : Exception(message)

private val JSON_MEDIA = "application/json".toMediaType()
private val PNG_MEDIA = "image/png".toMediaType()
