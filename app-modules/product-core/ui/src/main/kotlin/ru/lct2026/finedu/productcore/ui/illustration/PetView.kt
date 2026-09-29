package ru.lct2026.finedu.productcore.ui.illustration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.components.FinLottie
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.PetCoralFur
import ru.lct2026.finedu.productcore.ui.theme.PetLilacFur
import ru.lct2026.finedu.productcore.ui.theme.PetMintFur

/**
 * Питомец Дзынь — живая Lottie-анимация `anim_dzyn` (бабочка, мешочек, убор с монеткой-звездой): дышит, моргает,
 * покачивает мешочком. Эмоция — маркер композиции, шёрстка перекрашивается dynamic property `Fur`, убор —
 * прозрачностью слоя: [PetHat.NONE] — котелок, [PetHat.CAP] — кепка, [PetHat.HEADPHONES] — наушники
 * (3 шёрстки × 3 убора = 9 видов, ТЗ 2.6). Стадия меняет размер: Малыш меньше, Мастер мешочка — в полный рост.
 * Исходник анимации — `scripts/lottie/generate_dzyn.py`.
 */
@Composable
fun PetView(
    look: PetLook,
    modifier: Modifier = Modifier,
    mood: PetMood = PetMood.NEUTRAL,
    stage: PetStage = PetStage.MASTER,
    contentDescription: String? = null
) {
    val hat = look.hat
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.COLOR, look.fur.color.toArgb(), "**", FUR_KEY),
        // Искорка — часть котелка.
        rememberLottieDynamicProperty(LottieProperty.TRANSFORM_OPACITY, hat.opacityOf(PetHat.NONE), BOWLER_LAYER),
        rememberLottieDynamicProperty(LottieProperty.TRANSFORM_OPACITY, hat.opacityOf(PetHat.NONE), SPARKLE_LAYER),
        rememberLottieDynamicProperty(LottieProperty.TRANSFORM_OPACITY, hat.opacityOf(PetHat.CAP), CAP_LAYER),
        rememberLottieDynamicProperty(
            LottieProperty.TRANSFORM_OPACITY,
            hat.opacityOf(PetHat.HEADPHONES),
            HEADPHONES_LAYER
        )
    )
    FinLottie(
        animationRes = R.raw.anim_dzyn,
        contentDescription = contentDescription,
        modifier = modifier
            .aspectRatio(VIEW_WIDTH / VIEW_HEIGHT)
            .graphicsLayer {
                scaleX = stage.scale
                scaleY = stage.scale
                transformOrigin = TransformOrigin(0.5f, GROUND_Y / VIEW_HEIGHT)
            },
        iterations = LottieConstants.IterateForever,
        marker = mood.marker,
        dynamicProperties = dynamicProperties
    )
}

private const val VIEW_WIDTH = 200f
private const val VIEW_HEIGHT = 190f
private const val GROUND_Y = 160f
private const val FUR_KEY = "Fur"
private const val BOWLER_LAYER = "Bowler"
private const val SPARKLE_LAYER = "Sparkle"
private const val CAP_LAYER = "Cap"
private const val HEADPHONES_LAYER = "Headphones"
private const val VISIBLE = 100
private const val HIDDEN = 0

/** Прозрачность слоя убора [layerHat]: виден только выбранный. */
private fun PetHat.opacityOf(layerHat: PetHat): Int = if (this == layerHat) VISIBLE else HIDDEN

private val PetStage.scale: Float
    get() = when (this) {
        PetStage.BABY -> 0.8f
        PetStage.SPRY -> 0.9f
        PetStage.MASTER -> 1f
    }

private val PetFur.color
    get() = when (this) {
        PetFur.LILAC -> PetLilacFur
        PetFur.MINT -> PetMintFur
        PetFur.CORAL -> PetCoralFur
    }

/** Имена маркеров в `anim_dzyn.json`. */
private val PetMood.marker: String
    get() = when (this) {
        PetMood.NEUTRAL -> "neutral"
        PetMood.HAPPY -> "happy"
        PetMood.JOY -> "joy"
        PetMood.PROUD -> "proud"
        PetMood.SHOCK -> "shock"
        PetMood.THINKING -> "thinking"
        PetMood.COLD -> "cold"
    }

@Preview(widthDp = 360)
@Composable
private fun PetViewPreview() {
    FinEduPreview {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PetView(PetLook(PetFur.LILAC, PetHat.NONE), Modifier.width(100.dp), PetMood.NEUTRAL, PetStage.BABY)
                PetView(PetLook(PetFur.MINT, PetHat.CAP), Modifier.width(100.dp), PetMood.HAPPY, PetStage.SPRY)
                PetView(PetLook(PetFur.CORAL, PetHat.HEADPHONES), Modifier.width(100.dp), PetMood.JOY)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PetView(PetLook(PetFur.LILAC, PetHat.CAP), Modifier.width(80.dp), PetMood.SHOCK)
                PetView(PetLook(PetFur.MINT, PetHat.NONE), Modifier.width(80.dp), PetMood.THINKING)
                PetView(PetLook(PetFur.CORAL, PetHat.NONE), Modifier.width(80.dp), PetMood.COLD)
                PetView(PetLook(PetFur.LILAC, PetHat.HEADPHONES), Modifier.width(80.dp), PetMood.PROUD)
            }
        }
    }
}
