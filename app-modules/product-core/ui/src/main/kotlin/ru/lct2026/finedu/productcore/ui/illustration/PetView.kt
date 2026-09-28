package ru.lct2026.finedu.productcore.ui.illustration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.PetBelly
import ru.lct2026.finedu.productcore.ui.theme.PetBellyLight
import ru.lct2026.finedu.productcore.ui.theme.PetBellyMark
import ru.lct2026.finedu.productcore.ui.theme.PetBlush
import ru.lct2026.finedu.productcore.ui.theme.PetCap
import ru.lct2026.finedu.productcore.ui.theme.PetCapBrim
import ru.lct2026.finedu.productcore.ui.theme.PetCoral
import ru.lct2026.finedu.productcore.ui.theme.PetCoralEar
import ru.lct2026.finedu.productcore.ui.theme.PetHeadphones
import ru.lct2026.finedu.productcore.ui.theme.PetInk
import ru.lct2026.finedu.productcore.ui.theme.PetLilac
import ru.lct2026.finedu.productcore.ui.theme.PetLilacEar
import ru.lct2026.finedu.productcore.ui.theme.PetMint
import ru.lct2026.finedu.productcore.ui.theme.PetMintEar
import ru.lct2026.finedu.productcore.ui.theme.PetShadow
import ru.lct2026.finedu.productcore.ui.theme.PetStageGlow
import ru.lct2026.finedu.productcore.ui.theme.RoomCream
import ru.lct2026.finedu.productcore.ui.theme.RoomPlaidStripe
import ru.lct2026.finedu.productcore.ui.theme.RoomWarm

/**
 * Питомец Дзынь на Canvas по SVG архивного макета (viewBox 200×190): тело-шар, живот-монета, уши, хвост, глаза,
 * рот-эмоция, убор. Стадия меняет размер: Малыш меньше, Мастер мешочка — в полный рост.
 */
@Composable
fun PetView(
    look: PetLook,
    modifier: Modifier = Modifier,
    mood: PetMood = PetMood.NEUTRAL,
    stage: PetStage = PetStage.MASTER,
    contentDescription: String? = null
) {
    val paths = remember { PetPaths() }
    Canvas(
        modifier = modifier
            .aspectRatio(VIEW_WIDTH / VIEW_HEIGHT)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            )
    ) {
        withTransform({
            scale(size.width / VIEW_WIDTH, size.height / VIEW_HEIGHT, pivot = Offset.Zero)
        }) {
            drawGround()
            withTransform({ scale(stage.scale, stage.scale, pivot = Offset(CENTER_X, GROUND_Y)) }) {
                drawBody(paths, look.fur)
                drawFace(paths, mood)
                if (mood == PetMood.COLD) drawPlaid()
                drawHat(paths, look.hat)
            }
        }
    }
}

private const val VIEW_WIDTH = 200f
private const val VIEW_HEIGHT = 190f
private const val CENTER_X = 100f
private const val GROUND_Y = 158f

private val PetStage.scale: Float
    get() = when (this) {
        PetStage.BABY -> 0.8f
        PetStage.SPRY -> 0.9f
        PetStage.MASTER -> 1f
    }

/** Пути из SVG макета. Парсятся один раз на экземпляр `PetView`. */
private class PetPaths {
    val leftEar = path("M62 52 C58 28 66 18 78 26 C86 31 90 42 91 52 Z")
    val rightEar = path("M138 52 C142 28 134 18 122 26 C114 31 110 42 109 52 Z")
    val tail = path("M152 118 C176 112 182 92 170 80 C182 100 168 116 150 106 Z")
    val rim = path("M52 84 C58 58 78 45 100 45")
    val coinMark = path("M94 126 L106 110")
    val smile = path("M92 101 Q100 108 108 101")
    val bigSmile = path("M89 100 Q100 113 111 100")
    val softMouth = path("M94 103 Q100 106 106 103")
    val flatMouth = path("M94 104 L106 104")
    val wavyMouth = path("M92 105 Q96 102 100 105 Q104 108 108 105")
    val closedLeftEye = path("M70 90 Q80 78 90 90")
    val closedRightEye = path("M110 90 Q120 78 130 90")
    val sleepyLeftEye = path("M70 88 Q80 95 90 88")
    val sleepyRightEye = path("M110 88 Q120 95 130 88")
    val capTop = path("M60 46 C64 22 136 22 140 46 Z")
    val capBrim = path("M56 46 L152 46 C156 46 156 53 152 53 L56 53 C52 53 52 46 56 46 Z")
    val headband = path("M56 74 C56 38 144 38 144 74")

    private fun path(data: String): Path = PathParser().parsePathString(data).toPath()
}

private fun DrawScope.drawGround() {
    drawOval(
        brush = Brush.radialGradient(
            listOf(PetStageGlow.copy(alpha = STAGE_GLOW_ALPHA), PetStageGlow.copy(alpha = 0f)),
            center = Offset(CENTER_X, GROUND_Y),
            radius = 84f
        ),
        topLeft = Offset(16f, 134f),
        size = Size(168f, 48f)
    )
    drawOval(PetShadow.copy(alpha = SHADOW_ALPHA), topLeft = Offset(54f, 152f), size = Size(92f, 20f))
}

private fun DrawScope.drawBody(paths: PetPaths, fur: PetFur) {
    val (body, ear) = when (fur) {
        PetFur.LILAC -> PetLilac to PetLilacEar
        PetFur.MINT -> PetMint to PetMintEar
        PetFur.CORAL -> PetCoral to PetCoralEar
    }
    drawPath(paths.leftEar, ear)
    drawPath(paths.rightEar, ear)
    drawPath(paths.tail, ear)
    // Тело: радиальный градиент с бликом сверху слева (cx 36%, cy 28%).
    drawOval(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to body[0], BODY_MID_STOP to body[1], 1f to body[2]),
            center = Offset(40f + 120f * BODY_HIGHLIGHT_X, 46f + 112f * BODY_HIGHLIGHT_Y),
            radius = 120f * BODY_RADIUS
        ),
        topLeft = Offset(40f, 46f),
        size = Size(120f, 112f)
    )
    drawPath(paths.rim, Color.White.copy(alpha = RIM_ALPHA), style = Stroke(width = 5f, cap = StrokeCap.Round))
    // Живот-монета.
    drawOval(
        brush = Brush.radialGradient(listOf(PetBellyLight, PetBelly), center = Offset(100f, 110f), radius = 48f),
        topLeft = Offset(67f, 88f),
        size = Size(66f, 60f)
    )
    val mark = PetBellyMark.copy(alpha = BELLY_MARK_ALPHA)
    drawCircle(mark, radius = 17f, center = Offset(100f, 118f), style = Stroke(width = 3f))
    drawPath(paths.coinMark, mark, style = Stroke(width = 3f, cap = StrokeCap.Round))
}

private fun DrawScope.drawFace(paths: PetPaths, mood: PetMood) {
    drawEyes(paths, mood)
    if (mood == PetMood.HAPPY || mood == PetMood.JOY || mood == PetMood.PROUD) {
        drawOval(PetBlush.copy(alpha = BLUSH_ALPHA), topLeft = Offset(62f, 96f), size = Size(14f, 8f))
        drawOval(PetBlush.copy(alpha = BLUSH_ALPHA), topLeft = Offset(124f, 96f), size = Size(14f, 8f))
    }
    drawMouth(paths, mood)
}

private fun DrawScope.drawEyes(paths: PetPaths, mood: PetMood) {
    when (mood) {
        PetMood.JOY, PetMood.PROUD -> {
            drawPath(paths.closedLeftEye, PetInk, style = Stroke(width = 4f, cap = StrokeCap.Round))
            drawPath(paths.closedRightEye, PetInk, style = Stroke(width = 4f, cap = StrokeCap.Round))
        }

        PetMood.COLD -> {
            drawPath(paths.sleepyLeftEye, PetInk, style = Stroke(width = 4f, cap = StrokeCap.Round))
            drawPath(paths.sleepyRightEye, PetInk, style = Stroke(width = 4f, cap = StrokeCap.Round))
        }

        PetMood.SHOCK -> drawEyes(radiusX = 14f, radiusY = 16f, glintOffset = Offset(4f, -5f))

        PetMood.THINKING -> drawEyes(radiusX = 12f, radiusY = 13f, glintOffset = Offset(1f, -8f))

        PetMood.NEUTRAL, PetMood.HAPPY -> drawEyes(radiusX = 12f, radiusY = 13f, glintOffset = Offset(4f, -5f))
    }
}

private fun DrawScope.drawMouth(paths: PetPaths, mood: PetMood) {
    val line = Stroke(width = 3.5f, cap = StrokeCap.Round)
    when (mood) {
        PetMood.NEUTRAL -> drawPath(paths.softMouth, PetInk, style = line)
        PetMood.HAPPY -> drawPath(paths.smile, PetInk, style = line)
        PetMood.JOY, PetMood.PROUD -> drawPath(paths.bigSmile, PetInk, style = line)
        PetMood.SHOCK -> drawOval(PetInk, topLeft = Offset(94f, 99f), size = Size(12f, 15f))
        PetMood.THINKING -> drawPath(paths.flatMouth, PetInk, style = line)
        PetMood.COLD -> drawPath(paths.wavyMouth, PetInk, style = line)
    }
}

private fun DrawScope.drawEyes(radiusX: Float, radiusY: Float, glintOffset: Offset) {
    listOf(80f, 120f).forEach { x ->
        drawOval(PetInk, topLeft = Offset(x - radiusX, 88f - radiusY), size = Size(radiusX * 2, radiusY * 2))
        drawCircle(Color.White, radius = 4.2f, center = Offset(x, 88f) + glintOffset)
    }
}

/** Плед на плечах в низком состоянии: зябко, но уютно. */
private fun DrawScope.drawPlaid() {
    drawRoundRect(
        color = RoomCream,
        topLeft = Offset(44f, 112f),
        size = Size(112f, 46f),
        cornerRadius = CornerRadius(22f, 22f)
    )
    listOf(60f, 88f, 116f, 144f).forEach { x ->
        drawLine(RoomPlaidStripe, Offset(x, 114f), Offset(x, 156f), strokeWidth = 5f)
    }
    listOf(126f, 144f).forEach { y ->
        drawLine(RoomWarm.copy(alpha = PLAID_STRIPE_ALPHA), Offset(46f, y), Offset(154f, y), strokeWidth = 5f)
    }
}

private fun DrawScope.drawHat(paths: PetPaths, hat: PetHat) {
    when (hat) {
        PetHat.NONE -> Unit

        PetHat.CAP -> {
            drawPath(paths.capTop, PetCap)
            drawPath(paths.capBrim, PetCapBrim)
        }

        PetHat.HEADPHONES -> {
            drawPath(paths.headband, PetHeadphones, style = Stroke(width = 8f, cap = StrokeCap.Round))
            drawRoundRect(
                PetHeadphones,
                topLeft = Offset(42f, 68f),
                size = Size(22f, 34f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                PetHeadphones,
                topLeft = Offset(136f, 68f),
                size = Size(22f, 34f),
                cornerRadius = CornerRadius(10f, 10f)
            )
        }
    }
}

private const val STAGE_GLOW_ALPHA = 0.34f
private const val SHADOW_ALPHA = 0.55f
private const val BODY_MID_STOP = 0.52f
private const val BODY_HIGHLIGHT_X = 0.36f
private const val BODY_HIGHLIGHT_Y = 0.28f
private const val BODY_RADIUS = 0.8f
private const val RIM_ALPHA = 0.6f
private const val BELLY_MARK_ALPHA = 0.6f
private const val BLUSH_ALPHA = 0.55f
private const val PLAID_STRIPE_ALPHA = 0.75f

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
