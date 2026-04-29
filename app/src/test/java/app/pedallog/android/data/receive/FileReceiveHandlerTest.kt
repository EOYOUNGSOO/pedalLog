package app.pedallog.android.data.receive

import app.pedallog.android.data.model.FileFormat
import app.pedallog.android.data.model.ReceivedFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
class FileReceiveHandlerTest {

    @Test
    fun `TCX 확장자 파일 형식 감지`() {
        val format = FileFormat.fromExtension("뚝섬라이딩_2026-04-19.tcx")
        assertEquals(FileFormat.TCX, format)
    }

    @Test
    fun `GPX 확장자 파일 형식 감지`() {
        val format = FileFormat.fromExtension("activity_12345.gpx")
        assertEquals(FileFormat.GPX, format)
    }

    @Test
    fun `FIT 확장자 파일 형식 감지`() {
        val format = FileFormat.fromExtension("2026-04-19-08-30-00.fit")
        assertEquals(FileFormat.FIT, format)
    }

    @Test
    fun `대소문자 구분 없이 감지`() {
        assertEquals(FileFormat.TCX, FileFormat.fromExtension("RIDING.TCX"))
        assertEquals(FileFormat.GPX, FileFormat.fromExtension("TRACK.GPX"))
    }

    @Test
    fun `미지원 형식은 null 반환`() {
        val format = FileFormat.fromExtension("document.pdf")
        assertNull(format)
    }

    @Test
    fun `TCX XML 내용으로 형식 감지`() {
        val content = """
            <?xml version="1.0" encoding="UTF-8"?>
            <TrainingCenterDatabase xmlns="...">
        """.trimIndent().toByteArray()
        assertEquals(FileFormat.TCX, FileFormat.fromFileContent(content))
    }

    @Test
    fun `GPX XML 내용으로 형식 감지`() {
        val content = """
            <?xml version="1.0"?>
            <gpx version="1.1" creator="trimm">
        """.trimIndent().toByteArray()
        assertEquals(FileFormat.GPX, FileFormat.fromFileContent(content))
    }

    @Test
    fun `ReceivedFile 크기 표시 형식 확인`() {
        val file = ReceivedFile(
            file = File("test.tcx"),
            originalName = "test.tcx",
            format = FileFormat.TCX,
            fileSizeBytes = 512 * 1024L
        )
        assertEquals("512.0 KB", file.displaySize)
    }
}
