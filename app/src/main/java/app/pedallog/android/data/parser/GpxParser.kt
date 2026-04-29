package app.pedallog.android.data.parser

import app.pedallog.android.data.model.ParseResult
import app.pedallog.android.data.model.TrackPointData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpxParser @Inject constructor() {

    suspend fun parse(file: File): Result<ParseResult> = withContext(Dispatchers.IO) {
        try {
            Result.success(parseInternal(file))
        } catch (e: Exception) {
            Result.failure(ParseException("GPX 파싱 실패: ${e.message}", e))
        }
    }

    private fun parseInternal(file: File): ParseResult {
        FileInputStream(file).use { input ->
            val parser = XmlPullParserFactory.newInstance().newPullParser().apply {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                setInput(input, "UTF-8")
            }

            var title = file.nameWithoutExtension
            var startTimeStr: String? = null
            val trackPoints = mutableListOf<TrackPointData>()

            var tpLat = 0.0
            var tpLon = 0.0
            var tpAlt: Double? = null
            var tpTime: Long? = null
            var tpSpeed: Double? = null
            var tpCadence: Int? = null
            var tpHr: Int? = null
            var inTrackPoint = false
            var inExtensions = false
            var currentTag = ""

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name ?: ""
                        when (currentTag.lowercase()) {
                            "trkpt" -> {
                                inTrackPoint = true
                                tpLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull() ?: 0.0
                                tpLon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull() ?: 0.0
                                tpAlt = null
                                tpTime = null
                                tpSpeed = null
                                tpCadence = null
                                tpHr = null
                            }
                            "extensions" -> inExtensions = true
                        }
                    }

                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim().orEmpty()
                        if (text.isEmpty()) {
                            eventType = parser.next()
                            continue
                        }
                        when (currentTag.lowercase()) {
                            "name" -> if (!inTrackPoint) title = text
                            "time" -> {
                                if (!inTrackPoint && startTimeStr == null) {
                                    startTimeStr = text
                                } else if (inTrackPoint) {
                                    tpTime = ParserUtils.parseDate(text)
                                }
                            }
                            "ele" -> tpAlt = text.toDoubleOrNull()
                            "speed" -> if (inExtensions) tpSpeed = text.toDoubleOrNull()?.times(3.6)
                            "cadence", "cad" -> if (inExtensions) tpCadence = text.toIntOrNull()
                            "heartrate", "hr", "heartratebpm" -> if (inExtensions) tpHr = text.toIntOrNull()
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        when (parser.name?.lowercase()) {
                            "trkpt" -> {
                                if (inTrackPoint && tpLat != 0.0 && tpLon != 0.0) {
                                    trackPoints.add(
                                        TrackPointData(
                                            latitude = tpLat,
                                            longitude = tpLon,
                                            altitude = tpAlt,
                                            speedKmh = tpSpeed,
                                            cadence = tpCadence,
                                            heartRate = tpHr,
                                            timestamp = tpTime ?: System.currentTimeMillis()
                                        )
                                    )
                                }
                                inTrackPoint = false
                            }
                            "extensions" -> inExtensions = false
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }

            if (trackPoints.isEmpty()) {
                throw ParseException("TrackPoint 데이터가 없습니다")
            }

            return ParserUtils.buildParseResult(
                title = title,
                startTimeStr = startTimeStr,
                trackPoints = trackPoints,
                format = "GPX"
            )
        }
    }
}
