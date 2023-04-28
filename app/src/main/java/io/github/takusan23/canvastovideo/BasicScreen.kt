package io.github.takusan23.canvastovideo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.takusan23.canvastovideo.encoder.CanvasProcessor
import io.github.takusan23.canvastovideo.encoder.CanvasProcessorHighLevelApi

/** エンコーダーテスト用画面 */
@Composable
fun BasicScreen() {
    val isRunning = remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        // 適当なファイルを作成
        val resultFile = context.getExternalFilesDir(null)?.resolve("${System.currentTimeMillis()}.mp4") ?: return@LaunchedEffect
        val outlinePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            textSize = 80f
        }
        val innerPaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.WHITE
            textSize = 80f
        }
        isRunning.value = true
        // CanvasProcessor / CanvasProcessorHighLevelApi どっちを使うか
        val isUseLowLevelApi = true
        // 描画時に呼び出される関数
        val onCanvasDrawRequest: Canvas.(Long) -> Boolean = { positionMs ->
            // this は Canvas
            drawColor(Color.LTGRAY)
            // positionMs は現在の動画の時間
            val text = "動画の時間 = ${"%.2f".format(positionMs / 1000f)}"
            // 枠取り文字
            drawText(text, 0f, 80f, outlinePaint)
            // 枠無し文字
            drawText(text, 0f, 80f, innerPaint)
            // true を返している間は動画を作成する。とりあえず 10 秒
            positionMs < 10_000
        }
        if (isUseLowLevelApi) {
            CanvasProcessor.start(resultFile, onCanvasDrawRequest = onCanvasDrawRequest)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CanvasProcessorHighLevelApi.start(context, resultFile, onCanvasDrawRequest = onCanvasDrawRequest)
        }
        // コピーして元データを消す
        MediaStoreTool.copyToMovieFolder(context, resultFile)
        resultFile.delete()
        isRunning.value = false
    }

    Text(text = if (isRunning.value) "エンコード中です" else "終わりました")
}