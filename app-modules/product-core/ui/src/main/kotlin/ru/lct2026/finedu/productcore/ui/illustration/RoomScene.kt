package ru.lct2026.finedu.productcore.ui.illustration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.RoomBaseboard
import ru.lct2026.finedu.productcore.ui.theme.RoomBoxDark
import ru.lct2026.finedu.productcore.ui.theme.RoomBoxLight
import ru.lct2026.finedu.productcore.ui.theme.RoomBoxMid
import ru.lct2026.finedu.productcore.ui.theme.RoomCream
import ru.lct2026.finedu.productcore.ui.theme.RoomDim
import ru.lct2026.finedu.productcore.ui.theme.RoomFloor
import ru.lct2026.finedu.productcore.ui.theme.RoomHill
import ru.lct2026.finedu.productcore.ui.theme.RoomHouseLeft
import ru.lct2026.finedu.productcore.ui.theme.RoomHouseRight
import ru.lct2026.finedu.productcore.ui.theme.RoomLampBase
import ru.lct2026.finedu.productcore.ui.theme.RoomLitWindow
import ru.lct2026.finedu.productcore.ui.theme.RoomMoon
import ru.lct2026.finedu.productcore.ui.theme.RoomOutline
import ru.lct2026.finedu.productcore.ui.theme.RoomPlaidStripe
import ru.lct2026.finedu.productcore.ui.theme.RoomSky
import ru.lct2026.finedu.productcore.ui.theme.RoomStar
import ru.lct2026.finedu.productcore.ui.theme.RoomWall
import ru.lct2026.finedu.productcore.ui.theme.RoomWallStripe
import ru.lct2026.finedu.productcore.ui.theme.RoomWarm

/**
 * Уголок Дзыня — ночная сцена «Дзынь · 2026» (viewBox 360×340, выравнивание по низу): стена, батарея, коробка «ДОМ»
 * и предметы достигнутых целей — плед со звёздочками, лампа, окно с видом, самокат. Предметы и звёздочки не
 * пропадают. [dim] — низкое состояние: сцена приглушена, без красного и без «пустой миски». [pet] — питомец,
 * он сидит в коробке между задним и передним слоями.
 */
@Composable
fun RoomScene(
    placedGoalIds: Set<String>,
    stars: Int,
    modifier: Modifier = Modifier,
    dim: Boolean = false,
    pet: @Composable (Modifier) -> Unit
) {
    val paths = remember { RoomPaths() }
    val measurer = rememberTextMeasurer()
    val boxLabel = stringResource(R.string.room_box_label)
    val description = stringResource(R.string.room_a11y)
    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .semantics { contentDescription = description }
    ) {
        val scale = max(maxWidth / SCENE_WIDTH.dp, maxHeight / SCENE_HEIGHT.dp)
        val offsetX = (maxWidth - SCENE_WIDTH.dp * scale) / 2
        val offsetY = maxHeight - SCENE_HEIGHT.dp * scale
        val petWidth = PET_WIDTH.dp * scale
        val petHeight = petWidth * PET_ASPECT
        Canvas(modifier = Modifier.fillMaxSize()) {
            inScene { drawBackLayer(paths, placedGoalIds) }
        }
        pet(
            Modifier
                .offset(
                    x = offsetX + PET_CENTER_X.dp * scale - petWidth / 2,
                    y = offsetY + PET_GROUND_Y.dp * scale - petHeight * PET_GROUND_FRACTION
                )
                .width(petWidth)
                .height(petHeight)
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            inScene { drawFrontLayer(paths, placedGoalIds, stars, measurer, boxLabel) }
            if (dim) drawRect(RoomDim.copy(alpha = DIM_ALPHA))
        }
    }
}

private const val SCENE_WIDTH = 360f
private const val SCENE_HEIGHT = 340f
private const val PET_WIDTH = 150f
private const val PET_CENTER_X = 175f
private const val PET_GROUND_Y = 262f

// Пропорции PetView: высота = 190/200 ширины, «земля» на 158/190 высоты.
private const val PET_ASPECT = 0.95f
private const val PET_GROUND_FRACTION = 158f / 190f
private const val DIM_ALPHA = 0.35f
private const val MAX_STARS = 5

/** Координаты сцены: масштаб «slice» и выравнивание по низу, как `preserveAspectRatio="xMidYMax slice"`. */
private fun DrawScope.inScene(block: DrawScope.() -> Unit) {
    val scale = max(size.width / SCENE_WIDTH, size.height / SCENE_HEIGHT)
    withTransform({
        translate((size.width - SCENE_WIDTH * scale) / 2, size.height - SCENE_HEIGHT * scale)
        scale(scale, scale, pivot = Offset.Zero)
    }, block)
}

private class RoomPaths {
    val hill = path("M198 132 C222 112 246 118 266 128 C282 118 300 116 312 124 L312 160 L198 160 Z")
    val leftHouse = path("M214 160 L214 138 L226 128 L238 138 L238 160 Z")
    val rightHouse = path("M280 160 L280 142 L292 134 L304 142 L304 160 Z")
    val lampShade = path("M288 118 L336 118 L326 82 L298 82 Z")
    val boxBackTop = path("M112 206 L238 206 L226 186 L124 186 Z")
    val plaidBack = path("M118 212 C140 196 210 196 232 212 L232 236 L118 236 Z")
    val boxFront = path("M106 264 L244 264 L240 306 L110 306 Z")
    val leftFlap = path("M106 264 L90 246 L112 246 Z")
    val rightFlap = path("M244 264 L262 248 L238 248 Z")
    val plaidFront = path(
        "M100 264 C130 256 150 268 176 262 C204 256 226 268 250 262 L256 304 C220 312 150 312 96 306 Z"
    )
    val stars = listOf(
        Offset(126f, 284f),
        Offset(158f, 298f),
        Offset(194f, 284f),
        Offset(226f, 298f),
        Offset(242f, 276f)
    )
        .map { star(it) }

    private fun path(data: String): Path = PathParser().parsePathString(data).toPath()

    private fun star(center: Offset): Path = Path().apply {
        repeat(STAR_POINTS) { i ->
            val radius = if (i % 2 == 0) STAR_OUTER else STAR_INNER
            val angle = -PI / 2 + i * PI / (STAR_POINTS / 2)
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

private const val STAR_POINTS = 10
private const val STAR_OUTER = 7f
private const val STAR_INNER = 3f

private fun DrawScope.drawBackLayer(paths: RoomPaths, items: Set<String>) {
    drawRect(RoomWall, size = Size(SCENE_WIDTH, SCENE_HEIGHT))
    var x = 0f
    while (x < SCENE_WIDTH) {
        drawRect(RoomWallStripe, topLeft = Offset(x, 0f), size = Size(10f, 238f))
        x += 28f
    }
    if (RoomItems.WINDOW in items) drawWindow(paths)
    drawRect(RoomBaseboard, topLeft = Offset(0f, 238f), size = Size(SCENE_WIDTH, 8f))
    drawRect(RoomFloor, topLeft = Offset(0f, 246f), size = Size(SCENE_WIDTH, 74f))
    listOf(270f, 296f).forEach { y ->
        drawLine(RoomOutline.copy(alpha = FLOOR_LINE_ALPHA), Offset(0f, y), Offset(SCENE_WIDTH, y), strokeWidth = 1f)
    }
    drawRect(RoomBoxLight, topLeft = Offset(0f, 318f), size = Size(SCENE_WIDTH, 22f))
    drawLine(RoomOutline, Offset(0f, 318f), Offset(SCENE_WIDTH, 318f), strokeWidth = 2.5f)
    drawRadiator(glow = RoomItems.LAMP !in items)
    if (RoomItems.LAMP in items) drawLamp(paths)
    drawPath(paths.boxBackTop, RoomBoxMid)
    drawPath(paths.boxBackTop, RoomOutline, style = Stroke(width = 2f))
    drawRect(RoomBoxDark, topLeft = Offset(112f, 206f), size = Size(126f, 60f))
    if (RoomItems.PLAID in items) drawPlaid(paths.plaidBack)
}

private fun DrawScope.drawWindow(paths: RoomPaths) {
    val outline = Stroke(width = 2.5f)
    drawRoundRect(RoomSky, Offset(196f, 34f), Size(118f, 128f), CornerRadius(10f, 10f))
    drawCircle(RoomMoon, radius = 14f, center = Offset(222f, 66f))
    drawPath(paths.hill, RoomHill)
    drawPath(paths.leftHouse, RoomHouseLeft)
    drawPath(paths.rightHouse, RoomHouseRight)
    drawRect(RoomLitWindow, Offset(222f, 144f), Size(7f, 7f))
    drawRect(RoomLitWindow, Offset(288f, 146f), Size(7f, 7f))
    drawLine(RoomCream, Offset(255f, 34f), Offset(255f, 162f), strokeWidth = 6f)
    drawLine(RoomCream, Offset(196f, 98f), Offset(314f, 98f), strokeWidth = 6f)
    drawRoundRect(RoomOutline, Offset(196f, 34f), Size(118f, 128f), CornerRadius(10f, 10f), style = outline)
    drawRoundRect(RoomCream, Offset(190f, 160f), Size(130f, 10f), CornerRadius(4f, 4f))
}

private fun DrawScope.drawRadiator(glow: Boolean) {
    if (glow) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(RoomWarm.copy(alpha = WARM_GLOW_ALPHA), Color.Transparent),
                center = Offset(60f, 190f),
                radius = 80f
            ),
            radius = 80f,
            center = Offset(60f, 190f)
        )
    }
    drawLine(RoomOutline, Offset(20f, 246f), Offset(20f, 232f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(RoomOutline, Offset(100f, 246f), Offset(100f, 232f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    listOf(18f, 34f, 50f, 66f, 82f).forEach { x ->
        drawRoundRect(RoomCream, Offset(x, 150f), Size(16f, 84f), CornerRadius(8f, 8f))
        drawRoundRect(RoomOutline, Offset(x, 150f), Size(16f, 84f), CornerRadius(8f, 8f), style = Stroke(2f))
    }
}

private fun DrawScope.drawLamp(paths: RoomPaths) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(RoomWarm.copy(alpha = LAMP_GLOW_ALPHA), Color.Transparent),
            center = Offset(312f, 120f),
            radius = 110f
        ),
        radius = 110f,
        center = Offset(312f, 120f)
    )
    drawLine(RoomOutline, Offset(312f, 118f), Offset(312f, 258f), strokeWidth = 3f)
    drawOval(RoomLampBase, topLeft = Offset(294f, 253f), size = Size(36f, 10f))
    drawPath(paths.lampShade, RoomWarm)
    drawPath(paths.lampShade, RoomOutline, style = Stroke(width = 2.5f))
}

private fun DrawScope.drawFrontLayer(
    paths: RoomPaths,
    items: Set<String>,
    stars: Int,
    measurer: TextMeasurer,
    boxLabel: String
) {
    drawPath(paths.leftFlap, RoomBoxMid)
    drawPath(paths.rightFlap, RoomBoxMid)
    drawPath(paths.boxFront, RoomBoxLight)
    drawPath(paths.boxFront, RoomOutline, style = Stroke(width = 2.5f))
    rotate(degrees = LABEL_ROTATION, pivot = Offset(175f, 285f)) {
        drawRoundRect(RoomCream, Offset(150f, 274f), Size(50f, 22f), CornerRadius(3f, 3f))
        val label = measurer.measure(
            boxLabel,
            TextStyle(color = RoomOutline, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        )
        drawText(
            label,
            topLeft = Offset(175f - label.size.width / 2f, 285f - label.size.height / 2f)
        )
    }
    if (RoomItems.PLAID in items) {
        drawPlaid(paths.plaidFront)
        paths.stars.take(stars.coerceAtMost(MAX_STARS)).forEach { star ->
            drawPath(star, RoomStar)
            drawPath(star, RoomOutline, style = Stroke(width = 1.2f))
        }
    }
    if (RoomItems.SCOOTER in items) drawScooter()
}

private fun DrawScope.drawPlaid(shape: Path) {
    drawPath(shape, RoomCream)
    clipPath(shape) {
        var x = 100f
        while (x < 260f) {
            drawLine(RoomPlaidStripe.copy(alpha = PLAID_ALPHA), Offset(x, 190f), Offset(x, 320f), strokeWidth = 6f)
            x += 18f
        }
        var y = 196f
        while (y < 320f) {
            drawLine(RoomWarm.copy(alpha = PLAID_ALPHA), Offset(90f, y), Offset(260f, y), strokeWidth = 6f)
            y += 18f
        }
    }
    drawPath(shape, RoomOutline, style = Stroke(width = 2.5f))
}

private fun DrawScope.drawScooter() {
    val line = Stroke(width = 4f, cap = StrokeCap.Round)
    drawLine(RoomHouseRight, Offset(280f, 294f), Offset(334f, 294f), strokeWidth = line.width, cap = line.cap)
    drawLine(RoomHouseRight, Offset(330f, 294f), Offset(322f, 248f), strokeWidth = line.width, cap = line.cap)
    drawLine(RoomHouseRight, Offset(312f, 248f), Offset(332f, 248f), strokeWidth = line.width, cap = line.cap)
    listOf(284f, 332f).forEach { x ->
        drawCircle(RoomOutline, radius = 8f, center = Offset(x, 302f))
        drawCircle(RoomCream, radius = 3f, center = Offset(x, 302f))
    }
}

private const val FLOOR_LINE_ALPHA = 0.18f
private const val WARM_GLOW_ALPHA = 0.4f
private const val LAMP_GLOW_ALPHA = 0.55f
private const val PLAID_ALPHA = 0.7f
private const val LABEL_ROTATION = -4f

@Preview(widthDp = 360, heightDp = 340)
@Composable
private fun RoomSceneStartPreview() {
    FinEduPreview {
        RoomScene(placedGoalIds = emptySet(), stars = 0, modifier = Modifier.fillMaxSize()) { petModifier ->
            PetView(PetLook(PetFur.LILAC, PetHat.NONE), petModifier, stage = PetStage.BABY)
        }
    }
}

@Preview(widthDp = 360, heightDp = 300)
@Composable
private fun RoomSceneFullPreview() {
    FinEduPreview {
        Box {
            RoomScene(
                placedGoalIds = setOf(RoomItems.PLAID, RoomItems.LAMP, RoomItems.WINDOW, RoomItems.SCOOTER),
                stars = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) { petModifier ->
                PetView(PetLook(PetFur.MINT, PetHat.CAP), petModifier, mood = PetMood.PROUD)
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 300)
@Composable
private fun RoomSceneDimPreview() {
    FinEduPreview {
        RoomScene(
            placedGoalIds = setOf(RoomItems.PLAID),
            stars = 1,
            dim = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { petModifier ->
            PetView(PetLook(PetFur.CORAL, PetHat.HEADPHONES), petModifier, mood = PetMood.COLD)
        }
    }
}
