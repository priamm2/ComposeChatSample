package com.example.composechatsample.common

import com.example.composechatsample.core.MediaRecorderState
import com.example.composechatsample.core.RecordedMedia
import java.io.File
import com.example.composechatsample.core.Result
public interface StreamMediaRecorder {

    public fun startAudioRecording(
        recordingName: String,
        amplitudePollingInterval: Long = 100L,
        override: Boolean = true,
    ): Result<File>

    public fun startAudioRecording(
        recordingFile: File,
        amplitudePollingInterval: Long = 100L,
    ): Result<Unit>

    public fun stopRecording(): Result<RecordedMedia>

    public fun deleteRecording(recordingFile: File): Result<Unit>

    public fun release()

    public fun setOnErrorListener(onErrorListener: OnErrorListener)

    public fun setOnInfoListener(onInfoListener: OnInfoListener)

    public fun setOnRecordingStartedListener(onRecordingStarted: OnRecordingStarted)

    public fun setOnRecordingStoppedListener(onRecordingStopped: OnRecordingStopped)

    public fun setOnMaxAmplitudeSampledListener(onMaxAmplitudeSampled: OnMaxAmplitudeSampled)

    public fun setOnMediaRecorderStateChangedListener(onMediaRecorderStateChange: OnMediaRecorderStateChange)

    public fun setOnCurrentRecordingDurationChangedListener(
        onCurrentRecordingDurationChanged: OnCurrentRecordingDurationChanged,
    )

    public fun interface OnInfoListener {
        public fun onInfo(
            streamMediaRecorder: StreamMediaRecorder,
            what: Int,
            extra: Int,
        )
    }
    public fun interface OnErrorListener {
        public fun onError(
            streamMediaRecorder: StreamMediaRecorder,
            what: Int,
            extra: Int,
        )
    }

    public fun interface OnRecordingStarted {
        public fun onStarted()
    }

    public fun interface OnRecordingStopped {
        public fun onStopped()
    }

    public fun interface OnMaxAmplitudeSampled {
        public fun onSampled(maxAmplitude: Int)
    }

    public fun interface OnMediaRecorderStateChange {
        public fun onStateChanged(recorderState: MediaRecorderState)
    }

    public fun interface OnCurrentRecordingDurationChanged {
        public fun onDurationChanged(durationMs: Long)
    }
}