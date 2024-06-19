package com.example.composechatsample.ui.theme

import android.content.Context
import androidx.annotation.StringRes
import com.example.composechatsample.R
import com.example.composechatsample.core.getDisplayName
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.User

fun interface ChannelNameFormatter {

    fun formatChannelName(channel: Channel, currentUser: User?): String

    companion object {

        @JvmOverloads
        fun defaultFormatter(
            context: Context,
            @StringRes fallback: Int = R.string.stream_ui_channel_list_untitled_channel,
            maxMembers: Int = 2,
        ): ChannelNameFormatter {
            return DefaultChannelNameFormatter(context, fallback, maxMembers)
        }
    }
}

private class DefaultChannelNameFormatter(
    private val context: Context,
    @StringRes private val fallback: Int,
    private val maxMembers: Int,
) : ChannelNameFormatter {

    override fun formatChannelName(channel: Channel, currentUser: User?): String {
        return channel.getDisplayName(context, currentUser, fallback, maxMembers)
    }
}