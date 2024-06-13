package com.example.composechatsample.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.composechatsample.R

@Immutable
data class StreamColors(
    public val textHighEmphasis: Color,
    public val textHighEmphasisInverse: Color,
    public val textLowEmphasis: Color,
    public val disabled: Color,
    public val borders: Color,
    public val inputBackground: Color,
    public val appBackground: Color,
    public val barsBackground: Color,
    public val linkBackground: Color,
    public val overlay: Color,
    public val overlayDark: Color,
    public val primaryAccent: Color,
    public val errorAccent: Color,
    public val infoAccent: Color,
    public val highlight: Color,
    @Deprecated("Use MessageTheme.backgroundColor instead", level = DeprecationLevel.ERROR)
    public val ownMessagesBackground: Color,
    @Deprecated("Use MessageTheme.backgroundColor instead", level = DeprecationLevel.ERROR)
    public val otherMessagesBackground: Color,
    @Deprecated("Use MessageTheme.deletedBackgroundColor instead", level = DeprecationLevel.ERROR)
    public val deletedMessagesBackground: Color,
    public val giphyMessageBackground: Color,
    public val threadSeparatorGradientStart: Color,
    public val threadSeparatorGradientEnd: Color,
    @Deprecated("Use MessageTheme.textStyle.color instead", level = DeprecationLevel.ERROR)
    public val ownMessageText: Color = textHighEmphasis,
    @Deprecated("Use MessageTheme.textStyle.color instead", level = DeprecationLevel.ERROR)
    public val otherMessageText: Color = textHighEmphasis,
    public val imageBackgroundMessageList: Color,
    public val imageBackgroundMediaGalleryPicker: Color,
    public val videoBackgroundMessageList: Color,
    public val videoBackgroundMediaGalleryPicker: Color,
    public val showMoreOverlay: Color,
    public val showMoreCountText: Color,
    public val ownMessageQuotedBackground: Color = otherMessagesBackground,
    public val otherMessageQuotedBackground: Color = ownMessagesBackground,
    public val ownMessageQuotedText: Color = textHighEmphasis,
    public val otherMessageQuotedText: Color = textHighEmphasis,
) {

    public companion object {
        @Composable
        public fun defaultColors(): StreamColors = StreamColors(
            textHighEmphasis = colorResource(R.color.stream_compose_text_high_emphasis),
            textHighEmphasisInverse = colorResource(R.color.stream_compose_text_high_emphasis_inverse),
            textLowEmphasis = colorResource(R.color.stream_compose_text_low_emphasis),
            disabled = colorResource(R.color.stream_compose_disabled),
            borders = colorResource(R.color.stream_compose_borders),
            inputBackground = colorResource(R.color.stream_compose_input_background),
            appBackground = colorResource(R.color.stream_compose_app_background),
            barsBackground = colorResource(R.color.stream_compose_bars_background),
            linkBackground = colorResource(R.color.stream_compose_link_background),
            overlay = colorResource(R.color.stream_compose_overlay_regular),
            overlayDark = colorResource(R.color.stream_compose_overlay_dark),
            primaryAccent = colorResource(R.color.stream_compose_primary_accent),
            errorAccent = colorResource(R.color.stream_compose_error_accent),
            infoAccent = colorResource(R.color.stream_compose_info_accent),
            highlight = colorResource(R.color.stream_compose_highlight),
            ownMessagesBackground = colorResource(R.color.stream_compose_borders),
            otherMessagesBackground = colorResource(R.color.stream_compose_bars_background),
            deletedMessagesBackground = colorResource(R.color.stream_compose_input_background),
            giphyMessageBackground = colorResource(R.color.stream_compose_bars_background),
            threadSeparatorGradientStart = colorResource(R.color.stream_compose_input_background),
            threadSeparatorGradientEnd = colorResource(R.color.stream_compose_app_background),
            ownMessageText = colorResource(R.color.stream_compose_text_high_emphasis),
            otherMessageText = colorResource(R.color.stream_compose_text_high_emphasis),
            imageBackgroundMessageList = colorResource(R.color.stream_compose_input_background),
            imageBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background),
            videoBackgroundMessageList = colorResource(R.color.stream_compose_input_background),
            videoBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background),
            showMoreOverlay = colorResource(R.color.stream_compose_show_more_overlay),
            showMoreCountText = colorResource(R.color.stream_compose_show_more_text),
        )

        @Composable
        public fun defaultDarkColors(): StreamColors = StreamColors(
            textHighEmphasis = colorResource(R.color.stream_compose_text_high_emphasis_dark),
            textHighEmphasisInverse = colorResource(R.color.stream_compose_text_high_emphasis_inverse_dark),
            textLowEmphasis = colorResource(R.color.stream_compose_text_low_emphasis_dark),
            disabled = colorResource(R.color.stream_compose_disabled_dark),
            borders = colorResource(R.color.stream_compose_borders_dark),
            inputBackground = colorResource(R.color.stream_compose_input_background_dark),
            appBackground = colorResource(R.color.stream_compose_app_background_dark),
            barsBackground = colorResource(R.color.stream_compose_bars_background_dark),
            linkBackground = colorResource(R.color.stream_compose_link_background_dark),
            overlay = colorResource(R.color.stream_compose_overlay_regular_dark),
            overlayDark = colorResource(R.color.stream_compose_overlay_dark_dark),
            primaryAccent = colorResource(R.color.stream_compose_primary_accent_dark),
            errorAccent = colorResource(R.color.stream_compose_error_accent_dark),
            infoAccent = colorResource(R.color.stream_compose_info_accent_dark),
            highlight = colorResource(R.color.stream_compose_highlight_dark),
            ownMessagesBackground = colorResource(R.color.stream_compose_borders_dark),
            otherMessagesBackground = colorResource(R.color.stream_compose_bars_background_dark),
            deletedMessagesBackground = colorResource(R.color.stream_compose_input_background_dark),
            giphyMessageBackground = colorResource(R.color.stream_compose_bars_background_dark),
            threadSeparatorGradientStart = colorResource(R.color.stream_compose_input_background_dark),
            threadSeparatorGradientEnd = colorResource(R.color.stream_compose_app_background_dark),
            ownMessageText = colorResource(R.color.stream_compose_text_high_emphasis_dark),
            otherMessageText = colorResource(R.color.stream_compose_text_high_emphasis_dark),
            imageBackgroundMessageList = colorResource(R.color.stream_compose_input_background_dark),
            imageBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background_dark),
            videoBackgroundMessageList = colorResource(R.color.stream_compose_input_background_dark),
            videoBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background_dark),
            showMoreOverlay = colorResource(R.color.stream_compose_show_more_overlay_dark),
            showMoreCountText = colorResource(R.color.stream_compose_show_more_text_dark),
        )
    }
}