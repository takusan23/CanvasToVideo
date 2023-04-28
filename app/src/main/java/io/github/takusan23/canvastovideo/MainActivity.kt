package io.github.takusan23.canvastovideo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.github.takusan23.canvastovideo.ui.theme.CanvasToVideoTheme
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
                    MainScreen()
                }
            }
        }
    }

}

inline fun printMeasureTime(block: () -> Unit) {
    val time = measureTimeMillis(block)
    println("時間 $time ms")
}