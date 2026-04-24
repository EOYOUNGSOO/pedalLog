package app.pedallog.android.domain.usecase

import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.data.notion.NotionRidingProperties
import app.pedallog.android.domain.repository.NotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RegisterToNotionUseCaseTest {

    @Test
    fun `세션이 없으면 실패`() = runTest {
        val dao = FakeSessionDao(mapOf())
        val repo = FakeNotionRepository()
        val useCase = RegisterToNotionUseCase(repo, dao)
        val result = useCase(99L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("세션을 찾을 수 없습니다") == true)
    }

    @Test
    fun `이미 Notion 등록된 세션이면 실패`() = runTest {
        val session = RidingSessionEntity(
            id = 1L,
            title = "테스트",
            startTime = 0L,
            endTime = 60_000L,
            totalDistanceM = 1000.0,
            avgSpeedKmh = 20.0,
            maxSpeedKmh = 25.0,
            sourceFormat = "TCX",
            notionPageId = "already"
        )
        val dao = FakeSessionDao(mapOf(1L to session))
        val useCase = RegisterToNotionUseCase(FakeNotionRepository(), dao)
        val result = useCase(1L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("이미 Notion") == true)
    }

    @Test
    fun `미등록 세션이면 Repository 호출`() = runTest {
        val session = RidingSessionEntity(
            id = 2L,
            title = "라이딩",
            startTime = 1_700_000_000_000L,
            endTime = 1_700_000_003_600_000L,
            totalDistanceM = 5000.0,
            avgSpeedKmh = 22.5,
            maxSpeedKmh = 30.0,
            sourceFormat = "GPX"
        )
        val dao = FakeSessionDao(mapOf(2L to session))
        val repo = FakeNotionRepository()
        val useCase = RegisterToNotionUseCase(repo, dao)
        val result = useCase(2L)
        assertTrue(result.isSuccess)
        assertEquals("new-page-id", result.getOrNull())
        assertEquals(2L, repo.lastSessionId)
        assertEquals("라이딩", repo.lastProperties?.title)
        assertEquals("GPX", repo.lastProperties?.sourceFormat)
    }

    private class FakeSessionDao(
        private val sessions: Map<Long, RidingSessionEntity?>
    ) : RidingSessionDao {
        override fun getAllSessions(): Flow<List<RidingSessionEntity>> = emptyFlow()
        override suspend fun getSessionById(id: Long): RidingSessionEntity? = sessions[id]
        override suspend fun insert(session: RidingSessionEntity): Long = error("unused")
        override suspend fun update(session: RidingSessionEntity) = error("unused")
        override suspend fun delete(session: RidingSessionEntity) = error("unused")
        override suspend fun updateNotionResult(id: Long, pageId: String, registeredAt: Long) = error("unused")
        override fun getUnregisteredSessions(): Flow<List<RidingSessionEntity>> = emptyFlow()
        override suspend fun getTotalCount(): Int = 0
    }

    private class FakeNotionRepository : NotionRepository {
        var lastSessionId: Long? = null
        var lastProperties: NotionRidingProperties? = null
        var lastFile: File? = null

        override suspend fun registerRiding(
            sessionId: Long,
            properties: NotionRidingProperties,
            routeImageFile: File?
        ): Result<String> {
            lastSessionId = sessionId
            lastProperties = properties
            lastFile = routeImageFile
            return Result.success("new-page-id")
        }

        override suspend fun validateConnection(): Result<String> =
            Result.success("ok")
    }
}
