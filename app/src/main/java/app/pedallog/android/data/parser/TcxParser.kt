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
class TcxParser @Inject constructor() {

    suspend fun parse(file: File): Result<ParseResult> = withContext(Dispatchers.IO) {
        try {
            Result.success(parseInternal(file))
        } catch (e: Exception) {
            Result.failure(ParseException("TCX 파싱 실패: ${e.message}", e))
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
            var totalDistM = 0.0
            var maxSpeedMs = 0.0
            var calories = 0
            var avgHr: Int? = null
            var maxHr: Int? = null
            var avgCad: Int? = null
            val trackPoints = mutableListOf<TrackPointData>()

            var tpLat = 0.0
            var tpLon = 0.0
            var tpAlt: Double? = null
            var tpTime: Long? = null
            var tpHr: Int? = null
            var tpCad: Int? = null
            var tpSpeed: Double? = null

            var inTrackPoint = false
            var inExtensions = false
            var inAvgHr = false
            var inMaxHr = false
            var currentTag = ""

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name ?: ""
                        when (currentTag) {
                            "Trackpoint" -> {
                                inTrackPoint = true
                                tpLat = 0.0
                                tpLon = 0.0
                                tpAlt = null
                                tpTime = null
                                tpHr = null
                                tpCad = null
                                tpSpeed = null
                            }
                            "Extensions" -> inExtensions = true
                            "AverageHeartRateBpm" -> inAvgHr = true
                            "MaximumHeartRateBpm" -> inMaxHr = true
                        }
                    }

                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim().orEmpty()
                        if (text.isEmpty()) {
                            eventType = parser.next()
                            continue
                        }
                        when (currentTag) {
                            "Id" -> if (startTimeStr == null) startTimeStr = text
                            "Name" -> if (!inTrackPoint) title = text
                            "DistanceMeters" -> if (!inTrackPoint) {
                                totalDistM = text.toDoubleOrNull() ?: totalDistM
                            }
                            "MaximumSpeed" -> maxSpeedMs = text.toDoubleOrNull() ?: maxSpeedMs
                            "Calories" -> calories = text.toIntOrNull() ?: calories
                            "Cadence" -> {
                                if (inTrackPoint) {
                                    tpCad = text.toIntOrNull()
                                } else {
                                    avgCad = text.toIntOrNull()
                                }
                            }
                            "Value" -> when {
                                inAvgHr -> avgHr = text.toIntOrNull()
                                inMaxHr -> maxHr = text.toIntOrNull()
                                inTrackPoint -> tpHr = text.toIntOrNull()
                            }
                            "Time" -> if (inTrackPoint) tpTime = ParserUtils.parseDate(text)
                            "LatitudeDegrees" -> tpLat = text.toDoubleOrNull() ?: 0.0
                            "LongitudeDegrees" -> tpLon = text.toDoubleOrNull() ?: 0.0
                            "AltitudeMeters" -> tpAlt = text.toDoubleOrNull()
                            "Speed" -> if (inExtensions) tpSpeed = text.toDoubleOrNull()?.times(3.6)
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "Trackpoint" -> {
                                if (inTrackPoint && tpLat != 0.0 && tpLon != 0.0) {
                                    trackPoints.add(
                                        TrackPointData(
                                            latitude = tpLat,
                                            longitude = tpLon,
                                            altitude = tpAlt,
                                            speedKmh = tpSpeed,
                                            cadence = tpCad,
                                            heartRate = tpHr,
                                            timestamp = tpTime ?: System.currentTimeMillis()
                                        )
                                    )
                                }
                                inTrackPoint = false
                            }
                            "Extensions" -> inExtensions = false
                            "AverageHeartRateBpm" -> inAvgHr = false
                            "MaximumHeartRateBpm" -> inMaxHr = false
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
                format = "TCX",
                totalDistanceM = totalDistM.takeIf { it > 0 },
                calories = calories.takeIf { it > 0 },
                maxSpeedKmh = maxSpeedMs.takeIf { it > 0 }?.times(3.6),
                avgHeartRate = avgHr,
                maxHeartRate = maxHr,
                avgCadence = avgCad
            )
        }
    }
}
