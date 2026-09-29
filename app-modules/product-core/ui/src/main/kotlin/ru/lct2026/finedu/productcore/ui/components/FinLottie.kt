package ru.lct2026.finedu.productcore.ui.components

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieComposition
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Lottie-анимация из `res/raw`. Загрузка только из ресурсов приложения: сетевые запросы запрещены ТЗ.
 * [marker] ограничивает проигрывание одним маркером композиции (например, эмоцией Дзыня).
 * В «Спокойном режиме» ([FinEduTheme.reduceMotion]) вместо анимации — статичный кадр: начало [marker]
 * или последний кадр, если маркер не задан.
 * [contentDescription] = `null` — анимация декоративная, TalkBack её пропускает.
 */
@Composable
fun FinLottie(
    @RawRes animationRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    iterations: Int = 1,
    marker: String? = null,
    dynamicProperties: LottieDynamicProperties? = null
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val semanticsModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }
    if (FinEduTheme.reduceMotion) {
        val staticProgress = marker
            ?.let { name -> composition?.getMarker(name) }
            ?.let { found -> composition?.getProgressForFrame(found.startFrame) }
            ?: 1f
        LottieAnimation(
            composition = composition,
            progress = { staticProgress },
            modifier = semanticsModifier,
            dynamicProperties = dynamicProperties
        )
    } else {
        LottieAnimation(
            composition = composition,
            modifier = semanticsModifier,
            clipSpec = marker?.let { LottieClipSpec.Marker(it) },
            iterations = iterations,
            dynamicProperties = dynamicProperties
        )
    }
}
