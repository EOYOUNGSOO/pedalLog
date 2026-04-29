package app.pedallog.android.data.model

import java.io.File

data class ReceivedFile(
    val file: File,
    val originalName: String,
    val format: FileFormat,
    val fileSizeBytes: Long,
    val receivedAt: Long = System.currentTimeMillis()
) {
    val fileSizeKb: Double get() = fileSizeBytes / 1024.0
    val fileSizeMb: Double get() = fileSizeKb / 1024.0

    val displaySize: String get() = when {
        fileSizeMb >= 1.0 -> "%.1f MB".format(fileSizeMb)
        else -> "%.1f KB".format(fileSizeKb)
    }

    companion object {
        const val MAX_FILE_SIZE_MB = 50L
    }
}
