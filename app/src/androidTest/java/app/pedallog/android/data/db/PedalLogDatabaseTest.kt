package app.pedallog.android.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.dao.RidingTemplateDao
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.data.db.entity.RidingTemplateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PedalLogDatabaseTest {

    private lateinit var db: PedalLogDatabase
    private lateinit var sessionDao: RidingSessionDao
    private lateinit var templateDao: RidingTemplateDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PedalLogDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionDao = db.ridingSessionDao()
        templateDao = db.ridingTemplateDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndReadSession() = runBlocking {
        val session = RidingSessionEntity(
            title = "뚝섬라이딩",
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + 3600000L,
            totalDistanceM = 30000.0,
            avgSpeedKmh = 26.3,
            maxSpeedKmh = 38.2,
            sourceFormat = "TCX"
        )
        val insertedId = sessionDao.insert(session)
        val retrieved = sessionDao.getSessionById(insertedId)

        assertNotNull(retrieved)
        assertEquals("뚝섬라이딩", retrieved!!.title)
        assertEquals(30000.0, retrieved.totalDistanceM, 0.01)
    }

    @Test
    fun templateFavoriteAndSortOrder() = runBlocking {
        templateDao.upsert(
            RidingTemplateEntity(
                templateName = "뚝섬라이딩",
                isFavorite = true,
                sortOrder = 0
            )
        )
        templateDao.upsert(
            RidingTemplateEntity(
                templateName = "구리한강공원",
                isFavorite = false,
                sortOrder = 1
            )
        )

        val templates = templateDao.getAllTemplates().first()
        assertEquals(2, templates.size)
        assertEquals("뚝섬라이딩", templates[0].templateName)
        assertTrue(templates[0].isFavorite)
    }

    @Test
    fun notionPageIdUpdate() = runBlocking {
        val id = sessionDao.insert(
            RidingSessionEntity(
                title = "테스트라이딩",
                startTime = 0L,
                endTime = 1000L,
                totalDistanceM = 1000.0,
                avgSpeedKmh = 20.0,
                maxSpeedKmh = 25.0,
                sourceFormat = "TCX"
            )
        )
        val registeredAt = System.currentTimeMillis()
        sessionDao.updateNotionResult(id, "notion-page-abc-123", registeredAt)
        val updated = sessionDao.getSessionById(id)
        assertEquals("notion-page-abc-123", updated!!.notionPageId)
        assertNotNull(updated.notionRegisteredAt)
    }
}
