package ru.lct2026.finedu.feature.home.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import ru.lct2026.finedu.feature.home.ui.HomeBubble
import ru.lct2026.finedu.productcore.ui.sound.LocalSoundPlayer
import ru.lct2026.finedu.productcore.ui.sound.SoundEffect
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Место пузырька на кольце: [angle] в градусах (0° — справа, по часовой), диаметр [size], форма — [waves] волн
 * по контуру, [phase] (0..1) сдвигает покачивание, чтобы пузырьки не двигались в такт.
 */
internal class BubbleSlot(val angle: Float, val size: Dp, val waves: Int, val phase: Float)

/** Как выглядит пузырёк: цвет подсветки и акцентная кромка (без пульсации — ТЗ запрещает давящие механики). */
internal class BubbleStyle(val tint: Color, val isAccent: Boolean = false)

/**
 * Прозрачные пузырьки по кругу вокруг Дзыня. Центр кольца — середина свободной области между [header] (реплика
 * и плашки) и нижним краем [contentPadding] (шапка и нижнее меню); [background] — комната на весь экран, её
 * центр совмещается с центром кольца, чтобы питомец стоял внутри. Тап раскрывает пузырёк в карточку в центре,
 * остальные съезжают в ряд внизу; тап мимо, «Назад» или повторный тап сворачивают. На тесных экранах пузырьки
 * уменьшаются. В «Спокойном режиме» пузырьки не покачиваются, раскрытие — без анимации.
 */
@Composable
internal fun BubbleField(
    bubbles: List<HomeBubble>,
    openBubble: HomeBubble?,
    slot: (HomeBubble) -> BubbleSlot,
    style: @Composable (HomeBubble) -> BubbleStyle,
    onBubbleClick: (HomeBubble) -> Unit,
    onDismiss: () -> Unit,
    contentPadding: PaddingValues,
    background: @Composable () -> Unit,
    header: @Composable () -> Unit,
    collapsed: @Composable (HomeBubble) -> Unit,
    expanded: @Composable (HomeBubble) -> Unit,
    collapseLabel: String,
    modifier: Modifier = Modifier
) {
    // «Пузынь» — и когда пузырёк раскрывается, и когда сворачивается.
    val sound = LocalSoundPlayer.current
    val onBubbleTap: (HomeBubble) -> Unit = { bubble ->
        sound.play(SoundEffect.BUBBLE)
        onBubbleClick(bubble)
    }
    val onBubbleDismiss = {
        sound.play(SoundEffect.BUBBLE)
        onDismiss()
    }
    BackHandler(enabled = openBubble != null, onBack = onBubbleDismiss)
    val reduceMotion = FinEduTheme.reduceMotion
    val spec: AnimationSpec<Float> = if (reduceMotion) snap() else spring(OPEN_DAMPING, Spring.StiffnessMediumLow)
    val time = if (reduceMotion) null else rememberAmbientTime()
    val scrim by animateFloatAsState(if (openBubble != null) 1f else 0f, spec, label = "scrim")
    val progress = bubbles.associateWith { bubble ->
        key(bubble) {
            OpenProgress(
                open = animateFloatAsState(if (bubble == openBubble) 1f else 0f, spec, label = "open"),
                docked = animateFloatAsState(
                    if (openBubble != null && bubble != openBubble) 1f else 0f,
                    spec,
                    label = "docked"
                )
            )
        }
    }
    Layout(
        modifier = modifier,
        content = {
            Box(modifier = Modifier.layoutId(BACKGROUND_ID)) { background() }
            Box(
                modifier = Modifier
                    .layoutId(SCRIM_ID)
                    .drawBehind { drawRect(Color.Black.copy(alpha = SCRIM_ALPHA * scrim)) }
                    .then(
                        if (openBubble != null) {
                            Modifier
                                .clearAndSetSemantics {}
                                .clickable(interactionSource = null, indication = null, onClick = onBubbleDismiss)
                        } else {
                            Modifier
                        }
                    )
            )
            Box(modifier = Modifier.layoutId(HEADER_ID)) { header() }
            bubbles.forEach { bubble ->
                val isOpen = bubble == openBubble
                Bubble(
                    slot = slot(bubble),
                    style = style(bubble),
                    open = progress.getValue(bubble).open,
                    time = time,
                    isOpen = isOpen,
                    onClick = { onBubbleTap(bubble) },
                    onCollapse = onBubbleDismiss,
                    collapseLabel = collapseLabel,
                    collapsed = { collapsed(bubble) },
                    expanded = { expanded(bubble) },
                    modifier = Modifier.layoutId(bubble)
                )
            }
        }
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val side = SIDE_PADDING.roundToPx()
        val gap = GAP.roundToPx()
        val top = contentPadding.calculateTopPadding().roundToPx()
        val bottom = contentPadding.calculateBottomPadding().roundToPx()

        val scrimPlaceable = measurables.first { it.layoutId == SCRIM_ID }.measure(Constraints.fixed(width, height))
        val headerPlaceable = measurables.first { it.layoutId == HEADER_ID }
            .measure(Constraints(maxWidth = width - 2 * side, maxHeight = height))
        val areaTop = top + headerPlaceable.height + gap
        val areaBottom = height - bottom - gap
        val centerX = width / 2f
        val centerY = (areaTop + areaBottom) / 2f
        // Комната выше экрана на двойной сдвиг: её центр — центр кольца, а края всё равно закрывают экран.
        val backgroundHeight = height + 2 * abs(centerY - height / 2f).roundToInt()
        val backgroundPlaceable = measurables.first { it.layoutId == BACKGROUND_ID }
            .measure(Constraints.fixed(width, backgroundHeight))

        // Кольцо: эллипс вокруг центра, самый крупный пузырёк не выходит за края, шапку и меню. Если по высоте
        // тесно, пузырьки уменьшаются, но не меньше зоны нажатия.
        val maxDiameter = bubbles.maxOf { slot(it).size.toPx() }
        val halfHeight = (areaBottom - areaTop) / 2f
        val fit = (halfHeight / (RING_COMFORT_RADIUS.toPx() + maxDiameter / 2)).coerceIn(MIN_FIT, 1f)
        val maxRadius = maxDiameter * fit / 2
        val radiusX = (centerX - maxRadius - side).coerceAtLeast(0f)
        val radiusY = (halfHeight - maxRadius).coerceAtLeast(0f)

        // Ряд внизу для свёрнутых пузырьков, пока один раскрыт; место раскрытого остаётся пустым.
        val dockStep = (width - 2f * side) / bubbles.size
        val dockSize = min(DOCK_SIZE.toPx(), dockStep - gap / 2f)
        val dockY = areaBottom - dockSize / 2f
        val cardMaxHeight = (dockY - dockSize / 2f - gap - areaTop).roundToInt().coerceAtLeast(0)
        val cardWidth = min(width - 2 * side, CARD_MAX_WIDTH.roundToPx())
        val bubbleConstraints = Constraints(maxWidth = cardWidth, maxHeight = cardMaxHeight)

        val placed = bubbles.mapIndexed { index, bubble ->
            val placeable = measurables.first { it.layoutId == bubble }.measure(bubbleConstraints)
            val slot = slot(bubble)
            val angle = slot.angle / DEGREES_HALF_TURN * PI.toFloat()
            val ring = Offset(centerX + radiusX * cos(angle), centerY + radiusY * sin(angle))
            val dock = Offset(side + dockStep * (index + HALF), dockY)
            val cardTop = (areaTop + (cardMaxHeight - placeable.height) / 2f).coerceAtLeast(areaTop.toFloat())
            val card = Offset(centerX, cardTop + placeable.height / 2f)
            Triple(placeable, bubble, Triple(ring, dock, card))
        }
        layout(width, height) {
            backgroundPlaceable.place(0, (centerY - backgroundHeight / 2f).roundToInt())
            scrimPlaceable.place(0, 0)
            headerPlaceable.place(side, top)
            placed.forEach { (placeable, bubble, targets) ->
                val (ring, dock, card) = targets
                val (open, docked) = progress.getValue(bubble)
                val center = ring + (card - ring) * open.value + (dock - ring) * docked.value
                val slot = slot(bubble)
                val dockScale = dockSize / slot.size.toPx()
                placeable.placeWithLayer(
                    x = (center.x - placeable.width / 2f).roundToInt(),
                    y = (center.y - placeable.height / 2f).roundToInt(),
                    // Раскрытый пузырёк — поверх остальных, чтобы свёрнутые не перехватывали нажатия.
                    zIndex = if (open.value > 0f) 1f else 0f
                ) {
                    val rest = (1f - open.value - docked.value).coerceIn(0f, 1f)
                    translationY = rest * BOB.toPx() * wave(time?.invoke(), BOB_CYCLES, slot.phase)
                    val scale = lerp(lerp(1f, fit, 1f - open.value.coerceIn(0f, 1f)), dockScale, docked.value)
                    scaleX = scale
                    scaleY = scale
                }
            }
        }
    }
}

private data class OpenProgress(val open: State<Float>, val docked: State<Float>)

/** Общее время фоновых петель: 0..1 за [AMBIENT_CYCLE_MS]; каждая петля укладывает в цикл целое число оборотов. */
@Composable
private fun rememberAmbientTime(): () -> Float {
    val state = rememberInfiniteTransition(label = "bubbles").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(AMBIENT_CYCLE_MS, easing = LinearEasing)),
        label = "time"
    )
    return remember(state) { { state.value } }
}

private fun wave(time: Float?, cycles: Int, phase: Float): Float =
    if (time == null) 0f else sin(2 * PI.toFloat() * (time * cycles + phase))

@Composable
private fun Bubble(
    slot: BubbleSlot,
    style: BubbleStyle,
    open: State<Float>,
    time: (() -> Float)?,
    isOpen: Boolean,
    onClick: () -> Unit,
    onCollapse: () -> Unit,
    collapseLabel: String,
    collapsed: @Composable () -> Unit,
    expanded: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FinEduTheme.colors
    val path = remember { Path() }
    val showExpanded = isOpen || open.value > VISIBLE_THRESHOLD
    Layout(
        modifier = modifier
            .clipToBounds()
            .drawBehind {
                val wobble = wave(time?.invoke(), WOBBLE_CYCLES, slot.phase) * 2 * PI.toFloat()
                drawBubble(
                    path = path,
                    open = open.value.coerceIn(0f, 1f),
                    waves = slot.waves,
                    wobblePhase = wobble,
                    tint = style.tint,
                    isAccent = style.isAccent,
                    cardTop = colors.glassStrongTop,
                    cardBottom = colors.glassStrongBottom,
                    cardBorder = colors.glassStrongBorder
                )
            }
            .then(
                if (isOpen) {
                    Modifier.semantics {
                        onClick(label = collapseLabel) {
                            onCollapse()
                            true
                        }
                    }
                } else {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick
                    )
                }
            ),
        content = {
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = 1f - (open.value * 2).coerceIn(0f, 1f) }
                    .then(if (isOpen) Modifier.clearAndSetSemantics {} else Modifier)
            ) { collapsed() }
            if (showExpanded) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = ((open.value - EXPANDED_FADE_START) / (1f - EXPANDED_FADE_START)).coerceIn(0f, 1f)
                    }
                ) { expanded() }
            }
        }
    ) { measurables, constraints ->
        val collapsedSize = slot.size.roundToPx()
        val compact = measurables[0].measure(Constraints.fixed(collapsedSize, collapsedSize))
        val card = measurables.getOrNull(1)?.measure(
            Constraints(
                minWidth = constraints.maxWidth,
                maxWidth = constraints.maxWidth,
                maxHeight = constraints.maxHeight
            )
        )
        val t = open.value.coerceAtLeast(0f)
        val width = lerp(collapsedSize.toFloat(), (card?.width ?: collapsedSize).toFloat(), t).roundToInt()
        val height = lerp(collapsedSize.toFloat(), (card?.height ?: collapsedSize).toFloat(), t).roundToInt()
        layout(width, height) {
            compact.place((width - compact.width) / 2, (height - compact.height) / 2)
            card?.place((width - card.width) / 2, (height - card.height) / 2)
        }
    }
}

/**
 * Контур: суперэллипс, который из круга с «волнами» (пузырь, [open] = 0) становится скруглённой карточкой
 * ([open] = 1). Стекло: градиент с подсветкой [tint], кромка и блик сверху слева, как у мыльного пузыря.
 */
@Suppress("LongParameterList")
private fun DrawScope.drawBubble(
    path: Path,
    open: Float,
    waves: Int,
    wobblePhase: Float,
    tint: Color,
    isAccent: Boolean,
    cardTop: Color,
    cardBottom: Color,
    cardBorder: Color
) {
    val stroke = (if (isAccent) ACCENT_STROKE else STROKE).toPx()
    val a = size.width / 2 - stroke
    val b = size.height / 2 - stroke
    val exponent = lerp(CIRCLE_EXPONENT, CARD_EXPONENT, open)
    val amplitude = WOBBLE * (1 - open)
    path.reset()
    for (i in 0..CONTOUR_POINTS) {
        val theta = 2 * PI.toFloat() * i / CONTOUR_POINTS
        val c = cos(theta)
        val s = sin(theta)
        val radius = (1 + amplitude * sin(waves * theta + wobblePhase)) / (1 + amplitude)
        val x = size.width / 2 + a * radius * sign(c) * abs(c).pow(2 / exponent)
        val y = size.height / 2 + b * radius * sign(s) * abs(s).pow(2 / exponent)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    // Затемнение под стеклом: подпись читается над светлыми предметами, а карточка — поверх Дзыня.
    drawPath(path, Color.Black.copy(alpha = lerp(BUBBLE_SHADE_ALPHA, CARD_SHADE_ALPHA, open)))
    val bubbleTop = tint.copy(alpha = BUBBLE_TINT_ALPHA)
    val bubbleBottom = Color.White.copy(alpha = BUBBLE_BOTTOM_ALPHA)
    drawPath(
        path,
        Brush.verticalGradient(listOf(lerp(bubbleTop, cardTop, open), lerp(bubbleBottom, cardBottom, open)))
    )
    // Радужная кромка изнутри — только у пузыря.
    drawPath(
        path,
        Brush.radialGradient(
            0f to Color.Transparent,
            RIM_START to Color.Transparent,
            1f to tint.copy(alpha = RIM_ALPHA * (1 - open)),
            center = center,
            radius = maxOf(a, b)
        )
    )
    val border = if (isAccent) tint else Color.White.copy(alpha = BORDER_ALPHA)
    drawPath(path, lerp(border, cardBorder, open), style = Stroke(width = stroke))
    // Блик.
    val glint = Color.White.copy(alpha = GLINT_ALPHA * (1 - open))
    drawOval(
        glint,
        topLeft = Offset(size.width / 2 - a * GLINT_X, size.height / 2 - b * GLINT_Y),
        size = Size(a * GLINT_WIDTH, b * GLINT_HEIGHT)
    )
    drawCircle(
        glint,
        radius = a * GLINT_DOT,
        center = Offset(size.width / 2 - a * GLINT_DOT_X, size.height / 2 - b * GLINT_DOT_Y)
    )
}

private const val BACKGROUND_ID = "background"
private const val SCRIM_ID = "scrim"
private const val HEADER_ID = "header"
private const val HALF = 0.5f
private const val DEGREES_HALF_TURN = 180f
private const val SCRIM_ALPHA = 0.35f
private const val OPEN_DAMPING = 0.8f
private const val VISIBLE_THRESHOLD = 0.01f
private const val EXPANDED_FADE_START = 0.4f

// Фоновые петли: цикл 24 с; покачивание — 6 раз (4 с), «переливание» формы — 3 раза (8 с). Амплитуда ±4dp.
private const val AMBIENT_CYCLE_MS = 24_000
private const val BOB_CYCLES = 6
private const val WOBBLE_CYCLES = 3
private val BOB = 4.dp

// Кольцу удобно, когда от центра до пузырьков по высоте не меньше этого; иначе пузырьки уменьшаются до MIN_FIT.
private val RING_COMFORT_RADIUS = 130.dp
private const val MIN_FIT = 0.75f

private val SIDE_PADDING = 16.dp
private val GAP = 12.dp
private val DOCK_SIZE = 52.dp
private val CARD_MAX_WIDTH = 400.dp

// Рисунок пузыря.
private const val CONTOUR_POINTS = 72
private const val CIRCLE_EXPONENT = 2f
private const val CARD_EXPONENT = 6f
private const val WOBBLE = 0.05f
private const val BUBBLE_SHADE_ALPHA = 0.3f
private const val CARD_SHADE_ALPHA = 0.72f
private const val BUBBLE_TINT_ALPHA = 0.22f
private const val BUBBLE_BOTTOM_ALPHA = 0.05f
private const val RIM_START = 0.6f
private const val RIM_ALPHA = 0.45f
private const val BORDER_ALPHA = 0.4f
private const val GLINT_ALPHA = 0.55f
private const val GLINT_X = 0.62f
private const val GLINT_Y = 0.7f
private const val GLINT_WIDTH = 0.42f
private const val GLINT_HEIGHT = 0.22f
private const val GLINT_DOT = 0.06f
private const val GLINT_DOT_X = 0.62f
private const val GLINT_DOT_Y = 0.32f
private val STROKE = 1.5.dp
private val ACCENT_STROKE = 2.5.dp
