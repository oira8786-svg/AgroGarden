package com.agrogarden

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

@Composable
fun <T> ColumnScope.items(list: List<T>, content: @Composable (T) -> Unit) {
    list.forEach { content(it) }
}
