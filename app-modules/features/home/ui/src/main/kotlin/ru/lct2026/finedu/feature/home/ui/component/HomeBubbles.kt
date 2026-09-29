package ru.lct2026.finedu.feature.home.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.home.ui.HomeBubble
import ru.lct2026.finedu.feature.home.ui.HomeGoal
import ru.lct2026.finedu.feature.home.ui.HomeNotice
import ru.lct2026.finedu.feature.home.ui.HomeUiState
import ru.lct2026.finedu.feature.home.ui.R
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameRules
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.BagChip
import ru.lct2026.finedu.productcore.ui.components.DzynkiAmount
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.FinTab
import ru.lct2026.finedu.productcore.ui.components.TubeIndicator
import ru.lct2026.finedu.productcore.ui.components.color
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.components.iconRes
import ru.lct2026.finedu.productcore.ui.components.labelRes
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Места на кольце: показатели — слева, деньги, цель и задание — справа, «Завершить неделю» — внизу, отдельно
 * от остальных, чтобы не нажать случайно. У младших нет задания, цель опускается на его место.
 */
internal fun HomeBubble.slot(isYounger: Boolean): BubbleSlot = when (this) {
    is HomeBubble.Stat -> when (stat) {
        PetStat.CHARGE -> BubbleSlot(angle = 215f, size = 66.dp, waves = 3, phase = 0f)
        PetStat.VIBE -> BubbleSlot(angle = 180f, size = 66.dp, waves = 2, phase = 0.33f)
        PetStat.CALM -> BubbleSlot(angle = 145f, size = 66.dp, waves = 4, phase = 0.66f)
    }

    HomeBubble.Balance -> BubbleSlot(angle = if (isYounger) -30f else -38f, size = 92.dp, waves = 3, phase = 0.15f)

    HomeBubble.Goal -> BubbleSlot(angle = if (isYounger) 30f else 0f, size = 80.dp, waves = 2, phase = 0.5f)

    HomeBubble.Quest -> BubbleSlot(angle = 38f, size = 74.dp, waves = 4, phase = 0.8f)

    HomeBubble.FinishWeek -> BubbleSlot(angle = 90f, size = 68.dp, waves = 3, phase = 0.4f)
}

@Composable
internal fun HomeBubble.style(state: HomeUiState.Content): BubbleStyle = when (this) {
    is HomeBubble.Stat -> BubbleStyle(tint = stat.color)
    HomeBubble.Balance -> BubbleStyle(tint = FinEduTheme.colors.gold, isAccent = state.unallocated > Dzynki.ZERO)
    HomeBubble.Goal -> BubbleStyle(tint = FinEduTheme.colors.savings)
    HomeBubble.Quest -> BubbleStyle(tint = FinEduTheme.colors.gold)
    HomeBubble.FinishWeek -> BubbleStyle(tint = FinEduTheme.colors.selection)
}

/** Свёрнутый пузырёк: иконка, число или кольцо прогресса. TalkBack читает одну фразу на пузырёк. */
@Composable
internal fun CollapsedBubble(bubble: HomeBubble, state: HomeUiState.Content) {
    when (bubble) {
        is HomeBubble.Stat -> {
            val stat = bubble.stat
            val value = state.pet[stat]
            val label = stringResource(stat.labelRes)
            val description = if (value <= GameRules.TIRED_THRESHOLD) {
                stringResource(CoreR.string.stat_tired_a11y, label)
            } else {
                stringResource(CoreR.string.stat_value_a11y, label, value)
            }
            RingBubble(
                description = description,
                iconRes = stat.iconRes,
                tint = stat.color,
                progress = value / GameRules.STAT_MAX.toFloat(),
                text = value.toString().takeUnless { state.isYounger }
            )
        }

        HomeBubble.Balance -> {
            val balance = dzynkiText(state.balance)
            val description = if (state.unallocated > Dzynki.ZERO) {
                stringResource(R.string.home_balance_unallocated_a11y, balance, dzynkiText(state.unallocated))
            } else {
                stringResource(R.string.home_balance_a11y, balance)
            }
            CenteredContent(description) {
                Image(
                    painter = painterResource(CoreR.drawable.ic_coin),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Text(text = state.balance.amount.toString(), style = MaterialTheme.typography.titleMedium)
            }
        }

        HomeBubble.Goal -> {
            val goal = state.goal
            RingBubble(
                description = goal?.let { goalText(it, state.isYounger) } ?: stringResource(R.string.home_goals_done),
                iconRes = if (goal == null) CoreR.drawable.ic_check else CoreR.drawable.ic_goal,
                tint = FinEduTheme.colors.savings,
                progress = goal?.progress ?: 1f,
                text = null
            )
        }

        HomeBubble.Quest -> CenteredContent(
            description = state.quest?.let { stringResource(R.string.home_quest, it.title) }
                ?: stringResource(R.string.home_quests_done)
        ) {
            BubbleIcon(CoreR.drawable.ic_nav_quests, FinEduTheme.colors.gold)
        }

        HomeBubble.FinishWeek -> CenteredContent(description = stringResource(R.string.home_finish_week)) {
            BubbleIcon(CoreR.drawable.ic_nav_results, MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun CenteredContent(description: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}

@Composable
private fun RingBubble(description: String, @DrawableRes iconRes: Int, tint: Color, progress: Float, text: String?) {
    val track = FinEduTheme.colors.glassBorder
    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(RingInset)
        ) {
            val stroke = Stroke(width = RingWidth.toPx(), cap = StrokeCap.Round)
            drawArc(track, startAngle = 0f, sweepAngle = FULL_TURN, useCenter = false, style = stroke)
            drawArc(tint, RING_START, FULL_TURN * progress.coerceIn(0f, 1f), useCenter = false, style = stroke)
        }
        CenteredContent(description) {
            BubbleIcon(iconRes, tint)
            if (text != null) Text(text = text, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun BubbleIcon(@DrawableRes iconRes: Int, tint: Color) {
    Icon(painter = painterResource(iconRes), contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
}

/** Раскрытый пузырёк: заголовок, подробности и кнопка действия. */
@Composable
internal fun ExpandedBubble(
    bubble: HomeBubble,
    state: HomeUiState.Content,
    onNavigate: (FinEduRoute) -> Unit,
    onSelectTab: (FinTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (bubble) {
            is HomeBubble.Stat -> StatDetails(bubble.stat, state, onSelectTab)
            HomeBubble.Balance -> BalanceDetails(state, onNavigate)
            HomeBubble.Goal -> GoalDetails(state, onNavigate)
            HomeBubble.Quest -> QuestDetails(state, onNavigate)
            HomeBubble.FinishWeek -> FinishWeekDetails(state, onNavigate)
        }
    }
}

@Composable
private fun StatDetails(stat: PetStat, state: HomeUiState.Content, onSelectTab: (FinTab) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        TubeIndicator(stat = stat, value = state.pet[stat], showValue = !state.isYounger)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Title(stringResource(stat.labelRes))
            val isLow = when (val notice = state.notice) {
                is HomeNotice.Low -> notice.stat == stat
                HomeNotice.Return, null -> false
            }
            Text(
                text = stringResource(if (isLow) stat.lowNoticeRes else stat.hintRes),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    val (textRes, tab) = when (stat) {
        PetStat.CHARGE, PetStat.VIBE -> R.string.home_to_shop to FinTab.SHOP
        PetStat.CALM -> R.string.home_to_savings to FinTab.SAVINGS
    }
    FinButton(text = stringResource(textRes), onClick = { onSelectTab(tab) }, style = FinButtonStyle.Secondary)
}

@Composable
private fun BalanceDetails(state: HomeUiState.Content, onNavigate: (FinEduRoute) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Title(stringResource(R.string.home_balance), Modifier.weight(1f))
        DzynkiAmount(amount = state.balance, style = MaterialTheme.typography.titleLarge, coinSize = 24.dp)
    }
    if (state.isYounger) {
        BagChip(bag = Bag.SAVINGS, amount = state.savings)
    } else {
        Bag.entries.forEach { bag -> BagChip(bag = bag, amount = state.amountIn(bag)) }
    }
    if (state.unallocated > Dzynki.ZERO) {
        FinButton(
            text = stringResource(R.string.home_distribute, dzynkiText(state.unallocated)),
            onClick = { onNavigate(FinEduRoute.Budget) }
        )
    } else {
        FinButton(
            text = stringResource(R.string.home_plan),
            onClick = { onNavigate(FinEduRoute.Budget) },
            style = FinButtonStyle.Secondary
        )
    }
}

@Composable
private fun GoalDetails(state: HomeUiState.Content, onNavigate: (FinEduRoute) -> Unit) {
    Title(stringResource(R.string.home_goal))
    val goal = state.goal
    if (goal == null) {
        Text(text = stringResource(R.string.home_goals_done), style = MaterialTheme.typography.bodyMedium)
    } else {
        Text(text = goalText(goal, state.isYounger), style = MaterialTheme.typography.bodyLarge)
        LinearProgressIndicator(
            progress = { goal.progress },
            color = FinEduTheme.colors.savings,
            trackColor = FinEduTheme.colors.glassBorder,
            drawStopIndicator = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        if (!state.isYounger) {
            Text(
                text = goalWeeksText(goal.weeksLeft),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    FinButton(
        text = stringResource(R.string.home_to_savings),
        onClick = { onNavigate(FinEduRoute.Savings) },
        style = FinButtonStyle.Secondary
    )
}

@Composable
private fun QuestDetails(state: HomeUiState.Content, onNavigate: (FinEduRoute) -> Unit) {
    Title(stringResource(R.string.home_quest_title))
    val quest = state.quest
    if (quest == null) {
        Text(text = stringResource(R.string.home_quests_done), style = MaterialTheme.typography.bodyMedium)
        FinButton(
            text = stringResource(R.string.home_to_quests),
            onClick = { onNavigate(FinEduRoute.Quests) },
            style = FinButtonStyle.Secondary
        )
    } else {
        Text(text = quest.title, style = MaterialTheme.typography.bodyLarge)
        FinButton(
            text = stringResource(R.string.home_quest_start),
            onClick = { onNavigate(FinEduRoute.Quest(quest.id)) }
        )
    }
}

@Composable
private fun FinishWeekDetails(state: HomeUiState.Content, onNavigate: (FinEduRoute) -> Unit) {
    Title(stringResource(R.string.home_week_results))
    Text(text = stringResource(R.string.home_week_results_hint), style = MaterialTheme.typography.bodyMedium)
    FinButton(
        text = stringResource(R.string.home_finish_week),
        onClick = { onNavigate(FinEduRoute.PeriodSummary) },
        modifier = if (state.isYounger) Modifier.heightIn(min = YoungerButtonHeight) else Modifier
    )
}

@Composable
private fun Title(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.semantics { heading() }
    )
}

@Composable
private fun goalText(goal: HomeGoal, isYounger: Boolean): String = if (isYounger) {
    stringResource(R.string.home_goal_younger, goal.title)
} else {
    stringResource(R.string.home_goal_progress, goal.title, goal.saved.amount, goal.price.amount)
}

@Composable
private fun goalWeeksText(weeksLeft: Int?): String = when (weeksLeft) {
    null -> stringResource(R.string.home_goal_weeks_unknown)
    0 -> stringResource(R.string.home_goal_reached)
    else -> pluralStringResource(R.plurals.home_goal_weeks, weeksLeft, weeksLeft)
}

private val HomeGoal.progress: Float
    get() = (saved.amount.toFloat() / price.amount).coerceIn(0f, 1f)

private fun HomeUiState.Content.amountIn(bag: Bag): Dzynki = when (bag) {
    Bag.NEEDS -> needsLeft
    Bag.WANTS -> wantsLeft
    Bag.SAVINGS -> savings
}

private val PetStat.hintRes: Int
    get() = when (this) {
        PetStat.CHARGE -> R.string.home_stat_hint_charge
        PetStat.VIBE -> R.string.home_stat_hint_vibe
        PetStat.CALM -> R.string.home_stat_hint_calm
    }

internal val PetStat.lowNoticeRes: Int
    get() = when (this) {
        PetStat.CHARGE -> R.string.home_notice_low_charge
        PetStat.VIBE -> R.string.home_notice_low_vibe
        PetStat.CALM -> R.string.home_notice_low_calm
    }

private const val FULL_TURN = 360f
private const val RING_START = -90f
private val RingInset = 6.dp
private val RingWidth = 3.dp
private val YoungerButtonHeight = 72.dp
