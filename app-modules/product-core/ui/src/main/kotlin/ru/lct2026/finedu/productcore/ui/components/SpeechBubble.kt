package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/** Реплика Дзыня. Хвостик — срезанный нижний левый угол, «смотрит» на питомца слева снизу. */
@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .glass(shape = BubbleShape, style = GlassStyle.Strong)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}

private val BubbleShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)

@Preview
@Composable
private fun SpeechBubblePreview() {
    FinEduPreview {
        Column(modifier = Modifier.padding(20.dp)) {
            SpeechBubble(text = "Мешочек стал тяжелее. Приятно!")
        }
    }
}
