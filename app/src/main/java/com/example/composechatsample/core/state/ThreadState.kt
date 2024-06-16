package com.example.composechatsample.core.state;

import com.example.composechatsample.core.models.Message
import kotlinx.coroutines.flow.StateFlow

public interface ThreadState {
    public val parentId: String
    public val messages: StateFlow<List<Message>>
    public val loading: StateFlow<Boolean>
    public val endOfOlderMessages: StateFlow<Boolean>
    public val endOfNewerMessages: StateFlow<Boolean>
    public val oldestInThread: StateFlow<Message?>

    public val newestInThread: StateFlow<Message?>
}