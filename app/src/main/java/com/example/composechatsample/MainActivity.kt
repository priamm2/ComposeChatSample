package com.example.composechatsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val userCredentials = ChatApp.credentialsRepository.loadUserCredentials()
            if (userCredentials != null) {
                ChatHelper.connectUser(userCredentials)

                if (intent.hasExtra(KEY_CHANNEL_ID)) {
                    val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))
                    val messageId = intent.getStringExtra(KEY_MESSAGE_ID)
                    val parentMessageId = intent.getStringExtra(KEY_PARENT_MESSAGE_ID)

                    TaskStackBuilder.create(this@MainActivity)
                        .addNextIntent(ChannelsActivity.createIntent(this@MainActivity))
                        .addNextIntent(
                            MessagesActivity.createIntent(
                                context = this@MainActivity,
                                channelId = channelId,
                                messageId = messageId,
                                parentMessageId = parentMessageId,
                            ),
                        )
                        .startActivities()
                } else {
                    startActivity(ChannelsActivity.createIntent(this@MainActivity))
                }
            } else {
                startActivity(UserLoginActivity.createIntent(this@MainActivity))
            }
            finish()
        }
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_MESSAGE_ID = "messageId"
        private const val KEY_PARENT_MESSAGE_ID = "parentMessageId"

        fun createIntent(
            context: Context,
            channelId: String,
            messageId: String?,
            parentMessageId: String?,
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
                putExtra(KEY_MESSAGE_ID, messageId)
                putExtra(KEY_PARENT_MESSAGE_ID, parentMessageId)
            }
        }
    }
}
