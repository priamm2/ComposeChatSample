package com.example.composechatsample.screen.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.composechatsample.common.DateFormatType
import com.example.composechatsample.helper.DateFormatter
import com.example.composechatsample.ui.theme.ChatTheme
import java.util.Date

@Composable
public fun Timestamp(
    date: Date?,
    modifier: Modifier = Modifier,
    formatter: DateFormatter = ChatTheme.dateFormatter,
    formatType: DateFormatType = DateFormatType.DATE,
) {
    val timestamp = if (LocalInspectionMode.current) {
        "13:49"
    } else {
        when (formatType) {
            DateFormatType.TIME -> formatter.formatTime(date)
            DateFormatType.DATE -> formatter.formatDate(date)
            DateFormatType.RELATIVE -> formatter.formatRelativeTime(date)
        }
    }

    Text(
        modifier = modifier,
        text = timestamp,
        style = ChatTheme.typography.footnote,
        color = ChatTheme.colors.textLowEmphasis,
    )
}