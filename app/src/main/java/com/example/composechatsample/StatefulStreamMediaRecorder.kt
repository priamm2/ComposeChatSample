package com.example.composechatsample

import android.content.Context
import android.media.MediaRecorder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.composechatsample.common.StreamMediaRecorder
import com.example.composechatsample.core.MediaRecorderState
import com.example.composechatsample.core.RecordedMedia
import com.example.composechatsample.core.StreamMediaRecorderState
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.log.TaggedLogger
import java.io.File
import com.example.composechatsample.core.Result

class StatefulStreamMediaRecorder(
    private val streamMediaRecorder: StreamMediaRecorder,
) {

    private var maxAmplitudeSampleKey by mutableStateOf(0)
    private val logger: TaggedLogger = StreamLog.getLogger("StreamMediaRecorderStateHolder")
    private val _onInfoState: MutableState<StreamMediaRecorderState?> = mutableStateOf(null)
    public val onInfoState: State<StreamMediaRecorderState?> = _onInfoState
    private val _onErrorState: MutableState<StreamMediaRecorderState?> = mutableStateOf(null)
    public val onErrorState: State<StreamMediaRecorderState?> = _onErrorState
    private val _latestMaxAmplitude: MutableState<KeyValuePair<Int, Int>> = mutableStateOf(KeyValuePair(0, 0))
    public val latestMaxAmplitude: State<KeyValuePair<Int, Int>> = _latestMaxAmplitude
    private val _mediaRecorderState: MutableState<MediaRecorderState> =
        mutableStateOf(MediaRecorderState.UNINITIALIZED)

    public val mediaRecorderState: State<MediaRecorderState> = _mediaRecorderState
    private val _activeRecordingDuration: MutableState<Long> =
        mutableStateOf(0L)
    public val activeRecordingDuration: State<Long> = _activeRecordingDuration

    init {
        streamMediaRecorder.setOnInfoListener { streamMediaRecorder, what, extra ->
            logger.v { "[setOnInfoListener] -> what: $what , extra: $extra" }

            _onInfoState.value = StreamMediaRecorderState(
                streamMediaRecorder = streamMediaRecorder,
                what = what,
                extra = extra,
            )
        }

        streamMediaRecorder.setOnErrorListener { streamMediaRecorder, what, extra ->
            logger.v { "[setOnErrorListener] -> what: $what , extra: $extra" }

            if (what == MediaRecorder.MEDIA_ERROR_SERVER_DIED) {
                streamMediaRecorder.release()
            }

            _onErrorState.value = StreamMediaRecorderState(
                streamMediaRecorder = streamMediaRecorder,
                what = what,
                extra = extra,
            )
        }

        streamMediaRecorder.setOnMaxAmplitudeSampledListener {
            logger.v { "[setOnMaxAmplitudeSampledListener] -> $it" }

            _latestMaxAmplitude.value = KeyValuePair(maxAmplitudeSampleKey, it)
            maxAmplitudeSampleKey += 1
        }

        streamMediaRecorder.setOnMediaRecorderStateChangedListener {
            logger.v { "[setOnMediaRecorderStateChangedListener] -> ${it.name}" }

            maxAmplitudeSampleKey = 0
            _mediaRecorderState.value = it
        }

        streamMediaRecorder.setOnCurrentRecordingDurationChangedListener {
            logger.v { "[setOnCurrentRecordingDurationChangedListener] -> $it" }

            _activeRecordingDuration.value = it
        }
    }

    public fun startAudioRecording(
        context: Context,
        recordingName: String,
        override: Boolean = true,
    ): Result<File> =
        streamMediaRecorder.startAudioRecording(
            recordingName = recordingName,
            override = override,
        )

    public fun startAudioRecording(
        context: Context,
        recordingFile: File,
    ): Result<Unit> = streamMediaRecorder.startAudioRecording(
        recordingFile = recordingFile,
    )

    public fun stopRecording(): Result<RecordedMedia> =
        streamMediaRecorder.stopRecording()

    public fun deleteRecording(recordingFile: File): Result<Unit> =
        streamMediaRecorder.deleteRecording(recordingFile)

    public fun release() {
        streamMediaRecorder.release()
    }
}