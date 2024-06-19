package com.example.composechatsample.screen.messages

import com.example.composechatsample.R

public sealed class ModeratedMessageOption {
    public abstract val text: Int
}

public object SendAnyway : ModeratedMessageOption() {
    public override val text: Int = R.string.stream_ui_moderation_dialog_send
    override fun toString(): String = "SendAnyway"
}

public object EditMessage : ModeratedMessageOption() {
    public override val text: Int = R.string.stream_ui_moderation_dialog_edit
    override fun toString(): String = "EditMessage"
}

public object DeleteMessage : ModeratedMessageOption() {
    public override val text: Int = R.string.stream_ui_moderation_dialog_delete
    override fun toString(): String = "DeleteMessage"
}

public data class CustomModerationOption(
    override val text: Int,
    public val extraData: Map<String, Any> = emptyMap(),
) : ModeratedMessageOption()


public fun defaultMessageModerationOptions(): List<ModeratedMessageOption> = listOf(
    SendAnyway,
    EditMessage,
    DeleteMessage,
)