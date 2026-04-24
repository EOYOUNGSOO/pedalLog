package app.pedallog.android.data.notion

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotionApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: NotionApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val base = mockWebServer.url("/").toString().removeSuffix("/")
        val okHttpClient = OkHttpClient.Builder().build()
        apiClient = NotionApiClient(okHttpClient, base)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `유효하지 않은 Token은 UnauthorizedException 반환`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"message":"Unauthorized"}""")
        )
        val result = apiClient.validateToken("invalid_token")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NotionUnauthorizedException)
    }

    @Test
    fun `createRidingPage는 201 응답 시 page id 반환`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody("""{"id":"page-123","object":"page"}""")
        )
        val props = NotionRidingProperties(
            title = "뚝섬라이딩",
            date = "2026-04-19",
            distanceKm = 30.0,
            durationMin = 68.0,
            avgSpeedKmh = 26.3,
            sourceFormat = "TCX"
        )
        val result = apiClient.createRidingPage("db-uuid", "secret_x", props)
        assertTrue(
            "expected success, was ${result.exceptionOrNull()?.message}",
            result.isSuccess
        )
        assertEquals("page-123", result.getOrNull())
    }

    @Test
    fun `createRidingPage는 429가 연속이면 재시도 후 실패`() = runBlocking {
        repeat(3) {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(429)
                    .setBody("""{"message":"Rate limited"}""")
            )
        }
        val props = NotionRidingProperties(
            title = "뚝섬라이딩",
            date = "2026-04-19",
            distanceKm = 30.0,
            durationMin = 68.0,
            avgSpeedKmh = 26.3,
            sourceFormat = "TCX"
        )
        val result = apiClient.createRidingPage("db-uuid", "secret_x", props)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NotionRateLimitException)
    }

    @Test
    fun `NotionRidingProperties 기본 필드`() {
        val props = NotionRidingProperties(
            title = "뚝섬라이딩",
            date = "2026-04-19",
            distanceKm = 30.0,
            durationMin = 68.0,
            avgSpeedKmh = 26.3,
            sourceFormat = "TCX"
        )
        assertEquals("뚝섬라이딩", props.title)
        assertEquals(30.0, props.distanceKm, 0.01)
        assertNull(props.avgHeartRate)
    }
}
