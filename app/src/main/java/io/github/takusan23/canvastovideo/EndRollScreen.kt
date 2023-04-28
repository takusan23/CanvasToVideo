package io.github.takusan23.canvastovideo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import io.github.takusan23.canvastovideo.encoder.CanvasProcessor
import kotlinx.coroutines.launch

private const val VIDEO_DURATION_MS = 20_000
private const val VIDEO_WIDTH = 1280
private const val VIDEO_HEIGHT = 720

/** エンドロールをつくる */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndRollScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // 実行中フラグ
    val isRunning = remember { mutableStateOf(false) }

    // エンドロールの文字
    val endRollText = remember { mutableStateOf("") }

    // 画像
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    DisposableEffect(key1 = Unit) {
        onDispose { bitmap.value?.recycle() } // Bitmap を破棄する
    }
    val imagePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        val uri = it ?: return@rememberLauncherForActivityResult
        bitmap.value = createBitmapFromUri(context, uri).aspectResize(200, 200)
    }

    // エンコードする
    fun encode() {
        scope.launch {
            isRunning.value = true
            val resultFile = context.getExternalFilesDir(null)?.resolve("${System.currentTimeMillis()}.mp4")!!
            val bitmapPaint = Paint()
            val textPaint = Paint().apply {
                style = Paint.Style.FILL
                color = Color.WHITE
                textSize = 50f
            }

            CanvasProcessor.start(
                resultFile = resultFile,
                outputVideoWidth = VIDEO_WIDTH,
                outputVideoHeight = VIDEO_HEIGHT
            ) { positionMs ->

                // 背景は真っ黒
                drawColor(Color.BLACK)

                // アイコンを描く
                var textLeftPos = 100f
                bitmap.value?.also { bitmap ->
                    drawBitmap(bitmap, textLeftPos, ((VIDEO_HEIGHT / 2f) - (bitmap.height / 2f)), bitmapPaint)
                    textLeftPos += bitmap.width
                }

                // 適当に移動させる
                // 複数行描けないので行単位で drawText する
                endRollText.value.lines().forEachIndexed { index, text ->
                    drawText(text, textLeftPos + 100f, ((VIDEO_HEIGHT + (textPaint.textSize * (index + 1))) - (positionMs / 10f)), textPaint)
                }

                positionMs < VIDEO_DURATION_MS
            }

            // コピーして元データを消す
            MediaStoreTool.copyToMovieFolder(context, resultFile)
            resultFile.delete()

            isRunning.value = false
        }
    }

    Column {
        // 選択ボタン
        bitmap.value?.also {
            Text(text = "Bitmap width = ${it.width}")
        }
        Button(
            modifier = Modifier.padding(10.dp),
            onClick = { imagePicker.launch("image/*") }
        ) { Text(text = "画像を選ぶ") }

        // 文字
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            value = endRollText.value,
            onValueChange = { endRollText.value = it },
            label = { Text(text = "エンドロールのテキスト") }
        )

        Spacer(modifier = Modifier.height(50.dp))

        // 実行中はエンコードボタンを塞ぐ
        if (isRunning.value) {
            Text(text = "エンコード中です")
        } else {
            Button(
                modifier = Modifier.padding(10.dp),
                onClick = { encode() }
            ) { Text(text = "エンコードする") }
        }
    }
}

/** アスペクト比を保持してリサイズする */
private fun Bitmap.aspectResize(targetWidth: Int, targetHeight: Int): Bitmap {
    val width = width
    val height = height
    val aspectBitmap = width.toFloat() / height.toFloat()
    val aspectTarget = targetWidth.toFloat() / targetHeight.toFloat()
    var calcWidth = targetWidth
    var calcHeight = targetHeight
    if (aspectTarget > aspectBitmap) {
        calcWidth = (targetHeight.toFloat() * aspectBitmap).toInt()
    } else {
        calcHeight = (targetWidth.toFloat() / aspectBitmap).toInt()
    }
    return scale(calcWidth, calcHeight, true)
}

/** Uri から Bitmap を作る。 */
private fun createBitmapFromUri(context: Context, uri: Uri): Bitmap {
    // これだと ハードウェア Bitmap が出来てしまうので、 SOFTWARE をつけて、ソフトウェア Bitmap を作る必要がある（編集可能）
    // API が Android 9 以降なので、古いバージョンをサポートしたい場合は古い方法を使うか、いっその事画像を読み込むライブラリ Glide とかを入れるのもありだと思います
    return ImageDecoder.createSource(context.contentResolver, uri)
        .let { src -> ImageDecoder.decodeDrawable(src) { decoder, info, s -> decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE } }
        .toBitmap()
}