package app.pedallog.android.data.parser

import app.pedallog.android.data.model.FileFormat
import app.pedallog.android.data.model.ParseResult
import app.pedallog.android.data.model.ReceivedFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RidingFileParser @Inject constructor(
    private val gpxParser: GpxParser,
    private val tcxParser: TcxParser
) {
    suspend fun parse(receivedFile: ReceivedFile): Result<ParseResult> {
        return when (receivedFile.format) {
            FileFormat.GPX -> gpxParser.parse(receivedFile.file)
            FileFormat.TCX -> tcxParser.parse(receivedFile.file)
            FileFormat.FIT -> {
                Result.failure(
                    ParseException(
                        "FIT 형식은 아직 지원하지 않습니다.\nTCX 또는 GPX 파일로 내보내기 해주세요."
                    )
                )
            }
        }
    }
}
