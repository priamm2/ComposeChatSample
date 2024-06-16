package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Channel

public sealed class ChannelsStateData {

    public object NoQueryActive : ChannelsStateData() {
        override fun toString(): String = "ChannelsStateData.NoQueryActive"
    }

    public object Loading : ChannelsStateData() {
        override fun toString(): String = "ChannelsStateData.Loading"
    }

    public object OfflineNoResults : ChannelsStateData() {
        override fun toString(): String = "ChannelsStateData.OfflineNoResults"
    }

    public data class Result(val channels: List<Channel>) : ChannelsStateData() {
        override fun toString(): String {
            return "ChannelsStateData.Result(channels.size=${channels.size})"
        }
    }
}