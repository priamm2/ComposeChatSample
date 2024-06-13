package com.example.composechatsample.core

import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.composechatsample.log.taggedLogger
import java.io.IOException

public interface NativeMediaPlayer {

    public companion object {
        public const val MEDIA_ERROR_UNKNOWN: Int = 1
        public const val MEDIA_ERROR_SERVER_DIED: Int = 100
        public const val MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK: Int = 200

        public const val MEDIA_ERROR_IO: Int = -1004
        public const val MEDIA_ERROR_MALFORMED: Int = -1007
        public const val MEDIA_ERROR_UNSUPPORTED: Int = -1010

        public const val MEDIA_ERROR_TIMED_OUT: Int = -110
        public const val MEDIA_ERROR_SYSTEM: Int = -2147483648
    }

    @get:RequiresApi(Build.VERSION_CODES.M)
    @set:RequiresApi(Build.VERSION_CODES.M)
    @get:Throws(IllegalStateException::class)
    @set:Throws(IllegalStateException::class, IllegalArgumentException::class)
    public var speed: Float

    public val state: NativeMediaPlayerState

    public val currentPosition: Int

    public val duration: Int

    @Throws(
        IOException::class,
        IllegalArgumentException::class,
        SecurityException::class,
        IllegalStateException::class,
    )
    public fun setDataSource(path: String)

    @Throws(IOException::class, IllegalStateException::class)
    public fun prepare()

    @Throws(IllegalStateException::class)
    public fun prepareAsync()

    @Throws(IllegalStateException::class)
    public fun seekTo(msec: Int)

    @Throws(IllegalStateException::class)
    public fun start()

    @Throws(IllegalStateException::class)
    public fun pause()

    @Throws(IllegalStateException::class)
    public fun stop()

    public fun reset()

    public fun release()

    public fun setOnCompletionListener(listener: () -> Unit)

    public fun setOnErrorListener(listener: (what: Int, extra: Int) -> Boolean)

    public fun setOnPreparedListener(listener: () -> Unit)
}

public enum class NativeMediaPlayerState {
    IDLE,
    INITIALIZED,
    PREPARING,
    PREPARED,
    STARTED,
    PAUSED,
    STOPPED,
    PLAYBACK_COMPLETED,
    END,
    ERROR,
}

internal class NativeMediaPlayerImpl(
    private val builder: () -> MediaPlayer,
) : NativeMediaPlayer {

    companion object {
        private const val DEBUG = false
    }

    private val logger by taggedLogger("Chat:NativeMediaPlayer")

    private var _mediaPlayer: MediaPlayer? = null
        set(value) {
            if (DEBUG) logger.i { "[setMediaPlayerInstance] instance: $value" }
            field = value
        }

    private val mediaPlayer: MediaPlayer get() {
        return _mediaPlayer ?: builder().also {
            _mediaPlayer = it.setupListeners()
            state = NativeMediaPlayerState.IDLE
        }
    }

    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((what: Int, extra: Int) -> Boolean)? = null
    private var onPreparedListener: (() -> Unit)? = null

    override var state: NativeMediaPlayerState = NativeMediaPlayerState.END
        set(value) {
            if (DEBUG) logger.d { "[setMediaPlayerState] state: $value <= $field" }
            field = value
        }

    override var speed: Float
        @RequiresApi(Build.VERSION_CODES.M)
        @Throws(IllegalStateException::class)
        get() = mediaPlayer.playbackParams.speed

        @RequiresApi(Build.VERSION_CODES.M)
        @Throws(IllegalStateException::class, IllegalArgumentException::class)
        set(value) {
            if (DEBUG) logger.d { "[setSpeed] speed: $value" }
            mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(value)
        }
    override val currentPosition: Int
        get() = mediaPlayer.currentPosition

    override val duration: Int
        get() = mediaPlayer.duration

    @Throws(
        IOException::class,
        IllegalArgumentException::class,
        SecurityException::class,
        IllegalStateException::class,
    )
    override fun setDataSource(path: String) {
        if (DEBUG) logger.d { "[setDataSource] path: $path" }
        mediaPlayer.setDataSource(path)
        state = NativeMediaPlayerState.INITIALIZED
    }

    @Throws(IllegalStateException::class)
    override fun prepareAsync() {
        if (DEBUG) logger.d { "[prepareAsync] no args" }
        mediaPlayer.prepareAsync()
        state = NativeMediaPlayerState.PREPARING
    }

    @Throws(IOException::class, IllegalStateException::class)
    override fun prepare() {
        if (DEBUG) logger.d { "[prepare] no args" }
        mediaPlayer.prepare()
        state = NativeMediaPlayerState.PREPARED
    }

    @Throws(IllegalStateException::class)
    override fun seekTo(msec: Int) {
        if (DEBUG) logger.d { "[seekTo] msec: $msec" }
        mediaPlayer.seekTo(msec)
    }

    @Throws(IllegalStateException::class)
    override fun start() {
        if (DEBUG) logger.d { "[start] no args" }
        mediaPlayer.start()
        state = NativeMediaPlayerState.STARTED
    }

    @Throws(IllegalStateException::class)
    override fun pause() {
        if (DEBUG) logger.d { "[pause] no args" }
        mediaPlayer.pause()
        state = NativeMediaPlayerState.PAUSED
    }

    @Throws(IllegalStateException::class)
    override fun stop() {
        if (DEBUG) logger.d { "[stop] no args" }
        mediaPlayer.stop()
        state = NativeMediaPlayerState.STOPPED
    }

    override fun reset() {
        if (DEBUG) logger.d { "[reset] no args" }
        mediaPlayer.reset()
        state = NativeMediaPlayerState.IDLE
    }

    override fun release() {
        if (DEBUG) logger.d { "[release] no args" }
        mediaPlayer.release()
        state = NativeMediaPlayerState.END
        _mediaPlayer = null
    }

    override fun setOnPreparedListener(listener: () -> Unit) {
        if (DEBUG) logger.d { "[setOnPreparedListener] listener: $listener" }
        this.onPreparedListener = listener
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        if (DEBUG) logger.d { "[setOnCompletionListener] listener: $listener" }
        this.onCompletionListener = listener
    }

    override fun setOnErrorListener(listener: (what: Int, extra: Int) -> Boolean) {
        if (DEBUG) logger.d { "[setOnErrorListener] listener: $listener" }
        this.onErrorListener = listener
    }

    private fun MediaPlayer.setupListeners(): MediaPlayer {
        setOnErrorListener { _, what, extra ->
            if (DEBUG) logger.e { "[onError] what: $what, extra: $extra" }
            state = NativeMediaPlayerState.ERROR
            _mediaPlayer = null
            onErrorListener?.invoke(what, extra) ?: false
        }
        setOnPreparedListener {
            if (DEBUG) logger.d { "[onPrepared] no args" }
            state = NativeMediaPlayerState.PREPARED
            onPreparedListener?.invoke()
        }
        setOnCompletionListener {
            if (DEBUG) logger.d { "[onCompletion] no args" }
            state = NativeMediaPlayerState.PLAYBACK_COMPLETED
            onCompletionListener?.invoke()
        }
        return this
    }
}