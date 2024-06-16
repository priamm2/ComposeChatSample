package com.example.composechatsample.core.state

import com.example.composechatsample.core.NEVER
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus

class ThreadStateLogic(private val mutableState: ThreadMutableState) {

    fun writeThreadState(): ThreadMutableState = mutableState

    fun deleteMessage(message: Message) {
        mutableState.deleteMessage(message)
    }

    fun upsertMessage(message: Message) = upsertMessages(listOf(message))

    fun upsertMessages(messages: List<Message>) {
        val oldMessages = mutableState.rawMessage.value
        mutableState.upsertMessages(
            messages.filter { newMessage -> isMessageNewerThanCurrent(oldMessages[newMessage.id], newMessage) },
        )
    }

    private fun isMessageNewerThanCurrent(currentMessage: Message?, newMessage: Message): Boolean {
        return if (newMessage.syncStatus == SyncStatus.COMPLETED) {
            (currentMessage?.lastUpdateTime() ?: NEVER.time) <= newMessage.lastUpdateTime()
        } else {
            (currentMessage?.lastLocalUpdateTime() ?: NEVER.time) <= newMessage.lastLocalUpdateTime()
        }
    }

    private fun Message.lastUpdateTime(): Long = listOfNotNull(
        createdAt,
        updatedAt,
        deletedAt,
    ).maxOfOrNull { it.time }
        ?: NEVER.time

    private fun Message.lastLocalUpdateTime(): Long = listOfNotNull(
        createdLocallyAt,
        updatedLocallyAt,
        deletedAt,
    ).maxOfOrNull { it.time }
        ?: NEVER.time
}