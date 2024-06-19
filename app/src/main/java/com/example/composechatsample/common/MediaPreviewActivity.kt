package com.example.composechatsample.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import com.example.composechatsample.R
import com.example.composechatsample.ui.theme.ChatTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

public class MediaPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(KEY_URL)
        val title = intent.getStringExtra(KEY_TITLE) ?: ""

        if (url.isNullOrEmpty()) {
            finish()
            return
        }

        setContent {
            ChatTheme {
                SetupSystemUI()
                MediaPreviewScreen(
                    url = url,
                    title = title,
                    onPlaybackError = {
                        Toast.makeText(
                            this,
                            R.string.stream_ui_message_list_attachment_display_error,
                            Toast.LENGTH_SHORT,
                        ).show()
                        finish()
                    },
                    onBackPressed = { finish() },
                )
            }
        }
    }


    @Composable
    private fun MediaPreviewScreen(
        url: String,
        title: String,
        onPlaybackError: () -> Unit,
        onBackPressed: () -> Unit,
    ) {
        BackHandler(enabled = true, onBack = onBackPressed)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            backgroundColor = Color.Black,
            topBar = { MediaPreviewToolbar(title, onBackPressed) },
            content = { MediaPreviewContent(url, onBackPressed, onPlaybackError) },
        )
    }

    @Composable
    private fun SetupSystemUI() {
        val systemUiController = rememberSystemUiController()

        val statusBarColor = Color.Black
        val navigationBarColor = Color.Black

        SideEffect {
            systemUiController.setStatusBarColor(
                color = statusBarColor,
                darkIcons = false,
            )
            systemUiController.setNavigationBarColor(
                color = navigationBarColor,
                darkIcons = false,
            )
        }
    }

    @Composable
    private fun MediaPreviewToolbar(
        title: String,
        onBackPressed: () -> Unit = {},
    ) {
        TopAppBar(
            backgroundColor = Color.Black,
            elevation = 0.dp,
            navigationIcon = {
                IconButton(
                    modifier = Modifier.mirrorRtl(LocalLayoutDirection.current),
                    onClick = { onBackPressed() },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = ChatTheme.typography.body,
                    maxLines = 1,
                    color = Color.White,
                )
            },
        )
    }

    @Composable
    private fun MediaPreviewContent(
        url: String,
        onBackPressed: () -> Unit = {},
        onPlaybackError: () -> Unit,
    ) {
        val context = LocalContext.current

        val contentView = remember {
            val mediaController = createMediaController(context, onBackPressed)

            val frameLayout = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }

            val progressBar = ProgressBar(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    gravity = Gravity.CENTER
                }
            }

            progressBar.isVisible = true

            val videoView = VideoView(context).apply {
                setVideoURI(Uri.parse(url))
                setMediaController(mediaController)
                setOnErrorListener { _, _, _ ->
                    progressBar.isVisible = false
                    onPlaybackError()
                    true
                }
                setOnPreparedListener {
                    progressBar.isVisible = false
                    start()
                    mediaController.show()
                }
                mediaController.setAnchorView(frameLayout)

                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ).apply {
                    gravity = Gravity.CENTER
                }
            }

            frameLayout.apply {
                addView(videoView)
                addView(progressBar)
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            factory = { contentView },
        )
    }


    private fun createMediaController(
        context: Context,
        onBackPressed: () -> Unit = {},
    ): MediaController {
        return object : MediaController(context) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed()
                }
                return super.dispatchKeyEvent(event)
            }
        }
    }

    public companion object {
        private const val KEY_URL: String = "url"
        private const val KEY_TITLE: String = "title"

        public fun getIntent(context: Context, url: String, title: String? = null): Intent {
            return Intent(context, MediaPreviewActivity::class.java).apply {
                putExtra(KEY_URL, url)
                putExtra(KEY_TITLE, title)
            }
        }
    }
}