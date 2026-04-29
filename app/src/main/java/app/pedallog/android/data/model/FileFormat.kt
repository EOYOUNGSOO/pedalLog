package app.pedallog.android.data.model

enum class FileFormat(
    val extensions: List<String>,
    val mimeTypes: List<String>,
    val displayName: String,
    val description: String
) {
    TCX(
        extensions = listOf("tcx"),
        mimeTypes = listOf(
            "application/vnd.garmin.tcx+xml",
            "application/xml",
            "text/xml",
            "application/octet-stream"
        ),
        displayName = "TCX",
        description = "Training Center XML\n(trimm Cycling, 구형 가민)"
    ),
    GPX(
        extensions = listOf("gpx"),
        mimeTypes = listOf(
            "application/gpx+xml",
            "application/xml",
            "text/xml",
            "application/octet-stream"
        ),
        displayName = "GPX",
        description = "GPS Exchange Format\n(브라이튼, IGPSPORT, 와후)"
    ),
    FIT(
        extensions = listOf("fit"),
        mimeTypes = listOf(
            "application/vnd.ant.fit",
            "application/octet-stream"
        ),
        displayName = "FIT",
        description = "Flexible & Interoperable Data\n(가민 Edge, 와후 고급형)"
    );

    companion object {
        fun fromExtension(fileName: String): FileFormat? {
            val ext = fileName
                .substringAfterLast(".", "")
                .lowercase()
            return entries.find { ext in it.extensions }
        }

        fun fromMimeType(mimeType: String?): FileFormat? {
            if (mimeType == null) return null
            val lower = mimeType.lowercase()
            return when {
                lower.contains("tcx") -> TCX
                lower.contains("gpx") -> GPX
                lower.contains("fit") -> FIT
                else -> null
            }
        }

        fun fromFileContent(bytes: ByteArray): FileFormat? {
            if (bytes.isEmpty()) return null
            val header = String(bytes.take(200).toByteArray(), Charsets.UTF_8)
                .trim()
                .uppercase()
            return when {
                header.contains("<TRAININGCENTERDATABASE") ||
                    header.contains("<TCX") -> TCX

                header.contains("<GPX") -> GPX

                bytes.size >= 14 &&
                    bytes[8] == 0x2E.toByte() &&
                    bytes[9] == 0x46.toByte() &&
                    bytes[10] == 0x49.toByte() &&
                    bytes[11] == 0x54.toByte() -> FIT

                else -> null
            }
        }
    }
}
