package app.pedallog.android.data.parser

import app.pedallog.android.data.model.TrackPointData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GpxParserTest {

    private val parser = GpxParser()

    @Test
    fun `trimm GPX 파일 파싱 성공`() {
        runBlocking {
            val file = createTempFile(
                suffix = ".gpx"
            ).apply {
                writeText(
                    """
                    <gpx>
                      <metadata><time>2026-04-05T07:38:00Z</time></metadata>
                      <trk>
                        <name>뚝섬라이딩</name>
                        <trkseg>
                          <trkpt lat="37.5665" lon="126.9780">
                            <ele>10.0</ele>
                            <time>2026-04-05T07:38:05Z</time>
                            <extensions>
                              <speed>7.2</speed>
                              <cadence>88</cadence>
                              <heartrate>142</heartrate>
                            </extensions>
                          </trkpt>
                          <trkpt lat="37.5670" lon="126.9790">
                            <ele>12.0</ele>
                            <time>2026-04-05T07:38:15Z</time>
                            <extensions>
                              <speed>8.2</speed>
                              <cadence>90</cadence>
                              <heartrate>145</heartrate>
                            </extensions>
                          </trkpt>
                        </trkseg>
                      </trk>
                    </gpx>
                    """.trimIndent()
                )
            }

            val result = parser.parse(file)
            assertTrue(result.isSuccess)

            val data = result.getOrThrow()
            assertTrue(data.totalDistanceM > 0)
            assertTrue(data.avgSpeedKmh > 0)
            assertTrue(data.trackPoints.isNotEmpty())
            assertTrue(data.startTime < data.endTime)
            assertEquals("GPX", data.sourceFormat)

            file.delete()
        }
    }

    @Test
    fun `TrackPoint GPS 좌표 정상 추출`() {
        runBlocking {
            val file = createTempFile(suffix = ".gpx").apply {
                writeText(
                    """
                    <gpx>
                      <trk><trkseg>
                        <trkpt lat="37.123" lon="127.456">
                          <time>2026-04-05T07:38:05Z</time>
                        </trkpt>
                      </trkseg></trk>
                    </gpx>
                    """.trimIndent()
                )
            }

            val result = parser.parse(file).getOrThrow()
            val firstPoint = result.trackPoints.first()
            assertTrue(firstPoint.latitude in 33.0..43.0)
            assertTrue(firstPoint.longitude in 124.0..132.0)

            file.delete()
        }
    }

    @Test
    fun `Haversine 거리 계산 정확도`() {
        val points = listOf(
            TrackPointData(37.5666, 126.9784, null, null, null, null, 0L),
            TrackPointData(37.5474, 127.0659, null, null, null, null, 1000L)
        )
        val dist = ParserUtils.calcDistance(points)
        assertTrue(dist in 7000.0..9000.0)
    }

    @Test
    fun `상승 고도 계산 - 0_5m 이하 노이즈 제거`() {
        val points = listOf(
            TrackPointData(0.0, 0.0, 10.0, null, null, null, 0L),
            TrackPointData(0.0, 0.0, 10.3, null, null, null, 1000L),
            TrackPointData(0.0, 0.0, 11.0, null, null, null, 2000L),
            TrackPointData(0.0, 0.0, 12.5, null, null, null, 3000L)
        )
        val elevation = ParserUtils.calcElevation(points)
        assertTrue(elevation in 2.0..2.5)
    }
}
