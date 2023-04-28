package io.github.takusan23.canvastovideo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.takusan23.canvastovideo.encoder.CanvasProcessor
import io.github.takusan23.canvastovideo.encoder.CanvasProcessorHighLevelApi
import io.github.takusan23.canvastovideo.ui.theme.CanvasToVideoTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
                        // ベンチマーク用
                        GlobalScope.launch {
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
                            printMeasureTime {
                                val resultFile = getExternalFilesDir(null)?.resolve("${System.currentTimeMillis()}.mp4")!!
                                println("CanvasProcessor")
                                CanvasProcessor.start(resultFile, onCanvasDrawRequest = onCanvasDrawRequest)
                            }
                            printMeasureTime {
                                val resultFile = getExternalFilesDir(null)?.resolve("${System.currentTimeMillis()}.mp4")!!
                                println("CanvasProcessorHighLevelApi")
                                CanvasProcessorHighLevelApi.start(this@MainActivity, resultFile, onCanvasDrawRequest = onCanvasDrawRequest)
                            }
                        }
        */


        setContent {
            CanvasToVideoTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // MainScreen()
                    // SlideShowScreen()
                    EndRollScreen()
                }
            }
        }
    }

}

inline fun printMeasureTime(block: () -> Unit) {
    val time = measureTimeMillis(block)
    println("時間 $time ms")
}


// TODO 別に Compose を使う必要はない。
@Composable
fun MainScreen() {
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