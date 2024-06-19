package com.example.composechatsample.screen.messages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.composechatsample.core.models.User
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MentionSuggestionList(
    users: List<User>,
    modifier: Modifier = Modifier,
    onMentionSelected: (User) -> Unit = {},
    itemContent: @Composable (User) -> Unit = { user ->
        DefaultMentionSuggestionItem(
            user = user,
            onMentionSelected = onMentionSelected,
        )
    },
) {
    SuggestionList(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = ChatTheme.dimens.suggestionListMaxHeight),
    ) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(
                items = users,
                key = User::id,
            ) { user ->
                itemContent(user)
            }
        }
    }
}

@Composable
internal fun DefaultMentionSuggestionItem(
    user: User,
    onMentionSelected: (User) -> Unit,
) {
    MentionSuggestionItem(
        user = user,
        onMentionSelected = onMentionSelected,
    )
}