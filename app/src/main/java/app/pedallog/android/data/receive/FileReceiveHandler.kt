package app.pedallog.android.data.receive

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import app.pedallog.android.data.model.FileFormat
import app.pedallog.android.data.model.ReceivedFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileReceiveHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val RECEIVE_DIR = "received"
        private const val MAX_SIZE_BYTES = 50L * 1024 * 1024
        private const val HEADER_READ = 200
    }

    suspend fun handleUri(uri: Uri): Result<ReceivedFile> =
        withContext(Dispatchers.IO) {
            try {
                val originalName = getFileName(uri)
                    ?: "riding_${System.currentTimeMillis()}.tcx"

                val fileSize = getFileSize(uri)
                if (fileSize > MAX_SIZE_BYTES) {
                    return@withContext Result.failure(
                        FileTooLargeException(
                            "파일 크기가 너무 큽니다 (${fileSize / 1024 / 1024}MB). " +
                                "50MB 이하 파일만 지원합니다."
                        )
                    )
                }

                val destFile = copyToCache(uri, originalName)
                val copiedSize = destFile.length()

                val format = detectFormat(uri, originalName, destFile)
                    ?: return@withContext Result.failure(
                        UnsupportedFormatException(
                            "'$originalName' 파일 형식을 지원하지 않습니다.\n" +
                                "지원 형식: TCX, GPX, FIT"
                        )
                    )

                if (copiedSize < 100) {
                    return@withContext Result.failure(
                        InvalidFileException(
                            "파일이 손상되었거나 비어있습니다."
                        )
                    )
                }

                Result.success(
                    ReceivedFile(
                        file = destFile,
                        originalName = originalName,
                        format = format,
                        fileSizeBytes = copiedSize
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun getFileName(uri: Uri): String? {
        context.contentResolver
            .query(uri, null, null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        return cursor.getString(nameIndex)
                    }
                }
            }
        return uri.lastPathSegment
            ?.substringAfterLast("/")
    }

    private fun getFileSize(uri: Uri): Long {
        context.contentResolver
            .query(uri, null, null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        val size = cursor.getLong(sizeIndex)
                        if (size > 0) return size
                    }
                }
            }
        return context.contentResolver.openInputStream(uri)?.use { input ->
            var total = 0L
            val buf = ByteArray(8192)
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                total += n
                if (total > MAX_SIZE_BYTES) return total
            }
            total
        } ?: 0L
    }

    private fun copyToCache(uri: Uri, fileName: String): File {
        val receiveDir = File(context.cacheDir, RECEIVE_DIR).apply {
            if (!exists()) mkdirs()
        }

        cleanOldFiles(receiveDir)

        val destFile = File(
            receiveDir,
            "${System.currentTimeMillis()}_$fileName"
        )

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output, bufferSize = 8192)
            }
        } ?: throw IllegalStateException("파일을 읽을 수 없습니다.")

        return destFile
    }

    private fun detectFormat(
        uri: Uri,
        fileName: String,
        file: File
    ): FileFormat? {
        FileFormat.fromExtension(fileName)?.let { return it }

        val mimeType = context.contentResolver.getType(uri)
        FileFormat.fromMimeType(mimeType)?.let { return it }

        val headerBytes = readFileHeader(file, HEADER_READ)
        return FileFormat.fromFileContent(headerBytes)
    }

    private fun readFileHeader(file: File, maxLen: Int): ByteArray {
        val buffer = ByteArray(maxLen)
        val read = file.inputStream().use { it.read(buffer) }
        return if (read <= 0) ByteArray(0) else buffer.copyOf(read)
    }

    private fun cleanOldFiles(dir: File) {
        val cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        dir.listFiles()
            ?.filter { it.lastModified() < cutoffTime }
            ?.forEach { it.delete() }
    }
}

class UnsupportedFormatException(message: String) : Exception(message)
class FileTooLargeException(message: String) : Exception(message)
class InvalidFileException(message: String) : Exception(message)
