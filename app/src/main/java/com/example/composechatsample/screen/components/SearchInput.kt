package com.example.composechatsample.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun SearchInput(
    query: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchStarted: () -> Unit = {},
    leadingIcon: @Composable RowScope.() -> Unit = { DefaultSearchLeadingIcon() },
    label: @Composable () -> Unit = { DefaultSearchLabel() },
) {
    var isFocused by remember { mutableStateOf(false) }

    val trailingIcon: (@Composable RowScope.() -> Unit)? = if (isFocused && query.isNotEmpty()) {
        @Composable {
            IconButton(
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp),
                onClick = { onValueChange("") },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_clear),
                        contentDescription = stringResource(id = R.string.stream_compose_search_input_cancel),
                        tint = ChatTheme.colors.textLowEmphasis,
                    )
                },
            )
        }
    } else {
        null
    }

    InputField(
        modifier = modifier
            .onFocusEvent { newState ->
                val wasPreviouslyFocused = isFocused

                if (!wasPreviouslyFocused && newState.isFocused) {
                    onSearchStarted()
                }

                isFocused = newState.isFocused
            },
        value = query,
        onValueChange = onValueChange,
        decorationBox = { innerTextField ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingIcon()

                Box(modifier = Modifier.weight(8f)) {
                    if (query.isEmpty()) {
                        label()
                    }

                    innerTextField()
                }

                trailingIcon?.invoke(this)
            }
        },
        maxLines = 1,
        innerPadding = PaddingValues(4.dp),
    )
}

@Composable
internal fun RowScope.DefaultSearchLeadingIcon() {
    Icon(
        modifier = Modifier.weight(1f),
        painter = painterResource(id = R.drawable.stream_compose_ic_search),
        contentDescription = null,
        tint = ChatTheme.colors.textLowEmphasis,
    )
}

@Composable
internal fun DefaultSearchLabel() {
    Text(
        text = stringResource(id = R.string.stream_compose_search_input_hint),
        style = ChatTheme.typography.body,
        color = ChatTheme.colors.textLowEmphasis,
    )
}

@Preview(name = "Search input")
@Composable
private fun SearchInputPreview() {
    ChatTheme {
        var searchQuery by rememberSaveable { mutableStateOf("") }

        SearchInput(
            modifier = Modifier
                .background(color = ChatTheme.colors.appBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            query = searchQuery,
            onSearchStarted = {},
            onValueChange = {
                searchQuery = it
            },
        )
    }
}