package app.pedallog.android.data.db.migrations

// Phase 2: Health Connect 연동 (avgHeartRate, maxHeartRate는 이미 v1에 nullable로 포함)
// → riding_sessions에 이미 포함되어 있어 해당 컬럼만으로는 Migration 불필요

// Phase 3: FIT 파워 (avgPower, maxPower는 이미 v1에 nullable로 포함)
// → riding_sessions에 이미 포함되어 있어 해당 컬럼만으로는 Migration 불필요

// 향후 새 컬럼 추가 시 Migration 예시
/*
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE riding_sessions ADD COLUMN newColumn TEXT"
        )
    }
}
*/
