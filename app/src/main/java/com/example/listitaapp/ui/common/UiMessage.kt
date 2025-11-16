package com.example.listitaapp.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

data class UiMessage(
    @StringRes val resId: Int? = null,
    val text: String? = null,
    val formatArgs: List<Any> = emptyList()
)

@Composable
fun UiMessage.asString(): String {
    return resId?.let { stringResource(it, *formatArgs.toTypedArray()) } ?: text.orEmpty()
}
