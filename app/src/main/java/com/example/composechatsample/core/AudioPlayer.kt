package com.example.composechatsample.core

public interface AudioPlayer {

    public val currentState: AudioState

    public fun registerOnAudioStateChange(audioHash: Int, onAudioStateChange: (AudioState) -> Unit)

    public fun registerOnProgressStateChange(audioHash: Int, onProgressDataChange: (ProgressData) -> Unit)

    public fun registerOnSpeedChange(audioHash: Int, onSpeedChange: (Float) -> Unit)

    public fun registerTrack(sourceUrl: String, audioHash: Int, position: Int)

    public fun clearTracks()

    public fun prepare(sourceUrl: String, audioHash: Int)

    public fun play(sourceUrl: String, audioHash: Int)

    public fun pause()

    public fun resume(audioHash: Int)

    public fun resetAudio(audioHash: Int)

    public fun seekTo(positionInMs: Int, audioHash: Int)

    public fun startSeek(audioHash: Int)

    public fun changeSpeed()

    public fun currentSpeed(): Float

    public fun removeAudio(audioHash: Int)

    public fun removeAudios(audioHashList: List<Int>)

    public fun dispose()
}

public data class ProgressData(
    public val currentPosition: Int,
    public val progress: Float,
    public val duration: Int,
)

public enum class AudioState {
    UNSET,
    LOADING,
    IDLE,
    PAUSE,
    PLAYING,
}

public enum class PlayerState {
    UNSET,
    LOADING,
    IDLE,
    PAUSE,
    PLAYING,
}