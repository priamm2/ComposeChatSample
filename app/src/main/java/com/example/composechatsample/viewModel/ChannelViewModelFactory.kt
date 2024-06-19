package com.example.composechatsample.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.querysort.QuerySortByField
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.plugin.ChatEventHandlerFactory


class ChannelViewModelFactory(
    private val chatClient: ChatClient = ChatClient.instance(),
    private val querySort: QuerySorter<Channel> = QuerySortByField.descByName("last_updated"),
    private val filters: FilterObject? = null,
    private val channelLimit: Int = ChannelListViewModel.DEFAULT_CHANNEL_LIMIT,
    private val memberLimit: Int = ChannelListViewModel.DEFAULT_MEMBER_LIMIT,
    private val messageLimit: Int = ChannelListViewModel.DEFAULT_MESSAGE_LIMIT,
    private val chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(chatClient.clientState),
) : ViewModelProvider.Factory {

    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        ChannelListViewModel::class.java to {
            ChannelListViewModel(
                chatClient = chatClient,
                initialSort = querySort,
                initialFilters = filters,
                channelLimit = channelLimit,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
                chatEventHandlerFactory = chatEventHandlerFactory,
            )
        },
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException(
                "MessageListViewModelFactory can only create instances of the " +
                    "following classes: ${factories.keys.joinToString { it.simpleName }}",
            )

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}