package app.pedallog.android.ui.confirm

import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.dao.RidingTemplateDao
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.data.db.entity.RidingTemplateEntity
import app.pedallog.android.data.notion.NotionRidingProperties
import app.pedallog.android.domain.repository.NotionRepository
import app.pedallog.android.domain.usecase.RegisterToNotionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `세션 로드 시 즐겨찾기 템플릿이 자동 선택된다`() = runTest {
        val session = baseSession()
        val favorite = RidingTemplateEntity(
            id = 10L,
            templateName = "뚝섬라이딩",
            departure = "A",
            destination = "B",
            isFavorite = true
        )
        val other = RidingTemplateEntity(
            id = 11L,
            templateName = "일반",
            isFavorite = false
        )
        val sessionDao = FakeSessionDao(mutableMapOf(1L to session))
        val templateDao = FakeTemplateDao(listOf(other, favorite))
        val useCase = RegisterToNotionUseCase(FakeNotionRepository(), sessionDao)
        val vm = ConfirmViewModel(sessionDao, templateDao, useCase)

        vm.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("뚝섬라이딩", vm.uiState.value.selectedTemplate?.templateName)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `코스 선택 시 세션 스냅샷이 갱신된다`() = runTest {
        val session = baseSession()
        val template = RidingTemplateEntity(
            id = 2L,
            templateName = "구리한강공원",
            departure = "왕숙천교",
            destination = "구리한강공원",
            bikeType = "로드자전거",
            defaultMemo = "한강",
            isFavorite = false
        )
        val sessionDao = FakeSessionDao(mutableMapOf(1L to session))
        val templateDao = FakeTemplateDao(listOf(template))
        val vm = ConfirmViewModel(
            sessionDao,
            templateDao,
            RegisterToNotionUseCase(FakeNotionRepository(), sessionDao)
        )
        vm.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectTemplate(template)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("왕숙천교", vm.uiState.value.session?.departure)
        assertEquals("구리한강공원", vm.uiState.value.session?.destination)
        assertEquals("로드자전거", vm.uiState.value.session?.bikeType)
        assertEquals("한강", vm.uiState.value.editableMemo)
    }

    @Test
    fun `비고 편집 시 DB에 반영된다`() = runTest {
        val session = baseSession()
        val sessionDao = FakeSessionDao(mutableMapOf(1L to session))
        val vm = ConfirmViewModel(
            sessionDao,
            FakeTemplateDao(emptyList()),
            RegisterToNotionUseCase(FakeNotionRepository(), sessionDao)
        )
        vm.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.updateMemo("한강 자전거길")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("한강 자전거길", sessionDao.sessions[1L]?.memo)
        assertEquals("한강 자전거길", vm.uiState.value.editableMemo)
    }

    @Test
    fun `등록 성공 시 SUCCESS 상태`() = runTest {
        val session = baseSession()
        val sessionDao = FakeSessionDao(mutableMapOf(1L to session))
        val vm = ConfirmViewModel(
            sessionDao,
            FakeTemplateDao(emptyList()),
            RegisterToNotionUseCase(FakeNotionRepository(successPageId = "page-abc"), sessionDao)
        )
        vm.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.registerToNotion()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.registerState is ConfirmViewModel.RegisterState.SUCCESS)
        assertEquals("page-abc", vm.uiState.value.notionPageId)
    }

    @Test
    fun `등록 실패 시 ERROR 상태`() = runTest {
        val session = baseSession()
        val sessionDao = FakeSessionDao(mutableMapOf(1L to session))
        val vm = ConfirmViewModel(
            sessionDao,
            FakeTemplateDao(emptyList()),
            RegisterToNotionUseCase(
                FakeNotionRepository(shouldFail = true, failMessage = "Token 미설정"),
                sessionDao
            )
        )
        vm.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.registerToNotion()
        testDispatcher.scheduler.advanceUntilIdle()

        val err = vm.uiState.value.registerState as? ConfirmViewModel.RegisterState.ERROR
        assertTrue(err != null)
        assertTrue(err!!.message.contains("Token"))
    }

    @Test
    fun `clearError 후 IDLE`() = runTest {
        val session = baseSession()
        val sessionDao = FakeSessionDao(mutableMapOf(1L to session))
        val vm = ConfirmViewModel(
            sessionDao,
            FakeTemplateDao(emptyList()),
            RegisterToNotionUseCase(
                FakeNotionRepository(shouldFail = true, failMessage = "오류"),
                sessionDao
            )
        )
        vm.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.registerToNotion()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.clearError()

        assertTrue(vm.uiState.value.registerState is ConfirmViewModel.RegisterState.IDLE)
    }

    private fun baseSession() = RidingSessionEntity(
        id = 1L,
        title = "라이딩",
        startTime = 1_700_000_000_000L,
        endTime = 1_700_000_003_360_000L,
        totalDistanceM = 5000.0,
        avgSpeedKmh = 22.5,
        maxSpeedKmh = 30.0,
        sourceFormat = "GPX"
    )

    private class FakeSessionDao(
        val sessions: MutableMap<Long, RidingSessionEntity?>
    ) : RidingSessionDao {
        override fun getAllSessions(): Flow<List<RidingSessionEntity>> = emptyFlow()
        override suspend fun getSessionById(id: Long): RidingSessionEntity? = sessions[id]
        override suspend fun insert(session: RidingSessionEntity): Long {
            val id = session.id.takeIf { it != 0L } ?: 1L
            sessions[id] = session.copy(id = id)
            return id
        }

        override suspend fun update(session: RidingSessionEntity) {
            sessions[session.id] = session
        }

        override suspend fun delete(session: RidingSessionEntity) = Unit
        override suspend fun updateNotionResult(id: Long, pageId: String, registeredAt: Long) = Unit
        override fun getUnregisteredSessions(): Flow<List<RidingSessionEntity>> = emptyFlow()
        override suspend fun getTotalCount(): Int = sessions.size
    }

    private class FakeTemplateDao(
        private val templates: List<RidingTemplateEntity>
    ) : RidingTemplateDao {
        override fun getAllTemplates(): Flow<List<RidingTemplateEntity>> = flowOf(templates)
        override fun getFavoriteTemplates(): Flow<List<RidingTemplateEntity>> = emptyFlow()
        override suspend fun getTemplateById(id: Long): RidingTemplateEntity? =
            templates.find { it.id == id }

        override suspend fun upsert(template: RidingTemplateEntity): Long = error("unused")
        override suspend fun delete(template: RidingTemplateEntity) = Unit
        override suspend fun deleteById(id: Long) = Unit
        override suspend fun updateFavorite(id: Long, isFavorite: Boolean) = Unit
        override suspend fun updateSortOrder(id: Long, sortOrder: Int) = Unit
        override suspend fun updateAllSortOrders(idOrderMap: Map<Long, Int>) = Unit
        override suspend fun getTotalCount(): Int = templates.size
    }

    private class FakeNotionRepository(
        private val successPageId: String = "page-1",
        private val shouldFail: Boolean = false,
        private val failMessage: String = "fail"
    ) : NotionRepository {
        override suspend fun registerRiding(
            sessionId: Long,
            properties: NotionRidingProperties,
            routeImageFile: File?
        ): Result<String> {
            return if (shouldFail) Result.failure(Exception(failMessage))
            else Result.success(successPageId)
        }

        override suspend fun validateConnection(): Result<String> = Result.success("ok")
    }
}
