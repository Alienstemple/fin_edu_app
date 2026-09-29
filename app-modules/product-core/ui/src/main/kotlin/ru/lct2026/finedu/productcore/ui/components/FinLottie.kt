package ru.lct2026.finedu.productcore.ui.components

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Lottie-анимация из `res/raw`. Загрузка только из ресурсов приложения: сетевые запросы запрещены ТЗ.
 * В «Спокойном режиме» ([FinEduTheme.reduceMotion]) вместо анимации — статичный последний кадр.
 * [contentDescription] = `null` — анимация декоративная, TalkBack её пропускает.
 */
@Composable
fun FinLottie(
    @RawRes animationRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    iterations: Int = 1
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val semanticsModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }
    if (FinEduTheme.reduceMotion) {
        LottieAnimation(composition = composition, progress = { 1f }, modifier = semanticsModifier)
    } else {
        LottieAnimation(composition = composition, iterations = iterations, modifier = semanticsModifier)
    }
}
