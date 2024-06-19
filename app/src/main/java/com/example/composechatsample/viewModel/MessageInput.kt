package com.example.composechatsample.viewModel

public data class MessageInput(
    val text: String = "",
    val source: Source = Source.Default,
) {

    public sealed class Source {
        public data object Default : Source()
        public data object External : Source()
        public sealed class Internal : Source()
        public data object Edit : Internal()
        public data object CommandSelected : Internal()
        public data object MentionSelected : Internal()
    }
}