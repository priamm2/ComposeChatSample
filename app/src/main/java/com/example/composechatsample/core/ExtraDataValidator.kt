package com.example.composechatsample.core

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.CustomObject
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import kotlinx.coroutines.CoroutineScope

internal class ExtraDataValidator(
    private val scope: CoroutineScope,
    private val delegate: ChatApi,
) : ChatApi by delegate {

    override fun updateChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>,
        updateMessage: Message?,
    ): Call<Channel> {
        return delegate.updateChannel(channelType, channelId, extraData, updateMessage)
            .withExtraDataValidation(extraData)
            .withExtraDataValidation(updateMessage)
    }

    override fun updateChannelPartial(
        channelType: String,
        channelId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Channel> {
        return delegate.updateChannelPartial(channelType, channelId, set, unset)
            .withExtraDataValidation(set)
    }

    override fun updateMessage(message: Message): Call<Message> {
        return delegate.updateMessage(
            message = message,
        ).withExtraDataValidation(message.extraData)
    }

    override fun partialUpdateMessage(
        messageId: String,
        set: Map<String, Any>,
        unset: List<String>,
        skipEnrichUrl: Boolean,
    ): Call<Message> {
        return delegate.partialUpdateMessage(
            messageId = messageId,
            set = set,
            unset = unset,
            skipEnrichUrl = skipEnrichUrl,
        ).withExtraDataValidation(set)
    }

    override fun updateUsers(users: List<User>): Call<List<User>> {
        return delegate.updateUsers(users)
            .withExtraDataValidation(users)
    }

    override fun partialUpdateUser(id: String, set: Map<String, Any>, unset: List<String>): Call<User> {
        return delegate.partialUpdateUser(id, set, unset)
            .withExtraDataValidation(set)
    }

    private fun <T : CustomObject> Call<List<T>>.withExtraDataValidation(
        objects: List<T>,
    ): Call<List<T>> {
        val (obj, reserved) = objects.findReserved()
        return when (obj == null || reserved == null) {
            true -> this
            else -> ErrorCall(
                scope,
                Error.GenericError(
                    message = obj.composeErrorMessage(reserved),
                ),
            )
        }
    }

    private fun <T : CustomObject, K : CustomObject> Call<T>.withExtraDataValidation(obj: K?): Call<T> {
        val reserved = obj?.findReserved() ?: emptyList()
        return when (reserved.isEmpty()) {
            true -> this
            else -> ErrorCall(
                scope,
                Error.GenericError(
                    message = obj.composeErrorMessage(reserved),
                ),
            )
        }
    }

    private inline fun <reified T : CustomObject> Call<T>.withExtraDataValidation(
        extraData: Map<String, Any>,
    ): Call<T> {
        val reserved = extraData.findReserved<T>()
        return when (reserved.isEmpty()) {
            true -> this
            else -> ErrorCall(
                scope,
                Error.GenericError(
                    message = "'extraData' contains reserved keys: ${reserved.joinToString()}",
                ),
            )
        }
    }

    private fun <T : CustomObject> List<T>.findReserved(): Pair<T?, List<String>?> {
        for (obj in this) {
            val reserved = obj.findReserved()
            if (reserved.isNotEmpty()) {
                return Pair(obj, reserved)
            }
        }
        return Pair(null, null)
    }

    private fun CustomObject.findReserved(): List<String> {
        return when (this) {
            is Channel -> extraData.keys.filter(reservedInChannelPredicate)
            is Message -> extraData.keys.filter(reservedInChannelPredicate)
            is User -> extraData.keys.filter(reservedInChannelPredicate)
            else -> emptyList()
        }
    }

    private inline fun <reified T : CustomObject> Map<String, Any>.findReserved(): List<String> {
        return when (T::class) {
            Channel::class -> keys.filter(reservedInChannelPredicate)
            Message::class -> keys.filter(reservedInMessagePredicate)
            User::class -> keys.filter(reservedInUserPredicate)
            else -> emptyList()
        }
    }

    private fun <T : CustomObject> T?.composeErrorMessage(reserved: List<String>): String {
        return "'${resolveName()}(id=${resolveId()}" + ").extraData' contains reserved keys: ${reserved.joinToString()}"
    }

    private fun <T : CustomObject> T?.resolveName(): String {
        return when (this) {
            is Channel -> "channel"
            is Message -> "message"
            is User -> "user"
            else -> ""
        }
    }

    private fun <T : CustomObject> T?.resolveId(): String {
        return when (this) {
            is Channel -> id
            is Message -> id
            is User -> id
            else -> ""
        }
    }

    private companion object {

        private val reservedInChannel = arrayOf(
            "cid",
            "id",
            "type",
            "created_at",
            "deleted_at",
            "updated_at",
            "member_count",
            "created_by",
            "last_message_at",
            "own_capabilities",
            "config",
        )

        private val reservedInMessage = arrayOf(
            "id",
            "cid",
            "created_at",
            "updated_at",
            "deleted_at",
        )

        private val reservedInUser = arrayOf(
            "id",
            "cid",
            "created_at",
            "updated_at",
        )

        private val reservedInChannelPredicate: (String) -> Boolean = reservedInChannel::contains
        private val reservedInMessagePredicate: (String) -> Boolean = reservedInMessage::contains
        private val reservedInUserPredicate: (String) -> Boolean = reservedInUser::contains
    }
}