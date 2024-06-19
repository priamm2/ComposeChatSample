package com.example.composechatsample.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.composechatsample.R
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.DispatcherProvider
import com.example.composechatsample.core.StreamFileUtil
import com.example.composechatsample.core.applyStreamCdnImageResizingIfEnabled
import com.example.composechatsample.core.hasLink
import com.example.composechatsample.core.imagePreviewUrl
import com.example.composechatsample.core.initials
import com.example.composechatsample.core.isDeleted
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.AttachmentType
import com.example.composechatsample.core.models.ConnectionState
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.streamcdn.image.StreamCdnCropImageMode
import com.example.composechatsample.core.models.streamcdn.image.StreamCdnResizeImageMode
import com.example.composechatsample.screen.MediaGalleryPreviewResult
import com.example.composechatsample.screen.MediaGalleryPreviewResultType
import com.example.composechatsample.screen.components.Avatar
import com.example.composechatsample.screen.components.LoadingIndicator
import com.example.composechatsample.screen.components.NetworkLoadingIndicator
import com.example.composechatsample.screen.components.SimpleDialog
import com.example.composechatsample.screen.components.Timestamp
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.viewModel.MediaGalleryPreviewViewModel
import com.example.composechatsample.viewModel.MediaGalleryPreviewViewModelFactory
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.toMessage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.math.abs

public class MediaGalleryPreviewActivity : AppCompatActivity() {


    private val factory by lazy {
        val messageId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(
                KeyMediaGalleryPreviewActivityState, MediaGalleryPreviewActivityState::class.java,
            )?.messageId
        } else {
            intent?.getParcelableExtra<MediaGalleryPreviewActivityState>(
                KeyMediaGalleryPreviewActivityState,
            )?.messageId
        } ?: ""

        MediaGalleryPreviewViewModelFactory(
            chatClient = ChatClient.instance(),
            messageId = messageId,
            skipEnrichUrl = intent?.getBooleanExtra(KeySkipEnrichUrl, false) ?: false,
        )
    }

    private var fileSharingJob: Job? = null


    private val mediaGalleryPreviewViewModel by viewModels<MediaGalleryPreviewViewModel>(factoryProducer = { factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mediaGalleryPreviewActivityState = intent?.getParcelableExtra<MediaGalleryPreviewActivityState>(
            KeyMediaGalleryPreviewActivityState,
        )
        val videoThumbnailsEnabled = intent?.getBooleanExtra(KeyVideoThumbnailsEnabled, true) ?: true
        val streamCdnImageResizing = intent?.createStreamCdnImageResizing()
            ?: StreamCdnImageResizing.defaultStreamCdnImageResizing()
        val messageId = mediaGalleryPreviewActivityState?.messageId ?: ""

        if (!mediaGalleryPreviewViewModel.hasCompleteMessage) {
            val message = mediaGalleryPreviewActivityState?.toMessage()

            if (message != null) {
                mediaGalleryPreviewViewModel.message = message
            }
        }

        val attachmentPosition = intent?.getIntExtra(KeyAttachmentPosition, 0) ?: 0

        if (messageId.isBlank()) {
            throw IllegalArgumentException("Missing messageId necessary to load images.")
        }

        setContent {
            ChatTheme(
                videoThumbnailsEnabled = videoThumbnailsEnabled,
                streamCdnImageResizing = streamCdnImageResizing,
            ) {
                SetupSystemUI()

                val message = mediaGalleryPreviewViewModel.message

                if (message.isDeleted()) {
                    finish()
                    return@ChatTheme
                }

                MediaGalleryPreviewContentWrapper(message, attachmentPosition)
            }
        }
    }


    @Composable
    private fun SetupSystemUI() {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = !isSystemInDarkTheme()

        val systemBarsColor = ChatTheme.colors.barsBackground

        SideEffect {
            systemUiController.setSystemBarsColor(
                color = systemBarsColor,
                darkIcons = useDarkIcons,
            )
        }
    }


    @Suppress("MagicNumber", "LongMethod")
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun MediaGalleryPreviewContentWrapper(
        message: Message,
        initialAttachmentPosition: Int,
    ) {

        val filteredAttachments = message.attachments.filter { attachment ->
            !attachment.hasLink()
        }

        val startingPosition =
            if (initialAttachmentPosition !in filteredAttachments.indices) 0 else initialAttachmentPosition

        val scaffoldState = rememberScaffoldState()
        val pagerState = rememberPagerState(initialPage = startingPosition)
        val coroutineScope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                scaffoldState = scaffoldState,
                topBar = { MediaGalleryPreviewTopBar(message) },
                content = { contentPadding ->
                    if (message.id.isNotEmpty()) {
                        Box(Modifier.fillMaxSize()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(contentPadding),
                            ) {
                                MediaPreviewContent(pagerState, filteredAttachments) {
                                    coroutineScope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar(
                                            message = getString(R.string.stream_ui_message_list_video_display_error),
                                        )
                                    }
                                }
                            }
                        }

                        val promptedAttachment = mediaGalleryPreviewViewModel.promptedAttachment

                        if (promptedAttachment != null) {
                            SimpleDialog(
                                title = getString(
                                    R.string.stream_compose_media_gallery_share_large_file_prompt_title,
                                ),
                                message = getString(
                                    R.string.stream_compose_media_gallery_share_large_file_prompt_message,
                                    (promptedAttachment.fileSize.toFloat() / (1024 * 1024)),
                                ),
                                onPositiveAction = remember(mediaGalleryPreviewViewModel) {
                                    {
                                        shareAttachment(promptedAttachment)
                                        mediaGalleryPreviewViewModel.promptedAttachment = null
                                    }
                                },
                                onDismiss = remember(mediaGalleryPreviewViewModel) {
                                    {
                                        mediaGalleryPreviewViewModel.promptedAttachment = null
                                    }
                                },
                            )
                        }
                    }
                },
                bottomBar = {
                    if (message.id.isNotEmpty()) {
                        MediaGalleryPreviewBottomBar(filteredAttachments, pagerState)
                    }
                },
            )

            AnimatedVisibility(
                visible = mediaGalleryPreviewViewModel.isShowingOptions,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                MediaGalleryPreviewOptions(
                    options = defaultMediaOptions(message = message),
                    pagerState = pagerState,
                    attachments = filteredAttachments,
                    modifier = Modifier.animateEnterExit(
                        enter = slideInVertically(),
                        exit = slideOutVertically(),
                    ),
                )
            }

            if (message.id.isNotEmpty()) {
                AnimatedVisibility(
                    visible = mediaGalleryPreviewViewModel.isShowingGallery,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    MediaGallery(
                        pagerState = pagerState,
                        attachments = filteredAttachments,
                        modifier = Modifier.animateEnterExit(
                            enter = slideInVertically(initialOffsetY = { height -> height / 2 }),
                            exit = slideOutVertically(targetOffsetY = { height -> height / 2 }),
                        ),
                    )
                }
            }
        }
    }

    @Composable
    private fun MediaGalleryPreviewTopBar(message: Message) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            elevation = 4.dp,
            color = ChatTheme.colors.barsBackground,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = ::finish) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_close),
                        contentDescription = stringResource(id = R.string.stream_compose_cancel),
                        tint = ChatTheme.colors.textHighEmphasis,
                    )
                }

                MediaGalleryPreviewHeaderTitle(
                    modifier = Modifier.weight(8f),
                    message = message,
                )

                MediaGalleryPreviewOptionsToggle(
                    modifier = Modifier.weight(1f),
                    message = message,
                )
            }
        }
    }

    @Composable
    private fun MediaGalleryPreviewHeaderTitle(
        message: Message,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val textStyle = ChatTheme.typography.title3Bold
            val textColor = ChatTheme.colors.textHighEmphasis

            when (mediaGalleryPreviewViewModel.connectionState) {
                is ConnectionState.Connected -> Text(
                    text = message.user.name,
                    style = textStyle,
                    color = textColor,
                )

                is ConnectionState.Connecting -> NetworkLoadingIndicator(
                    textStyle = textStyle,
                    textColor = textColor,
                )

                is ConnectionState.Offline -> Text(
                    text = getString(R.string.stream_compose_disconnected),
                    style = textStyle,
                    color = textColor,
                )
            }

            Timestamp(date = message.updatedAt ?: message.createdAt ?: Date())
        }
    }

    @Composable
    private fun MediaGalleryPreviewOptionsToggle(
        message: Message,
        modifier: Modifier = Modifier,
    ) {
        Icon(
            modifier = modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            mediaGalleryPreviewViewModel.toggleMediaOptions(
                                isShowingOptions = true,
                            )
                        }
                    },
                    enabled = message.id.isNotEmpty(),
                ),
            painter = painterResource(id = R.drawable.stream_compose_ic_menu_vertical),
            contentDescription = stringResource(R.string.stream_compose_image_options),
            tint = if (message.id.isNotEmpty()) ChatTheme.colors.textHighEmphasis else ChatTheme.colors.disabled,
        )
    }


    @Composable
    private fun MediaGalleryPreviewOptions(
        options: List<MediaGalleryPreviewOption>,
        pagerState: PagerState,
        attachments: List<Attachment>,
        modifier: Modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.overlay)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            mediaGalleryPreviewViewModel.toggleMediaOptions(
                                isShowingOptions = false,
                            )
                        }
                    },
                ),
        ) {
            Surface(
                modifier = modifier
                    .padding(16.dp)
                    .width(150.dp)
                    .wrapContentHeight()
                    .align(Alignment.TopEnd),
                shape = RoundedCornerShape(16.dp),
                elevation = 4.dp,
                color = ChatTheme.colors.barsBackground,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, option ->
                        MediaGalleryPreviewOptionItem(
                            mediaGalleryPreviewOption = option,
                            pagerState = pagerState,
                            attachments = attachments,
                        )

                        if (index != options.lastIndex) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(ChatTheme.colors.borders),
                            )
                        }
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun MediaGalleryPreviewOptionItem(
        mediaGalleryPreviewOption: MediaGalleryPreviewOption,
        pagerState: PagerState,
        attachments: List<Attachment>,
    ) {
        val (writePermissionState, downloadPayload) = attachmentDownloadState()
        val context = LocalContext.current

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatTheme.colors.barsBackground)
                .padding(8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            mediaGalleryPreviewViewModel.toggleMediaOptions(isShowingOptions = false)

                            handleMediaAction(
                                context = context,
                                mediaGalleryPreviewAction = mediaGalleryPreviewOption.action,
                                currentPage = pagerState.currentPage,
                                writePermissionState = writePermissionState,
                                downloadPayload = downloadPayload,
                                attachments = attachments,
                            )
                        }
                    },
                    enabled = mediaGalleryPreviewOption.isEnabled,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                modifier = Modifier
                    .size(18.dp),
                painter = mediaGalleryPreviewOption.iconPainter,
                tint = mediaGalleryPreviewOption.iconColor,
                contentDescription = mediaGalleryPreviewOption.title,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = mediaGalleryPreviewOption.title,
                color = mediaGalleryPreviewOption.titleColor,
                style = ChatTheme.typography.bodyBold,
                fontSize = 12.sp,
            )
        }
    }


    @OptIn(ExperimentalPermissionsApi::class)
    private fun handleMediaAction(
        context: Context,
        mediaGalleryPreviewAction: MediaGalleryPreviewAction,
        currentPage: Int,
        attachments: List<Attachment>,
        writePermissionState: PermissionState,
        downloadPayload: MutableState<Attachment?>,
    ) {
        val message = mediaGalleryPreviewAction.message

        when (mediaGalleryPreviewAction) {
            is ShowInChat -> {
                handleResult(
                    MediaGalleryPreviewResult(
                        messageId = message.id,
                        parentMessageId = message.parentId,
                        resultType = MediaGalleryPreviewResultType.SHOW_IN_CHAT,
                    ),
                )
            }

            is Reply -> {
                handleResult(
                    MediaGalleryPreviewResult(
                        messageId = message.id,
                        parentMessageId = message.parentId,
                        resultType = MediaGalleryPreviewResultType.QUOTE,
                    ),
                )
            }

            is Delete -> mediaGalleryPreviewViewModel.deleteCurrentMediaAttachment(attachments[currentPage])
            is SaveMedia -> {
                onDownloadHandleRequest(
                    context = context,
                    payload = attachments[currentPage],
                    permissionState = writePermissionState,
                    downloadPayload = downloadPayload,
                )
            }
        }
    }

    private fun handleResult(result: MediaGalleryPreviewResult) {
        val data = Intent().apply {
            putExtra(KeyMediaGalleryPreviewResult, result)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    @Suppress("LongMethod", "ComplexMethod")
    @Composable
    private fun MediaPreviewContent(
        pagerState: PagerState,
        attachments: List<Attachment>,
        onPlaybackError: () -> Unit,
    ) {
        if (attachments.isEmpty()) {
            finish()
            return
        }

        HorizontalPager(
            modifier = Modifier.background(ChatTheme.colors.appBackground),
            state = pagerState,
            count = attachments.size,
        ) { page ->
            if (attachments[page].isImage()) {
                ImagePreviewContent(attachment = attachments[page], pagerState = pagerState, page = page)
            } else if (attachments[page].isVideo()) {
                VideoPreviewContent(
                    attachment = attachments[page],
                    pagerState = pagerState,
                    page = page,
                    onPlaybackError = onPlaybackError,
                )
            }
        }
    }


    @Composable
    private fun ImagePreviewContent(
        attachment: Attachment,
        pagerState: PagerState,
        page: Int,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {

            var retryHash by remember {
                mutableStateOf(0)
            }

            val data = attachment.imagePreviewUrl
            val painter =
                rememberStreamImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(data)
                        .crossfade(true)
                        .setParameter(key = RetryHash, value = retryHash)
                        .build(),
                )

            val density = LocalDensity.current
            val parentSize = Size(density.run { maxWidth.toPx() }, density.run { maxHeight.toPx() })
            var imageSize by remember { mutableStateOf(Size(0f, 0f)) }

            var currentScale by remember { mutableStateOf(DefaultZoomScale) }
            var translation by remember { mutableStateOf(Offset(0f, 0f)) }

            val scale by animateFloatAsState(targetValue = currentScale)

            onImageNeedsToReload(
                data = data,
                connectionState = mediaGalleryPreviewViewModel.connectionState,
                asyncImagePainterState = painter.state,
            ) {
                retryHash++
            }

            val transformModifier = if (painter.state is AsyncImagePainter.State.Success) {
                val size = painter.intrinsicSize
                Modifier
                    .aspectRatio(size.width / size.height, true)
                    .background(color = ChatTheme.colors.overlay)
            } else {
                Modifier
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                MediaPreviewPlaceHolder(
                    asyncImagePainterState = painter.state,
                    isImage = attachment.isImage(),
                    progressIndicatorStrokeWidth = 6.dp,
                    progressIndicatorFillMaxSizePercentage = 0.2f,
                )

                Image(
                    modifier = transformModifier
                        .graphicsLayer(
                            scaleY = scale,
                            scaleX = scale,
                            translationX = translation.x,
                            translationY = translation.y,
                        )
                        .onGloballyPositioned {
                            imageSize = Size(it.size.width.toFloat(), it.size.height.toFloat())
                        }
                        .pointerInput(Unit) {
                            coroutineScope {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = true)
                                    do {
                                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                                        val zoom = event.calculateZoom()
                                        currentScale = (zoom * currentScale).coerceAtMost(MaxZoomScale)

                                        val maxTranslation = calculateMaxOffset(
                                            imageSize = imageSize,
                                            scale = currentScale,
                                            parentSize = parentSize,
                                        )

                                        val offset = event.calculatePan()
                                        val newTranslationX = translation.x + offset.x * currentScale
                                        val newTranslationY = translation.y + offset.y * currentScale

                                        translation = Offset(
                                            newTranslationX.coerceIn(-maxTranslation.x, maxTranslation.x),
                                            newTranslationY.coerceIn(-maxTranslation.y, maxTranslation.y),
                                        )

                                        if (abs(newTranslationX) < calculateMaxOffsetPerAxis(
                                                imageSize.width,
                                                currentScale,
                                                parentSize.width,
                                            ) || zoom != DefaultZoomScale
                                        ) {
                                            event.changes.forEach { it.consume() }
                                        }
                                    } while (event.changes.any { it.pressed })

                                    if (currentScale < DefaultZoomScale) {
                                        currentScale = DefaultZoomScale
                                    }
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            coroutineScope {
                                awaitEachGesture {
                                    awaitFirstDown()
                                    withTimeoutOrNull(DoubleTapTimeoutMs) {
                                        awaitFirstDown()
                                        currentScale = when {
                                            currentScale == MaxZoomScale -> DefaultZoomScale
                                            currentScale >= MidZoomScale -> MaxZoomScale
                                            else -> MidZoomScale
                                        }

                                        if (currentScale == DefaultZoomScale) {
                                            translation = Offset(0f, 0f)
                                        }
                                    }
                                }
                            }
                        },
                    painter = painter,
                    contentDescription = null,
                )

                Log.d("isCurrentPage", "${page != pagerState.currentPage}")

                if (pagerState.currentPage != page) {
                    currentScale = DefaultZoomScale
                    translation = Offset(0f, 0f)
                }
            }
        }
    }

    @Composable
    private fun VideoPreviewContent(
        attachment: Attachment,
        pagerState: PagerState,
        page: Int,
        onPlaybackError: () -> Unit,
    ) {
        val context = LocalContext.current

        var hasPrepared by remember {
            mutableStateOf(false)
        }

        var userHasClickedPlay by remember {
            mutableStateOf(false)
        }

        var shouldShowProgressBar by remember {
            mutableStateOf(false)
        }

        var shouldShowPreview by remember {
            mutableStateOf(true)
        }

        var shouldShowPlayButton by remember {
            mutableStateOf(true)
        }

        val mediaController = remember {
            createMediaController(context)
        }

        val videoView = remember {
            VideoView(context)
        }

        val contentView = remember {
            val frameLayout = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
            videoView.apply {
                setVideoURI(Uri.parse(attachment.assetUrl))
                this.setMediaController(mediaController)
                setOnErrorListener { _, _, _ ->
                    shouldShowProgressBar = false
                    onPlaybackError()
                    true
                }
                setOnPreparedListener {
                    if (!hasPrepared && userHasClickedPlay && page == pagerState.currentPage) {
                        shouldShowProgressBar = false
                        shouldShowPreview = false
                        mediaController.show()
                    }
                    hasPrepared = true
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
            }
        }

        Box(contentAlignment = Alignment.Center) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                factory = { contentView },
            )

            if (shouldShowPreview) {
                val data = if (ChatTheme.videoThumbnailsEnabled) {
                    attachment.thumbUrl
                } else {
                    null
                }

                val painter = rememberStreamImagePainter(data = data)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Image(
                        modifier = Modifier
                            .clickable {
                                shouldShowProgressBar = true
                                shouldShowPlayButton = false
                                userHasClickedPlay = true
                                if (hasPrepared) {
                                    shouldShowProgressBar = false
                                    shouldShowPreview = false
                                    mediaController.show()
                                }
                                videoView.start()
                            }
                            .fillMaxSize()
                            .background(color = Color.Black),
                        painter = painter,
                        contentDescription = null,
                    )

                    if (shouldShowPlayButton) {
                        PlayButton(
                            modifier = Modifier
                                .shadow(6.dp, shape = CircleShape)
                                .background(color = Color.White, shape = CircleShape)
                                .size(
                                    width = 42.dp,
                                    height = 42.dp,
                                ),
                            contentDescription = getString(R.string.stream_compose_cd_play_button),
                        )
                    }
                }
            }

            if (shouldShowProgressBar) {
                LoadingIndicator()
            }
        }

        if (page != pagerState.currentPage) {
            shouldShowPlayButton = true
            shouldShowPreview = true
            shouldShowProgressBar = false
            mediaController.hide()
        }
    }

    private fun createMediaController(
        context: Context,
    ): MediaController {
        return object : MediaController(context) {}
    }

    private fun calculateMaxOffset(imageSize: Size, scale: Float, parentSize: Size): Offset {
        val maxTranslationY = calculateMaxOffsetPerAxis(imageSize.height, scale, parentSize.height)
        val maxTranslationX = calculateMaxOffsetPerAxis(imageSize.width, scale, parentSize.width)
        return Offset(maxTranslationX, maxTranslationY)
    }

    private fun calculateMaxOffsetPerAxis(axisSize: Float, scale: Float, parentAxisSize: Float): Float {
        return (axisSize * scale - parentAxisSize).coerceAtLeast(0f) / 2
    }

    @Suppress("LongMethod")
    @Composable
    private fun MediaGalleryPreviewBottomBar(attachments: List<Attachment>, pagerState: PagerState) {
        val attachmentCount = attachments.size

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            elevation = 4.dp,
            color = ChatTheme.colors.barsBackground,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            val attachment = attachments[pagerState.currentPage]

                            when {
                                mediaGalleryPreviewViewModel.isSharingInProgress -> {
                                    fileSharingJob?.cancel()
                                    mediaGalleryPreviewViewModel.isSharingInProgress = false
                                }

                                attachment.fileSize >= MaxUnpromptedFileSize -> {
                                    val result = StreamFileUtil.getFileFromCache(
                                        context = applicationContext,
                                        attachment = attachment,
                                    )

                                    when (result) {
                                        is Result.Success -> shareAttachment(
                                            mediaUri = result.value,
                                            attachmentType = attachment.type,
                                        )

                                        is Result.Failure ->
                                            mediaGalleryPreviewViewModel.promptedAttachment =
                                                attachment
                                    }
                                }

                                else -> shareAttachment(attachment)
                            }
                        }
                    },
                    enabled = mediaGalleryPreviewViewModel.connectionState is ConnectionState.Connected,
                ) {
                    val shareIcon = if (!mediaGalleryPreviewViewModel.isSharingInProgress) {
                        R.drawable.stream_compose_ic_share
                    } else {
                        R.drawable.stream_compose_ic_clear
                    }

                    Icon(
                        painter = painterResource(id = shareIcon),
                        contentDescription = stringResource(id = R.string.stream_compose_image_preview_share),
                        tint = if (mediaGalleryPreviewViewModel.connectionState is ConnectionState.Connected) {
                            ChatTheme.colors.textHighEmphasis
                        } else {
                            ChatTheme.colors.disabled
                        },
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (mediaGalleryPreviewViewModel.isSharingInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp,
                            color = ChatTheme.colors.primaryAccent,
                        )
                    }

                    val text = if (!mediaGalleryPreviewViewModel.isSharingInProgress) {
                        stringResource(
                            id = R.string.stream_compose_image_order,
                            pagerState.currentPage + 1,
                            attachmentCount,
                        )
                    } else {
                        stringResource(id = R.string.stream_compose_media_gallery_preview_preparing)
                    }

                    Text(
                        text = text,
                        style = ChatTheme.typography.title3Bold,
                        color = ChatTheme.colors.textHighEmphasis,
                    )
                }

                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            mediaGalleryPreviewViewModel.toggleGallery(
                                isShowingGallery = true,
                            )
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_gallery),
                        contentDescription = stringResource(id = R.string.stream_compose_image_preview_photos),
                        tint = ChatTheme.colors.textHighEmphasis,
                    )
                }
            }
        }
    }

    @Composable
    private fun defaultMediaOptions(message: Message): List<MediaGalleryPreviewOption> {
        val user by mediaGalleryPreviewViewModel.user.collectAsState()

        val isChatConnected by remember(mediaGalleryPreviewViewModel.connectionState) {
            derivedStateOf {
                mediaGalleryPreviewViewModel.connectionState is ConnectionState.Connected
            }
        }

        val saveMediaColor =
            if (isChatConnected) {
                ChatTheme.colors.textHighEmphasis
            } else {
                ChatTheme.colors.disabled
            }

        val options = mutableListOf(
            MediaGalleryPreviewOption(
                title = stringResource(id = R.string.stream_compose_media_gallery_preview_reply),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_reply),
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = Reply(message),
                isEnabled = true,
            ),
            MediaGalleryPreviewOption(
                title = stringResource(id = R.string.stream_compose_media_gallery_preview_show_in_chat),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_show_in_chat),
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = ShowInChat(message),
                isEnabled = true,
            ),
            MediaGalleryPreviewOption(
                title = stringResource(id = R.string.stream_compose_media_gallery_preview_save_image),
                titleColor = saveMediaColor,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_download),
                iconColor = saveMediaColor,
                action = SaveMedia(message),
                isEnabled = isChatConnected,
            ),
        )

        if (message.user.id == user?.id) {
            val deleteColor =
                if (mediaGalleryPreviewViewModel.connectionState is ConnectionState.Connected) {
                    ChatTheme.colors.errorAccent
                } else {
                    ChatTheme.colors.disabled
                }

            options.add(
                MediaGalleryPreviewOption(
                    title = stringResource(id = R.string.stream_compose_media_gallery_preview_delete),
                    titleColor = deleteColor,
                    iconPainter = painterResource(id = R.drawable.stream_compose_ic_delete),
                    iconColor = deleteColor,
                    action = Delete(message),
                    isEnabled = isChatConnected,
                ),
            )
        }

        return options
    }

    private fun shareAttachment(attachment: Attachment) {
        fileSharingJob = lifecycleScope.launch {
            mediaGalleryPreviewViewModel.isSharingInProgress = true

            when (attachment.type) {
                AttachmentType.IMAGE -> shareImage(attachment)
                AttachmentType.VIDEO -> shareVideo(attachment)
                else -> toastFailedShare()
            }
        }
    }

    private suspend fun shareImage(attachment: Attachment) {
        val attachmentUrl = attachment.imagePreviewUrl

        if (attachmentUrl != null) {
            StreamImageLoader.instance().loadAsBitmap(
                context = applicationContext,
                url = attachmentUrl,
            )?.let {
                val imageUri = StreamFileUtil.writeImageToSharableFile(applicationContext, it)

                shareAttachment(
                    mediaUri = imageUri,
                    attachmentType = attachment.type,
                )
            }
        } else {
            mediaGalleryPreviewViewModel.isSharingInProgress = false
            toastFailedShare()
        }
    }

    private fun toastFailedShare() {
        Toast.makeText(
            applicationContext,
            applicationContext.getString(R.string.stream_compose_media_gallery_preview_could_not_share_attachment),
            Toast.LENGTH_SHORT,
        ).show()
    }

    private fun shareAttachment(
        mediaUri: Uri?,
        attachmentType: String?,
    ) {
        mediaGalleryPreviewViewModel.isSharingInProgress = false

        if (mediaUri == null) {
            toastFailedShare()
            return
        }

        val mediaType = when (attachmentType) {
            AttachmentType.IMAGE -> "image/*"
            AttachmentType.VIDEO -> "video/*"
            else -> {
                toastFailedShare()
                return
            }
        }

        ContextCompat.startActivity(
            this,
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = mediaType
                    putExtra(Intent.EXTRA_STREAM, mediaUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                getString(R.string.stream_compose_attachment_gallery_share),
            ),
            null,
        )
    }


    private suspend fun shareVideo(attachment: Attachment) {
        val result = withContext(DispatcherProvider.IO) {
            StreamFileUtil.writeFileToShareableFile(
                context = applicationContext,
                attachment = attachment,
            )
        }

        mediaGalleryPreviewViewModel.isSharingInProgress = false

        when (result) {
            is Result.Success -> shareAttachment(
                mediaUri = result.value,
                attachmentType = attachment.type,
            )

            is Result.Failure -> toastFailedShare()
        }
    }

    @Composable
    private fun MediaGallery(
        pagerState: PagerState,
        attachments: List<Attachment>,
        modifier: Modifier = Modifier,
    ) {
        val message = mediaGalleryPreviewViewModel.message

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.overlay)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            mediaGalleryPreviewViewModel.toggleGallery(
                                isShowingGallery = false,
                            )
                        }
                    },
                ),
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                    ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = 4.dp,
                color = ChatTheme.colors.barsBackground,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MediaGalleryHeader()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(ColumnCount),
                        content = {
                            itemsIndexed(attachments) { index, attachment ->
                                MediaGalleryItem(index, attachment, message.user, pagerState)
                            }
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun MediaGalleryHeader() {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(8.dp)
                    .clickable(
                        indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = remember(mediaGalleryPreviewViewModel) {
                            {
                                mediaGalleryPreviewViewModel.toggleGallery(
                                    isShowingGallery = false,
                                )
                            }
                        },
                    ),
                painter = painterResource(id = R.drawable.stream_compose_ic_close),
                contentDescription = stringResource(id = R.string.stream_compose_cancel),
                tint = ChatTheme.colors.textHighEmphasis,
            )

            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.stream_compose_image_preview_photos),
                style = ChatTheme.typography.title3Bold,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }
    }

    @Composable
    private fun MediaGalleryItem(
        index: Int,
        attachment: Attachment,
        user: User,
        pagerState: PagerState,
    ) {
        val isImage = attachment.isImage()
        val isVideo = attachment.isVideo()
        var retryHash by remember {
            mutableStateOf(0)
        }

        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable {
                    coroutineScope.launch {
                        mediaGalleryPreviewViewModel.toggleGallery(isShowingGallery = false)
                        pagerState.animateScrollToPage(index)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            val data =
                if (isImage || (isVideo && ChatTheme.videoThumbnailsEnabled)) {
                    attachment.imagePreviewUrl?.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing)
                } else {
                    null
                }

            val painter = rememberStreamImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(data)
                    .setParameter(RetryHash, retryHash.toString())
                    .build(),
            )

            onImageNeedsToReload(
                data = data,
                connectionState = mediaGalleryPreviewViewModel.connectionState,
                asyncImagePainterState = painter.state,
            ) {
                retryHash++
            }

            val backgroundColor = if (isImage) {
                ChatTheme.colors.imageBackgroundMediaGalleryPicker
            } else {
                ChatTheme.colors.videoBackgroundMediaGalleryPicker
            }

            Image(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize()
                    .background(color = backgroundColor),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            MediaPreviewPlaceHolder(
                asyncImagePainterState = painter.state,
                isImage = isImage,
                progressIndicatorStrokeWidth = 3.dp,
                progressIndicatorFillMaxSizePercentage = 0.3f,
            )

            Avatar(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(24.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = ChatTheme.shapes.avatar,
                    )
                    .shadow(
                        elevation = 4.dp,
                        shape = ChatTheme.shapes.avatar,
                    ),
                imageUrl = user.image,
                initials = user.initials,
            )

            if (isVideo && painter.state !is AsyncImagePainter.State.Loading) {
                PlayButton(
                    modifier = Modifier
                        .shadow(6.dp, shape = CircleShape)
                        .background(color = Color.White, shape = CircleShape)
                        .fillMaxSize(0.2f),
                    contentDescription = getString(R.string.stream_compose_cd_play_button),
                )
            }
        }
    }

    private fun Intent.createStreamCdnImageResizing(): StreamCdnImageResizing {
        val imageResizingEnabled = getBooleanExtra(KeyImageResizingEnabled, false)

        val resizedWidthPercentage = getFloatExtra(KeyStreamCdnResizeImagedWidthPercentage, 1f)
        val resizedHeightPercentage = getFloatExtra(KeyStreamCdnResizeImagedHeightPercentage, 1f)

        val resizeModeEnumValue = getStringExtra(KeyStreamCdnResizeImageMode)
        val resizeMode =
            if (resizeModeEnumValue != null) StreamCdnResizeImageMode.valueOf(value = resizeModeEnumValue) else null

        val cropModeEnumValue = getStringExtra(KeyStreamCdnResizeImageCropMode)
        val cropMode =
            if (cropModeEnumValue != null) StreamCdnCropImageMode.valueOf(value = cropModeEnumValue) else null

        return StreamCdnImageResizing(
            imageResizingEnabled = imageResizingEnabled,
            resizedWidthPercentage = resizedWidthPercentage,
            resizedHeightPercentage = resizedHeightPercentage,
            cropMode = cropMode,
            resizeMode = resizeMode,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        StreamFileUtil.clearStreamCache(context = applicationContext)
    }

    public companion object {
        private const val MaxUnpromptedFileSize = 10 * 1024 * 1024
        private const val ColumnCount = 3
        private const val KeyMediaGalleryPreviewActivityState: String = "mediaGalleryPreviewActivityState"
        private const val KeyVideoThumbnailsEnabled: String = "videoThumbnailsEnabled"
        private const val KeyImageResizingEnabled: String = "imageResizingEnabled"
        private const val KeyStreamCdnResizeImagedWidthPercentage: String = "streamCdnResizeImagedWidthPercentage"
        private const val KeyStreamCdnResizeImagedHeightPercentage: String = "streamCdnResizeImagedHeightPercentage"
        private const val KeyStreamCdnResizeImageMode: String = "streamCdnResizeImageMode"
        private const val KeyStreamCdnResizeImageCropMode: String = "streamCdnResizeImageCropMode"
        private const val KeyAttachmentPosition: String = "attachmentPosition"
        public const val KeyMediaGalleryPreviewResult: String = "mediaGalleryPreviewResult"
        private const val KeySkipEnrichUrl: String = "skipEnrichUrl"
        private const val DoubleTapTimeoutMs: Long = 500L
        private const val MaxZoomScale: Float = 3f
        private const val MidZoomScale: Float = 2f
        private const val DefaultZoomScale: Float = 1f
        public fun getIntent(
            context: Context,
            message: Message,
            attachmentPosition: Int,
            videoThumbnailsEnabled: Boolean,
            streamCdnImageResizing: StreamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing(),
            skipEnrichUrl: Boolean = false,
        ): Intent {
            return Intent(context, MediaGalleryPreviewActivity::class.java).apply {
                val mediaGalleryPreviewActivityState = message.toMediaGalleryPreviewActivityState()

                putExtra(KeyMediaGalleryPreviewActivityState, mediaGalleryPreviewActivityState)
                putExtra(KeyAttachmentPosition, attachmentPosition)
                putExtra(KeyVideoThumbnailsEnabled, videoThumbnailsEnabled)
                putExtra(KeyImageResizingEnabled, streamCdnImageResizing.imageResizingEnabled)
                putExtra(KeyStreamCdnResizeImagedWidthPercentage, streamCdnImageResizing.resizedWidthPercentage)
                putExtra(KeyStreamCdnResizeImagedHeightPercentage, streamCdnImageResizing.resizedHeightPercentage)
                putExtra(KeyStreamCdnResizeImageMode, streamCdnImageResizing.resizeMode?.name)
                putExtra(KeyStreamCdnResizeImageCropMode, streamCdnImageResizing.cropMode?.name)
                putExtra(KeySkipEnrichUrl, skipEnrichUrl)
            }
        }
    }
}