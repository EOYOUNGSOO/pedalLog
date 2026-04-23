package app.pedalLog.android.data.db.prepopulate

import app.pedalLog.android.data.db.entity.BikeTypeEntity
import app.pedalLog.android.data.db.entity.RidingTemplateEntity

object DefaultTemplates {
    fun templates(): List<RidingTemplateEntity> = listOf(
        template("출근 코스 A", "집", "회사", listOf("하천길"), true, 0),
        template("출근 코스 B", "집", "회사", listOf("공원"), false, 1),
        template("퇴근 코스", "회사", "집", listOf("강변"), true, 2),
        template("주말 장거리 1", "집", "북한강", listOf("양수역"), false, 3),
        template("주말 장거리 2", "집", "남한강", listOf("이포보"), false, 4),
        template("회복 라이딩", "집", "근린공원", emptyList(), false, 5),
        template("야간 라이딩", "집", "한강공원", listOf("반포"), false, 6),
        template("인터벌 코스", "집", "언덕구간", listOf("업힐 3회"), false, 7),
        template("카페 라이딩", "집", "카페", listOf("강변북로"), false, 8),
        template("동호회 집결", "집", "집결지", listOf("편의점"), true, 9),
        template("가벼운 출사", "집", "호수공원", emptyList(), false, 10),
        template("시내 순환", "집", "시청", listOf("광화문"), false, 11),
        template("브런치 라이딩", "집", "브런치카페", listOf("성수"), false, 12),
        template("테스트 코스", "집", "테스트 지점", listOf("중간지점"), false, 13)
    )

    fun bikeTypes(): List<BikeTypeEntity> = listOf(
        BikeTypeEntity(name = "로드", sortOrder = 0),
        BikeTypeEntity(name = "MTB", sortOrder = 1),
        BikeTypeEntity(name = "하이브리드", sortOrder = 2)
    )

    private fun template(
        courseName: String,
        departure: String,
        destination: String,
        waypoints: List<String>,
        isFavorite: Boolean,
        sortOrder: Int
    ): RidingTemplateEntity = RidingTemplateEntity(
        courseName = courseName,
        departure = departure,
        destination = destination,
        waypoints = waypoints,
        bikeType = "로드",
        defaultNote = "",
        isFavorite = isFavorite,
        sortOrder = sortOrder
    )
}
