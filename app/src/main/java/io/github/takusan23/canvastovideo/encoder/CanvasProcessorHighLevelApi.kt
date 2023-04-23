package io.github.takusan23.canvastovideo.encoder

import android.content.Context
import android.graphics.Canvas
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File

/** OpenGL と MediaCodec を使わずに [CanvasProcessor] をする */
@RequiresApi(Build.VERSION_CODES.M)
object CanvasProcessorHighLevelApi {

    /**
     * 処理を開始する
     *
     * @param context [Context]
     * @param resultFile エンコード先のファイル
     * @param videoCodec 動画コーデック
     * @param containerFormat コンテナフォーマット
     * @param bitRate ビットレート
     * @param frameRate フレームレート
     * @param outputVideoWidth 動画の高さ
     * @param outputVideoHeight 動画の幅
     * @param onCanvasDrawRequest Canvasの描画が必要になったら呼び出される。trueを返している間、動画を作成する
     */
    suspend fun start(
        context: Context,
        resultFile: File,
        videoCodec: Int = MediaRecorder.VideoEncoder.H264,
        containerFormat: Int = MediaRecorder.OutputFormat.MPEG_4,
        bitRate: Int = 1_000_000,
        frameRate: Int = 30,
        outputVideoWidth: Int = 1280,
        outputVideoHeight: Int = 720,
        onCanvasDrawRequest: Canvas.(positionMs: Long) -> Boolean,
    ) = withContext(Dispatchers.Default) {
        val mediaRecorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()).apply {
            // メソッド呼び出しには順番があります
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(containerFormat)
            setVideoEncoder(videoCodec)
            setVideoEncodingBitRate(bitRate)
            setVideoFrameRate(frameRate)
            setVideoSize(outputVideoWidth, outputVideoHeight)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(resultFile.path)
            prepare()
        }
        val inputSurface = mediaRecorder.surface
        // 終了フラグ
        var isRunning = true
        // トータルフレーム数。経過時間の計算で
        var totalFrameCount = 0
        // この値の間隔で Canvas を再描画する。 60 fps なら 16 になる
        val frameToMs = 1000L / frameRate
        mediaRecorder.start()
        while (isActive && isRunning) {
            for (i in 0 until frameRate) {
                val positionMs = totalFrameCount * frameToMs
                val canvas = inputSurface.lockHardwareCanvas()
                isRunning = onCanvasDrawRequest(canvas, positionMs)
                inputSurface.unlockCanvasAndPost(canvas)
                totalFrameCount++
                delay(frameToMs)
                if (!isRunning) {
                    break
                }
            }
        }
        mediaRecorder.stop()
        mediaRecorder.release()
    }
}