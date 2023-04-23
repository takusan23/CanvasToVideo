package io.github.takusan23.canvastovideo

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.contentValuesOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MediaStoreTool {

    /**
     * 端末の動画フォルダーへコピーする
     *
     * @param context [Context]
     * @param file コピーするファイル
     */
    suspend fun copyToMovieFolder(
        context: Context,
        file: File
    ) = withContext(Dispatchers.IO) {
        val contentValues = contentValuesOf(
            MediaStore.MediaColumns.DISPLAY_NAME to file.name,
            // RELATIVE_PATH（ディレクトリを掘る） は Android 10 以降のみです
            MediaStore.MediaColumns.RELATIVE_PATH to "${Environment.DIRECTORY_MOVIES}/CanvasToVideo"
        )
        val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            file.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

}