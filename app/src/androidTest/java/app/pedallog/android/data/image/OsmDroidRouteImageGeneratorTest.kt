package app.pedallog.android.data.image

import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class OsmDroidRouteImageGeneratorTest {

    @Test
    fun `좌표 배열로 1024 이미지 생성`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val generator = OsmDroidRouteImageGenerator(context)

        val points = listOf(
            LatLng(37.5474, 127.0659),
            LatLng(37.5512, 127.0823),
            LatLng(37.5589, 127.1012),
            LatLng(37.5632, 127.1234),
            LatLng(37.5701, 127.1456)
        )

        val file = generator.generate(sessionId = 987654321L, trackPoints = points)
        assertNotNull(file)
        assertTrue(file!!.exists())
        assertTrue(file.length() > 0L)

        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        assertEquals(1024, bitmap.width)
        assertEquals(1024, bitmap.height)
        bitmap.recycle()
        file.delete()
    }

    @Test
    fun `트랙포인트 2개 미만은 null`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val generator = OsmDroidRouteImageGenerator(context)

        val file = generator.generate(
            sessionId = 100L,
            trackPoints = listOf(LatLng(37.5, 127.0))
        )
        assertNull(file)
    }
}
