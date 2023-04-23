package io.github.takusan23.canvastovideo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
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

private const val TIME_MS = 3_000
private const val VIDEO_WIDTH = 1280
private const val VIDEO_HEIGHT = 720

/** スライドショーを作る */
@Composable
fun SlideShowScreen() {
    val context = LocalContext.current
    val isRunning = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // 画像を取得する
    val imageList = remember { mutableStateListOf<Uri>() }
    val imagePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) {
        imageList.addAll(it)
    }

    // エンコードする
    @RequiresApi(Build.VERSION_CODES.P)
    fun encode() {
        scope.launch {
            isRunning.value = true
            val resultFile = context.getExternalFilesDir(null)?.resolve("${System.currentTimeMillis()}.mp4")!!
            val paint = Paint()

            var prevBitmapPathPair: Pair<Uri, Bitmap>? = null
            CanvasProcessor.start(
                resultFile = resultFile,
                outputVideoWidth = VIDEO_WIDTH,
                outputVideoHeight = VIDEO_HEIGHT
            ) { positionMs ->
                // 再生位置から表示すべき画像の配列の添え字を計算
                val index = (positionMs / TIME_MS).toInt()
                val uri = imageList.getOrNull(index) ?: return@start false
                // 前回と違う画像の場合
                if (prevBitmapPathPair?.first != uri) {
                    // ハードウェア Bitmap だと、Canvas に描画出来ないため、ソフトウェア Bitmap を作る
                    val bitmap = createBitmapFromUri(context, uri).aspectResize(VIDEO_WIDTH, VIDEO_HEIGHT)
                    // 前の Bitmap を破棄してから
                    prevBitmapPathPair?.second?.recycle()
                    prevBitmapPathPair = uri to bitmap
                }

                // 真ん中に Bitmap
                val bitmap = prevBitmapPathPair!!.second
                drawBitmap(bitmap, ((VIDEO_WIDTH / 2f) - (bitmap.width / 2f)), 0f, paint)

                // true を返している間
                positionMs < TIME_MS * imageList.size
            }

            // コピーして元データを消す
            MediaStoreTool.copyToMovieFolder(context, resultFile)
            resultFile.delete()

            isRunning.value = false
        }
    }

    Column {
        imageList.forEach { Text(text = it.toString()) }
        Button(
            modifier = Modifier.padding(10.dp),
            onClick = {
                imageList.clear()
                imagePicker.launch("image/*")
            }
        ) { Text(text = "画像を選ぶ") }

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
    return ImageDecoder.createSource(context.contentResolver, uri)
        .let { src -> ImageDecoder.decodeDrawable(src) { decoder, info, s -> decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE } }
        .toBitmap()
}