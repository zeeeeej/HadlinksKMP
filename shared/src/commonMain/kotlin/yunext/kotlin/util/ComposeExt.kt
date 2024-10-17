package yunext.kotlin.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import color
import randomZhongGuoSe

fun Modifier.randomBG(padding:Dp = 12.dp): Modifier {
    return this.composed {

        this
            .padding(padding)
            .clip(RoundedCornerShape(12.dp))
            .background(randomZhongGuoSe().color.copy(alpha = .5f))
            .padding(12.dp)
    }
}