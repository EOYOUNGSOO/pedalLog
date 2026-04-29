package app.pedallog.android.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Typeface
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Singleton
class OsmDroidRouteImageGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) : RouteImageGenerator {

    companion object {
        private const val IMAGE_SIZE = 1024
        private const val PADDING_RATIO = 0.10f
        private const val PATH_WIDTH = 6f
        private const val MARKER_RADIUS = 18f
        private const val MARKER_STROKE = 4f

        private val COLOR_BG = Color.parseColor("#1A1A1A")
        private val COLOR_PATH = Color.parseColor("#F5C500")
        private val COLOR_PATH_GLOW = Color.parseColor("#80F5C500")
        private val COLOR_START = Color.parseColor("#2ECC71")
        private val COLOR_END = Color.parseColor("#E74C3C")
        private val COLOR_GRID = Color.parseColor("#2A2A2A")
    }

    override suspend fun generate(
        sessionId: Long,
        trackPoints: List<LatLng>
    ): File? = withContext(Dispatchers.Default) {
        if (trackPoints.size < 2) return@withContext null
        try {
            val outputDir = File(context.filesDir, "route_images").apply {
                if (!exists()) mkdirs()
            }
            val outputFile = File(outputDir, "$sessionId.png")

            val bitmap = Bitmap.createBitmap(
                IMAGE_SIZE,
                IMAGE_SIZE,
                Bitmap.Config.ARGB_8888
            )

            try {
                val canvas = Canvas(bitmap)
                drawBackground(canvas)
                val pixels = projectToPixels(trackPoints)
                drawRoutePath(canvas, pixels)
                drawMarkers(canvas, pixels)

                FileOutputStream(outputFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            } finally {
                bitmap.recycle()
            }

            outputFile
        } catch (e: Exception) {
            Log.e("RouteImage", "경로 이미지 생성 실패: ${e.message}", e)
            null
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(COLOR_BG)

        val gridPaint = Paint().apply {
            color = COLOR_GRID
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val gridStep = IMAGE_SIZE / 8f
        for (i in 1..7) {
            val pos = i * gridStep
            canvas.drawLine(pos, 0f, pos, IMAGE_SIZE.toFloat(), gridPaint)
            canvas.drawLine(0f, pos, IMAGE_SIZE.toFloat(), pos, gridPaint)
        }
    }

    private fun projectToPixels(points: List<LatLng>): List<PointF> {
        val padding = IMAGE_SIZE * PADDING_RATIO
        val minLat = points.minOf { it.latitude }
        val maxLat = points.maxOf { it.latitude }
        val minLon = points.minOf { it.longitude }
        val maxLon = points.maxOf { it.longitude }

        val latRange = max(maxLat - minLat, 0.001)
        val lonRange = max(maxLon - minLon, 0.001)
        val drawSize = IMAGE_SIZE - (padding * 2)

        val scaleX = drawSize / lonRange
        val scaleY = drawSize / latRange
        val scale = min(scaleX, scaleY)

        val offsetX = (IMAGE_SIZE - lonRange * scale) / 2
        val offsetY = (IMAGE_SIZE - latRange * scale) / 2

        return points.map { point ->
            PointF(
                ((point.longitude - minLon) * scale + offsetX).toFloat(),
                ((maxLat - point.latitude) * scale + offsetY).toFloat()
            )
        }
    }

    private fun drawRoutePath(canvas: Canvas, pixels: List<PointF>) {
        if (pixels.size < 2) return
        val path = Path().apply {
            moveTo(pixels.first().x, pixels.first().y)
            for (i in 1 until pixels.size) {
                val dx = abs(pixels[i].x - pixels[i - 1].x)
                val dy = abs(pixels[i].y - pixels[i - 1].y)
                if (dx < IMAGE_SIZE * 0.3f && dy < IMAGE_SIZE * 0.3f) {
                    lineTo(pixels[i].x, pixels[i].y)
                } else {
                    moveTo(pixels[i].x, pixels[i].y)
                }
            }
        }

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PATH_GLOW
            strokeWidth = PATH_WIDTH * 3f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            maskFilter = BlurMaskFilter(PATH_WIDTH * 2f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(path, glowPaint)

        val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PATH
            strokeWidth = PATH_WIDTH
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(path, pathPaint)
    }

    private fun drawMarkers(canvas: Canvas, pixels: List<PointF>) {
        drawMarker(canvas, pixels.first(), COLOR_START, "S")
        drawMarker(canvas, pixels.last(), COLOR_END, "E")
    }

    private fun drawMarker(canvas: Canvas, point: PointF, color: Int, label: String) {
        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(point.x, point.y, MARKER_RADIUS + MARKER_STROKE, outerPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = COLOR_BG
            style = Paint.Style.FILL
        }
        canvas.drawCircle(point.x, point.y, MARKER_RADIUS + MARKER_STROKE * 0.5f, borderPaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawCircle(point.x, point.y, MARKER_RADIUS, fillPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = MARKER_RADIUS * 1.1f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val textY = point.y - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(label, point.x, textY, textPaint)
    }
}
