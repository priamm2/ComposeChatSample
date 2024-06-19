package com.example.composechatsample.screen.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composechatsample.R
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.querysort.QuerySortByField
import com.example.composechatsample.screen.PreviewChannelData
import com.example.composechatsample.screen.PreviewUserData
import com.example.composechatsample.screen.components.EmptyContent
import com.example.composechatsample.screen.components.LoadingIndicator
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.viewModel.ChannelListViewModel
import com.example.composechatsample.viewModel.ChannelViewModelFactory

@Composable
public fun ChannelList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ChannelListViewModel = viewModel(
        factory =
        ChannelViewModelFactory(
            ChatClient.instance(),
            QuerySortByField.descByName("last_updated"),
            filters = null,
        ),
    ),
    lazyListState: LazyListState = rememberLazyListState(),
    onLastItemReached: () -> Unit = remember(viewModel) { { viewModel.loadMore() } },
    onChannelClick: (Channel) -> Unit = {},
    onChannelLongClick: (Channel) -> Unit = remember(viewModel) { { viewModel.selectChannel(it) } },
    onSearchResultClick: (Message) -> Unit = {},
    loadingContent: @Composable () -> Unit = { LoadingIndicator(modifier) },
    emptyContent: @Composable () -> Unit = { DefaultChannelListEmptyContent(modifier) },
    emptySearchContent: @Composable (String) -> Unit = { searchQuery ->
        DefaultChannelSearchEmptyContent(
            searchQuery = searchQuery,
            modifier = modifier,
        )
    },
    helperContent: @Composable BoxScope.() -> Unit = {},
    loadingMoreContent: @Composable () -> Unit = { DefaultChannelsLoadingMoreIndicator() },
    channelContent: @Composable (ItemState.ChannelItemState) -> Unit = { itemState ->
        val user by viewModel.user.collectAsState()
        DefaultChannelItem(
            channelItem = itemState,
            currentUser = user,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
        )
    },
    searchResultContent: @Composable (ItemState.SearchResultItemState) -> Unit = { itemState ->
        val user by viewModel.user.collectAsState()
        DefaultSearchResultItem(
            searchResultItemState = itemState,
            currentUser = user,
            onSearchResultClick = onSearchResultClick,
        )
    },
    divider: @Composable () -> Unit = { DefaultChannelItemDivider() },
) {
    val user by viewModel.user.collectAsState()

    ChannelList(
        modifier = modifier,
        contentPadding = contentPadding,
        channelsState = viewModel.channelsState,
        currentUser = user,
        lazyListState = lazyListState,
        onLastItemReached = onLastItemReached,
        onChannelClick = onChannelClick,
        onChannelLongClick = onChannelLongClick,
        loadingContent = loadingContent,
        emptyContent = emptyContent,
        emptySearchContent = emptySearchContent,
        helperContent = helperContent,
        loadingMoreContent = loadingMoreContent,
        channelContent = channelContent,
        searchResultContent = searchResultContent,
        divider = divider,
    )
}

@Composable
public fun ChannelList(
    channelsState: ChannelsState,
    currentUser: User?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    lazyListState: LazyListState = rememberLazyListState(),
    onLastItemReached: () -> Unit = {},
    onChannelClick: (Channel) -> Unit = {},
    onChannelLongClick: (Channel) -> Unit = {},
    onSearchResultClick: (Message) -> Unit = {},
    loadingContent: @Composable () -> Unit = { DefaultChannelListLoadingIndicator(modifier) },
    emptyContent: @Composable () -> Unit = { DefaultChannelListEmptyContent(modifier) },
    emptySearchContent: @Composable (String) -> Unit = { searchQuery ->
        DefaultChannelSearchEmptyContent(
            searchQuery = searchQuery,
            modifier = modifier,
        )
    },
    helperContent: @Composable BoxScope.() -> Unit = {},
    loadingMoreContent: @Composable () -> Unit = { DefaultChannelsLoadingMoreIndicator() },
    channelContent: @Composable (ItemState.ChannelItemState) -> Unit = { itemState ->
        DefaultChannelItem(
            channelItem = itemState,
            currentUser = currentUser,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
        )
    },
    searchResultContent: @Composable (ItemState.SearchResultItemState) -> Unit = { itemState ->
        DefaultSearchResultItem(
            searchResultItemState = itemState,
            currentUser = currentUser,
            onSearchResultClick = onSearchResultClick,
        )
    },
    divider: @Composable () -> Unit = { DefaultChannelItemDivider() },
) {
    val (isLoading, _, _, channels, searchQuery) = channelsState

    when {
        isLoading -> loadingContent()
        !isLoading && channels.isNotEmpty() -> Channels(
            modifier = modifier,
            contentPadding = contentPadding,
            channelsState = channelsState,
            lazyListState = lazyListState,
            onLastItemReached = onLastItemReached,
            helperContent = helperContent,
            loadingMoreContent = loadingMoreContent,
            itemContent = { itemState ->
                WrapperItemContent(
                    itemState = itemState,
                    channelContent = channelContent,
                    searchResultContent = searchResultContent,
                )
            },
            divider = divider,
        )
        searchQuery.query.isNotBlank() -> emptySearchContent(searchQuery.query)
        else -> emptyContent()
    }
}

@Composable
internal fun WrapperItemContent(
    itemState: ItemState,
    channelContent: @Composable (ItemState.ChannelItemState) -> Unit,
    searchResultContent: @Composable (ItemState.SearchResultItemState) -> Unit,
) {
    when (itemState) {
        is ItemState.ChannelItemState -> channelContent(itemState)
        is ItemState.SearchResultItemState -> searchResultContent(itemState)
    }
}

@Composable
internal fun DefaultSearchResultItem(
    searchResultItemState: ItemState.SearchResultItemState,
    currentUser: User?,
    onSearchResultClick: (Message) -> Unit,
) {
    SearchResultItem(
        searchResultItemState = searchResultItemState,
        currentUser = currentUser,
        onSearchResultClick = onSearchResultClick,
    )
}

@Composable
internal fun DefaultChannelItem(
    channelItem: ItemState.ChannelItemState,
    currentUser: User?,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
) {
    ChannelItem(
        channelItem = channelItem,
        currentUser = currentUser,
        onChannelClick = onChannelClick,
        onChannelLongClick = onChannelLongClick,
    )
}

@Composable
internal fun DefaultChannelListLoadingIndicator(modifier: Modifier) {
    LoadingIndicator(modifier)
}

@Composable
internal fun DefaultChannelListEmptyContent(modifier: Modifier = Modifier) {
    EmptyContent(
        modifier = modifier,
        painter = painterResource(id = R.drawable.stream_compose_empty_channels),
        text = stringResource(R.string.stream_compose_channel_list_empty_channels),
    )
}

@Composable
internal fun DefaultChannelSearchEmptyContent(
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    EmptyContent(
        modifier = modifier,
        painter = painterResource(id = R.drawable.stream_compose_empty_search_results),
        text = stringResource(R.string.stream_compose_channel_list_empty_search_results, searchQuery),
    )
}

@Composable
public fun DefaultChannelItemDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(color = ChatTheme.colors.borders),
    )
}

@Preview(showBackground = true, name = "ChannelList Preview (Content state)")
@Composable
private fun ChannelListForContentStatePreview() {
    ChannelListPreview(
        ChannelsState(
            isLoading = false,
            channelItems = listOf(
                ItemState.ChannelItemState(channel = PreviewChannelData.channelWithImage),
                ItemState.ChannelItemState(channel = PreviewChannelData.channelWithMessages),
                ItemState.ChannelItemState(channel = PreviewChannelData.channelWithFewMembers),
                ItemState.ChannelItemState(channel = PreviewChannelData.channelWithManyMembers),
                ItemState.ChannelItemState(channel = PreviewChannelData.channelWithOnlineUser),
            ),
        ),
    )
}

@Preview(showBackground = true, name = "ChannelList Preview (Empty state)")
@Composable
private fun ChannelListForEmptyStatePreview() {
    ChannelListPreview(
        ChannelsState(
            isLoading = false,
            channelItems = emptyList(),
        ),
    )
}

@Preview(showBackground = true, name = "ChannelList Preview (Loading state)")
@Composable
private fun ChannelListForLoadingStatePreview() {
    ChannelListPreview(
        ChannelsState(
            isLoading = true,
        ),
    )
}

@Composable
private fun ChannelListPreview(channelsState: ChannelsState) {
    ChatTheme {
        ChannelList(
            modifier = Modifier.fillMaxSize(),
            channelsState = channelsState,
            currentUser = PreviewUserData.user1,
        )
    }
}