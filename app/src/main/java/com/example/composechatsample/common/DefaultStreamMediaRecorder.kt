package com.example.composechatsample.common

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import androidx.core.net.toUri
import com.example.composechatsample.core.DispatcherProvider
import com.example.composechatsample.core.MediaRecorderState
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.RecordedMedia
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.AttachmentType
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.log10
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.StreamFileUtil

const val EXTRA_DURATION: String = "duration"
const val EXTRA_WAVEFORM_DATA: String = "waveform_data"

class DefaultStreamMediaRecorder(
    private val context: Context,
) : StreamMediaRecorder {


    private var mediaRecorderState: MediaRecorderState = MediaRecorderState.UNINITIALIZED
        set(value) {
            field = value
            onStreamMediaRecorderStateChangedListener?.onStateChanged(field)

            when (field) {
                MediaRecorderState.RECORDING -> {
                    activeRecordingStartedAt = System.currentTimeMillis()
                    logger.d { "[onMediaRecorderState] #1; activeRecordingStartedAt: $activeRecordingStartedAt" }
                    trackMaxDuration()
                }
                else -> {
                    activeRecordingStartedAt = 0L
                    logger.d { "[onMediaRecorderState] #2; activeRecordingStartedAt: $activeRecordingStartedAt" }
                }
            }
        }

    private val coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO)

    private var pollingJob: Job? = null

    private var currentRecordingDurationJob: Job? = null

    private val logger by taggedLogger("Chat:DefaultStreamMediaRecorder")

    private var mediaRecorder: MediaRecorder? = null
        set(value) {
            if (value != null) {
                onErrorListener?.let { value.setOnErrorListener(it) }
                onInfoListener?.let { value.setOnInfoListener(it) }
            }
            field = value
        }

    private var recordingFile: File? = null

    private var activeRecordingStartedAt: Long? = null

    private var sampleData = arrayListOf<Float>()

    private var onErrorListener: MediaRecorder.OnErrorListener? = null

    private var onInfoListener: MediaRecorder.OnInfoListener? = null

    private var onStartRecordingListener: StreamMediaRecorder.OnRecordingStarted? = null

    private var onStopRecordingListener: StreamMediaRecorder.OnRecordingStopped? = null

    private var onMaxAmplitudeSampledListener: StreamMediaRecorder.OnMaxAmplitudeSampled? = null

    private var onStreamMediaRecorderStateChangedListener: StreamMediaRecorder.OnMediaRecorderStateChange? = null

    private var onCurrentRecordingDurationChangedListener: StreamMediaRecorder.OnCurrentRecordingDurationChanged? = null

    @Throws
    private fun initializeMediaRecorderForAudio(
        context: Context,
        recordingFile: File,
    ) {
        release()

        mediaRecorder = if (Build.VERSION.SDK_INT < 31) {
            MediaRecorder()
        } else {
            MediaRecorder(context)
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recordingFile.path)
            prepare()
            mediaRecorderState = MediaRecorderState.PREPARED
        }
    }

    override fun startAudioRecording(
        recordingName: String,
        amplitudePollingInterval: Long,
        override: Boolean,
    ): Result<File> {
        return try {
            StreamFileUtil.createFileInCacheDir(context, recordingName)
                .onSuccess {
                    recordingFile = it
                    initializeMediaRecorderForAudio(
                        context = context,
                        recordingFile = it,
                    )
                    requireNotNull(mediaRecorder)
                    mediaRecorder?.start()
                    onStartRecordingListener?.onStarted()

                    mediaRecorderState = MediaRecorderState.RECORDING
                    pollMaxAmplitude(amplitudePollingInterval)

                    Result.Success(it)
                }
        } catch (exception: Exception) {
            release()
            logger.e(exception) { "Could not start recording audio" }
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not start audio recording.",
                    cause = exception,
                ),
            )
        }
    }

    override fun startAudioRecording(
        recordingFile: File,
        amplitudePollingInterval: Long,
    ): Result<Unit> {
        return try {
            this.recordingFile = recordingFile

            initializeMediaRecorderForAudio(
                context = context,
                recordingFile = recordingFile,
            )

            requireNotNull(mediaRecorder)

            mediaRecorder?.start()
            onStartRecordingListener?.onStarted()
            mediaRecorderState = MediaRecorderState.RECORDING
            pollMaxAmplitude(amplitudePollingInterval)
            Result.Success(Unit)
        } catch (exception: Exception) {
            release()
            logger.e(exception) { "Could not start recording audio" }
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not start audio recording.",
                    cause = exception,
                ),
            )
        }
    }

    override fun stopRecording(): Result<RecordedMedia> {
        return try {
            requireNotNull(mediaRecorder)
            mediaRecorder?.stop()

            val calculatedDurationInMs = activeRecordingStartedAt?.let {
                System.currentTimeMillis() - it
            } ?: 0
            val parsedDurationInMs = getAudioDurationInMs(recordingFile)
            logger.d {
                "[stopRecording] startedAt: $activeRecordingStartedAt, " +
                    "calculatedDuration: $calculatedDurationInMs, parsedDuration: $parsedDurationInMs"
            }

            val durationInMs = when (parsedDurationInMs > 0) {
                true -> parsedDurationInMs
                else -> calculatedDurationInMs.toInt()
            }
            onCurrentRecordingDurationChangedListener?.onDurationChanged(durationInMs.toLong())
            release()
            onStopRecordingListener?.onStopped()

            val attachment = Attachment(
                title = recordingFile?.name ?: "Recording",
                upload = recordingFile,
                type = AttachmentType.AUDIO_RECORDING,
                mimeType = "audio/aac",
                extraData = mapOf(
                    EXTRA_DURATION to durationInMs / 1000f,
                    EXTRA_WAVEFORM_DATA to sampleData,
                ),
            )
            val recordedMedia = RecordedMedia(attachment = attachment, durationInMs = durationInMs)
            logger.v { "[stopRecording] succeed: $recordedMedia" }
            Result.Success(recordedMedia)
        } catch (exception: Exception) {
            logger.e(exception) { "[stopRecording] failed: $exception" }
            release()
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not Stop audio recording.",
                    cause = exception,
                ),
            )
        }
    }

    private fun getAudioDurationInMs(file: File?): Int {
        file ?: return 0
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.toUri().toString())
            val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationString?.toInt()
            duration ?: 0
        } catch (e: Throwable) {
            logger.e(e) { "[getAudioDurationInMs] failed: $e" }
            0
        } finally {
            try {
                retriever.release()
            } catch (_: Throwable) {
            }
        }
    }

    override fun deleteRecording(recordingFile: File): Result<Unit> {
        return try {
            recordingFile.delete()

            Result.Success(Unit)
        } catch (exception: Exception) {
            logger.e(exception) { "Could not delete audio recording" }
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not delete audio recording.",
                    cause = exception,
                ),
            )
        }
    }

    override fun release() {
        mediaRecorder?.release()
        mediaRecorderState = MediaRecorderState.UNINITIALIZED
        onStopRecordingListener?.onStopped()
    }

    private fun pollMaxAmplitude(amplitudePollingInterval: Long) {
        sampleData.clear()
        pollingJob?.cancel()
        pollingJob = coroutineScope.launch {
            try {
                while (mediaRecorderState == MediaRecorderState.RECORDING) {
                    val maxAmplitude = mediaRecorder?.maxAmplitude

                    if (maxAmplitude != null) {
                        val db = 20 * log10(maxAmplitude.toDouble())
                        val normalized = maxAmplitude / Short.MAX_VALUE.toFloat()
                        logger.d { "[pollMaxAmplitude] maxAmplitude: $maxAmplitude, db: $db, normalized: $normalized" }
                        sampleData.add(normalized)
                        onMaxAmplitudeSampledListener?.onSampled(maxAmplitude)
                    }
                    delay(amplitudePollingInterval)
                }
            } catch (e: Exception) {
                logger.e {
                    "Could not start poll max amplitude: ${e.message ?: e.cause}"
                }
            }
        }
    }

    private fun trackMaxDuration() {
        currentRecordingDurationJob?.cancel()

        currentRecordingDurationJob = coroutineScope.launch {
            while (mediaRecorderState == MediaRecorderState.RECORDING) {
                val activeRecordingStartedAt = this@DefaultStreamMediaRecorder.activeRecordingStartedAt
                val currentDuration =
                    if (activeRecordingStartedAt != null) {
                        System.currentTimeMillis() - activeRecordingStartedAt
                    } else {
                        0L
                    }

                onCurrentRecordingDurationChangedListener?.onDurationChanged(currentDuration)

                delay(1000)
            }
        }
    }

    override fun setOnErrorListener(onErrorListener: StreamMediaRecorder.OnErrorListener) {
        mediaRecorder?.setOnErrorListener { _, what, extra ->
            onErrorListener.onError(
                streamMediaRecorder = this,
                what = what,
                extra = extra,
            )
        }
    }

    override fun setOnInfoListener(onInfoListener: StreamMediaRecorder.OnInfoListener) {
        mediaRecorder?.setOnInfoListener { _, what, extra ->
            onInfoListener.onInfo(
                this,
                what = what,
                extra = extra,
            )
        }
    }

    override fun setOnRecordingStartedListener(onRecordingStarted: StreamMediaRecorder.OnRecordingStarted) {
        this.onStartRecordingListener = onRecordingStarted
    }

    override fun setOnRecordingStoppedListener(onRecordingStopped: StreamMediaRecorder.OnRecordingStopped) {
        this.onStopRecordingListener = onRecordingStopped
    }

    override fun setOnMaxAmplitudeSampledListener(onMaxAmplitudeSampled: StreamMediaRecorder.OnMaxAmplitudeSampled) {
        this.onMaxAmplitudeSampledListener = onMaxAmplitudeSampled
    }

    override fun setOnMediaRecorderStateChangedListener(
        onMediaRecorderStateChange: StreamMediaRecorder.OnMediaRecorderStateChange,
    ) {
        this.onStreamMediaRecorderStateChangedListener = onMediaRecorderStateChange
    }

    override fun setOnCurrentRecordingDurationChangedListener(
        onCurrentRecordingDurationChanged: StreamMediaRecorder.OnCurrentRecordingDurationChanged,
    ) {
        this.onCurrentRecordingDurationChangedListener = onCurrentRecordingDurationChanged
    }
}