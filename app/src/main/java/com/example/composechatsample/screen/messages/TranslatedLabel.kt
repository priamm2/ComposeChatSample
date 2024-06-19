package com.example.composechatsample.screen.messages

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.composechatsample.R
import com.example.composechatsample.ui.theme.ChatTheme
import java.util.Locale

@Composable
public fun TranslatedLabel(
    translatedTo: String,
    modifier: Modifier = Modifier,
) {
    val textLanguageMetaInfo = if (LocalInspectionMode.current) {
        "Translated to $translatedTo"
    } else {
        val languageDisplayName = Locale(translatedTo).getDisplayName(Locale.getDefault())
        LocalContext.current.getString(R.string.stream_compose_message_list_translated, languageDisplayName)
    }

    Text(
        modifier = modifier,
        text = textLanguageMetaInfo,
        style = ChatTheme.typography.footnote,
        color = ChatTheme.colors.textLowEmphasis,
    )
}