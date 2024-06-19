package com.example.composechatsample.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun NetworkLoadingIndicator(
    modifier: Modifier = Modifier,
    spinnerSize: Dp = 18.dp,
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    textColor: Color = ChatTheme.colors.textHighEmphasis,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(spinnerSize),
            strokeWidth = 2.dp,
            color = ChatTheme.colors.primaryAccent,
        )

        Text(
            text = stringResource(id = R.string.stream_compose_waiting_for_network),
            style = textStyle,
            color = textColor,
        )
    }
}