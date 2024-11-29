package com.yunext.kotlin.kmp.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.clickablePure(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    return composed {
        this.clickable(
            remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
            enabled = enabled
        )
    }
}