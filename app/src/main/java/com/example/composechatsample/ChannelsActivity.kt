package com.example.composechatsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.querysort.QuerySortByField
import com.example.composechatsample.screen.channels.ChannelsScreen
import com.example.composechatsample.screen.channels.SearchMode
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.viewModel.ChannelListViewModel
import com.example.composechatsample.viewModel.ChannelViewModelFactory
import kotlinx.coroutines.launch

class ChannelsActivity : BaseConnectedActivity() {

    private val factory by lazy {
        ChannelViewModelFactory(
            ChatClient.instance(),
            QuerySortByField.descByName("last_updated"),
            null,
        )
    }

    private val listViewModel: ChannelListViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme(
                dateFormatter = ChatApp.dateFormatter,
                autoTranslationEnabled = ChatApp.autoTranslationEnabled,
                allowUIAutomationTest = true,
            ) {
                ChannelsScreen(
                    viewModelFactory = factory,
                    title = stringResource(id = R.string.app_name),
                    isShowingHeader = true,
                    searchMode = SearchMode.Messages,
                    onChannelClick = ::openMessages,
                    onSearchMessageItemClick = ::openMessages,
                    onBackPressed = ::finish,
                    onHeaderAvatarClick = {
                        listViewModel.viewModelScope.launch {
                            ChatHelper.disconnectUser()
                            openUserLogin()
                        }
                    },
                )
            }
        }
    }
    private fun openMessages(channel: Channel) {
        startActivity(
            MessagesActivity.createIntent(
                context = this,
                channelId = channel.cid,
                messageId = null,
                parentMessageId = null,
            ),
        )
    }


    private fun openMessages(message: Message) {
        startActivity(
            MessagesActivity.createIntent(
                context = this,
                channelId = message.cid,
                messageId = message.id,
                parentMessageId = message.parentId,
            ),
        )
    }


    private fun openUserLogin() {
        finish()
        startActivity(UserLoginActivity.createIntent(this))
        overridePendingTransition(0, 0)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, ChannelsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }
}