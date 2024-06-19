package com.example.composechatsample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.example.composechatsample.common.DefaultStreamMediaRecorder
import com.example.composechatsample.common.StreamCdnImageResizing
import com.example.composechatsample.common.StreamCoilImageLoaderFactory
import com.example.composechatsample.common.StreamMediaRecorder
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.LocalStreamImageLoader
import com.example.composechatsample.core.VersionPrefixHeader
import com.example.composechatsample.helper.DateFormatter

private val LocalColors = compositionLocalOf<StreamColors> {
    error("No colors provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalDimens = compositionLocalOf<StreamDimens> {
    error("No dimens provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalTypography = compositionLocalOf<StreamTypography> {
    error("No typography provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalShapes = compositionLocalOf<StreamShapes> {
    error("No shapes provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalAttachmentFactories = compositionLocalOf<List<AttachmentFactory>> {
    error("No attachment factories provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalAttachmentPreviewHandlers = compositionLocalOf<List<AttachmentPreviewHandler>> {
    error("No attachment preview handlers provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalQuotedAttachmentFactories = compositionLocalOf<List<AttachmentFactory>> {
    error("No quoted attachment factories provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalReactionIconFactory = compositionLocalOf<ReactionIconFactory> {
    error("No reaction icon factory provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalDateFormatter = compositionLocalOf<DateFormatter> {
    error("No DateFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalChannelNameFormatter = compositionLocalOf<ChannelNameFormatter> {
    error("No ChannelNameFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessagePreviewFormatter = compositionLocalOf<MessagePreviewFormatter> {
    error("No MessagePreviewFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageTextFormatter = compositionLocalOf<MessageTextFormatter> {
    error("No MessageTextFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalQuotedMessageTextFormatter = compositionLocalOf<QuotedMessageTextFormatter> {
    error("No QuotedMessageTextFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalSearchResultNameFormatter = compositionLocalOf<SearchResultNameFormatter> {
    error("No SearchResultNameFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageAlignmentProvider = compositionLocalOf<MessageAlignmentProvider> {
    error("No MessageAlignmentProvider provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageOptionsUserReactionAlignment = compositionLocalOf<MessageOptionsUserReactionAlignment> {
    error(
        "No LocalMessageOptionsUserReactionAlignment provided! Make sure to wrap all usages of Stream components " +
                "in a ChatTheme.",
    )
}

private val LocalAttachmentsPickerTabFactories = compositionLocalOf<List<AttachmentsPickerTabFactory>> {
    error("No attachments picker tab factories provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}

private val LocalVideoThumbnailsEnabled = compositionLocalOf<Boolean> {
    error(
        "No videoThumbnailsEnabled Boolean provided! " +
                "Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalStreamCdnImageResizing = compositionLocalOf<StreamCdnImageResizing> {
    error(
        "No StreamCdnImageResizing provided! " +
                "Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalReadCountEnabled = compositionLocalOf<Boolean> {
    error(
        "No readCountEnabled Boolean provided! " +
                "Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalOwnMessageTheme = compositionLocalOf<MessageTheme> {
    error("No OwnMessageTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalOtherMessageTheme = compositionLocalOf<MessageTheme> {
    error("No OtherMessageTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageDateSeparatorTheme = compositionLocalOf<MessageDateSeparatorTheme> {
    error("No MessageDateSeparatorTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageUnreadSeparatorTheme = compositionLocalOf<MessageUnreadSeparatorTheme> {
    error("No MessageUnreadSeparatorTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageComposerTheme = compositionLocalOf<MessageComposerTheme> {
    error("No MessageComposerTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalAutoTranslationEnabled = compositionLocalOf<Boolean> {
    error(
        "No AutoTranslationEnabled Boolean provided! " +
                "Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalComposerLinkPreviewEnabled = compositionLocalOf<Boolean> {
    error(
        "No ComposerLinkPreviewEnabled Boolean provided! " +
                "Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalStreamMediaRecorder = compositionLocalOf<StreamMediaRecorder> {
    error("No StreamMediaRecorder provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun ChatTheme(
    isInDarkMode: Boolean = isSystemInDarkTheme(),
    autoTranslationEnabled: Boolean = false,
    isComposerLinkPreviewEnabled: Boolean = false,
    colors: StreamColors = if (isInDarkMode) StreamColors.defaultDarkColors() else StreamColors.defaultColors(),
    dimens: StreamDimens = StreamDimens.defaultDimens(),
    typography: StreamTypography = StreamTypography.defaultTypography(),
    shapes: StreamShapes = StreamShapes.defaultShapes(),
    rippleTheme: RippleTheme = StreamRippleTheme,
    attachmentFactories: List<AttachmentFactory> = StreamAttachmentFactories.defaultFactories(),
    attachmentPreviewHandlers: List<AttachmentPreviewHandler> =
        AttachmentPreviewHandler.defaultAttachmentHandlers(LocalContext.current),
    quotedAttachmentFactories: List<AttachmentFactory> = StreamAttachmentFactories.defaultQuotedFactories(),
    reactionIconFactory: ReactionIconFactory = ReactionIconFactory.defaultFactory(),
    allowUIAutomationTest: Boolean = false,
    dateFormatter: DateFormatter = DateFormatter.from(LocalContext.current),
    channelNameFormatter: ChannelNameFormatter = ChannelNameFormatter.defaultFormatter(LocalContext.current),
    messagePreviewFormatter: MessagePreviewFormatter = MessagePreviewFormatter.defaultFormatter(
        context = LocalContext.current,
        typography = typography,
        attachmentFactories = attachmentFactories,
        autoTranslationEnabled = autoTranslationEnabled,
    ),
    searchResultNameFormatter: SearchResultNameFormatter = SearchResultNameFormatter.defaultFormatter(),
    imageLoaderFactory: StreamCoilImageLoaderFactory = StreamCoilImageLoaderFactory.defaultFactory(),
    messageAlignmentProvider: MessageAlignmentProvider = MessageAlignmentProvider.defaultMessageAlignmentProvider(),
    messageOptionsUserReactionAlignment: MessageOptionsUserReactionAlignment = MessageOptionsUserReactionAlignment.END,
    attachmentsPickerTabFactories: List<AttachmentsPickerTabFactory> = AttachmentsPickerTabFactories.defaultFactories(),
    videoThumbnailsEnabled: Boolean = true,
    streamCdnImageResizing: StreamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing(),
    readCountEnabled: Boolean = true,
    ownMessageTheme: MessageTheme = MessageTheme.defaultOwnTheme(
        typography = typography,
        colors = colors,
    ),
    otherMessageTheme: MessageTheme = MessageTheme.defaultOtherTheme(
        typography = typography,
        colors = colors,
    ),
    messageDateSeparatorTheme: MessageDateSeparatorTheme = MessageDateSeparatorTheme.defaultTheme(
        typography = typography,
        colors = colors,
    ),
    messageUnreadSeparatorTheme: MessageUnreadSeparatorTheme = MessageUnreadSeparatorTheme.defaultTheme(
        typography = typography,
        colors = colors,
    ),
    messageComposerTheme: MessageComposerTheme = MessageComposerTheme.defaultTheme(
        typography = typography,
        shapes = shapes,
        colors = colors,
    ),
    messageTextFormatter: MessageTextFormatter = MessageTextFormatter.defaultFormatter(
        autoTranslationEnabled = autoTranslationEnabled,
        typography = typography,
        colors = colors,
        ownMessageTheme = ownMessageTheme,
        otherMessageTheme = otherMessageTheme,
    ),
    quotedMessageTextFormatter: QuotedMessageTextFormatter = QuotedMessageTextFormatter.defaultFormatter(
        autoTranslationEnabled = autoTranslationEnabled,
        context = LocalContext.current,
        typography = typography,
        colors = colors,
        ownMessageTheme = ownMessageTheme,
        otherMessageTheme = otherMessageTheme,
    ),
    streamMediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder(LocalContext.current),
    content: @Composable () -> Unit,
) {
    LaunchedEffect(Unit) {
        ChatClient.VERSION_PREFIX_HEADER = VersionPrefixHeader.Compose
    }

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalDimens provides dimens,
        LocalTypography provides typography,
        LocalShapes provides shapes,
        LocalRippleTheme provides rippleTheme,
        LocalAttachmentFactories provides attachmentFactories,
        LocalAttachmentPreviewHandlers provides attachmentPreviewHandlers,
        LocalQuotedAttachmentFactories provides quotedAttachmentFactories,
        LocalReactionIconFactory provides reactionIconFactory,
        LocalDateFormatter provides dateFormatter,
        LocalChannelNameFormatter provides channelNameFormatter,
        LocalMessagePreviewFormatter provides messagePreviewFormatter,
        LocalMessageTextFormatter provides messageTextFormatter,
        LocalQuotedMessageTextFormatter provides quotedMessageTextFormatter,
        LocalSearchResultNameFormatter provides searchResultNameFormatter,
        LocalOwnMessageTheme provides ownMessageTheme,
        LocalOtherMessageTheme provides otherMessageTheme,
        LocalMessageDateSeparatorTheme provides messageDateSeparatorTheme,
        LocalMessageUnreadSeparatorTheme provides messageUnreadSeparatorTheme,
        LocalMessageComposerTheme provides messageComposerTheme,
        LocalStreamImageLoader provides imageLoaderFactory.imageLoader(LocalContext.current.applicationContext),
        LocalMessageAlignmentProvider provides messageAlignmentProvider,
        LocalMessageOptionsUserReactionAlignment provides messageOptionsUserReactionAlignment,
        LocalAttachmentsPickerTabFactories provides attachmentsPickerTabFactories,
        LocalVideoThumbnailsEnabled provides videoThumbnailsEnabled,
        LocalStreamCdnImageResizing provides streamCdnImageResizing,
        LocalReadCountEnabled provides readCountEnabled,
        LocalStreamMediaRecorder provides streamMediaRecorder,
        LocalAutoTranslationEnabled provides autoTranslationEnabled,
        LocalComposerLinkPreviewEnabled provides isComposerLinkPreviewEnabled,
    ) {
        if (allowUIAutomationTest) {
            Box(
                modifier = Modifier.semantics { testTagsAsResourceId = allowUIAutomationTest },
            ) {
                content()
            }
        } else {
            content()
        }
    }
}

public object ChatTheme {
    public val colors: StreamColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    public val dimens: StreamDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalDimens.current

    public val typography: StreamTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    public val shapes: StreamShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current

    public val attachmentFactories: List<AttachmentFactory>
        @Composable
        @ReadOnlyComposable
        get() = LocalAttachmentFactories.current

    public val attachmentPreviewHandlers: List<AttachmentPreviewHandler>
        @Composable
        @ReadOnlyComposable
        get() = LocalAttachmentPreviewHandlers.current

    public val quotedAttachmentFactories: List<AttachmentFactory>
        @Composable
        @ReadOnlyComposable
        get() = LocalQuotedAttachmentFactories.current

    public val reactionIconFactory: ReactionIconFactory
        @Composable
        @ReadOnlyComposable
        get() = LocalReactionIconFactory.current

    public val dateFormatter: DateFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalDateFormatter.current

    public val channelNameFormatter: ChannelNameFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalChannelNameFormatter.current

    public val messagePreviewFormatter: MessagePreviewFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalMessagePreviewFormatter.current

    public val messageTextFormatter: MessageTextFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageTextFormatter.current

    public val quotedMessageTextFormatter: QuotedMessageTextFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalQuotedMessageTextFormatter.current

    public val searchResultNameFormatter: SearchResultNameFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalSearchResultNameFormatter.current

    public val messageAlignmentProvider: MessageAlignmentProvider
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageAlignmentProvider.current

    public val messageOptionsUserReactionAlignment: MessageOptionsUserReactionAlignment
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageOptionsUserReactionAlignment.current

    public val attachmentsPickerTabFactories: List<AttachmentsPickerTabFactory>
        @Composable
        @ReadOnlyComposable
        get() = LocalAttachmentsPickerTabFactories.current

    public val videoThumbnailsEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalVideoThumbnailsEnabled.current

    public val streamCdnImageResizing: StreamCdnImageResizing
        @Composable
        @ReadOnlyComposable
        get() = LocalStreamCdnImageResizing.current

    public val readCountEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalReadCountEnabled.current

    public val ownMessageTheme: MessageTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalOwnMessageTheme.current

    public val otherMessageTheme: MessageTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalOtherMessageTheme.current

    public val messageDateSeparatorTheme: MessageDateSeparatorTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageDateSeparatorTheme.current

    public val messageUnreadSeparatorTheme: MessageUnreadSeparatorTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageUnreadSeparatorTheme.current

    public val messageComposerTheme: MessageComposerTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageComposerTheme.current

    public val autoTranslationEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAutoTranslationEnabled.current

    public val isComposerLinkPreviewEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalComposerLinkPreviewEnabled.current

    public val streamMediaRecorder: StreamMediaRecorder
        @Composable
        @ReadOnlyComposable
        get() = LocalStreamMediaRecorder.current
}