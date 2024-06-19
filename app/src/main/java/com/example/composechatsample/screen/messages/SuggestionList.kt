package com.example.composechatsample.screen.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.window.Popup
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun SuggestionList(
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.suggestionList,
    contentPadding: PaddingValues = PaddingValues(vertical = ChatTheme.dimens.suggestionListPadding),
    headerContent: @Composable () -> Unit = {},
    centerContent: @Composable () -> Unit,
) {
    Popup(popupPositionProvider = AboveAnchorPopupPositionProvider()) {
        Card(
            modifier = modifier,
            elevation = ChatTheme.dimens.suggestionListElevation,
            shape = shape,
            backgroundColor = ChatTheme.colors.barsBackground,
        ) {
            Column(Modifier.padding(contentPadding)) {
                headerContent()

                centerContent()
            }
        }
    }
}