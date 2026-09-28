package ru.lct2026.finedu.feature.parent.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.parent.ui.R
import ru.lct2026.finedu.productcore.domain.model.ScamScheme
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Вкладка «Мошенники»: памятка-чеклист схем и вопросы для разговора дома. */
@Composable
internal fun ScamsTab(
    schemes: List<ScamScheme>,
    discussedIds: Set<String>,
    talkQuestions: List<String>,
    onSchemeToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ParentCard {
            SectionTitle(stringResource(R.string.parent_scams_title))
            SecondaryText(stringResource(R.string.parent_scams_lead))
            schemes.forEach { scheme ->
                SchemeRow(
                    scheme = scheme,
                    isDiscussed = scheme.id in discussedIds,
                    onToggle = { onSchemeToggle(scheme.id) }
                )
            }
        }
        ParentCard {
            SectionTitle(stringResource(R.string.parent_scams_talk_title))
            SecondaryText(stringResource(R.string.parent_scams_talk_lead))
            talkQuestions.forEachIndexed { index, question -> NumberedLine(number = index + 1, text = question) }
        }
        Text(text = stringResource(R.string.parent_scams_rule), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SchemeRow(scheme: ScamScheme, isDiscussed: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = isDiscussed, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val colors = FinEduTheme.colors
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(2.dp, if (isDiscussed) colors.savings else MaterialTheme.colorScheme.outline, CheckShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDiscussed) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_check),
                    contentDescription = null,
                    tint = colors.savings,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = scheme.title, style = MaterialTheme.typography.titleMedium)
            Text(text = scheme.text, style = MaterialTheme.typography.bodyMedium)
            if (isDiscussed) {
                Text(
                    text = stringResource(R.string.parent_scams_discussed),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.savings
                )
            }
        }
    }
}

private val CheckShape = RoundedCornerShape(8.dp)

internal val PreviewSchemes = listOf(
    ScamScheme("sms", "Коды из СМС", "«Скажи код — и приз твой». Код из СМС — ключ от аккаунта."),
    ScamScheme("prize", "«Ты выиграл!»", "Выигрыш в конкурсе, где ребёнок не участвовал, — приманка.")
)

@Preview(heightDp = 900)
@Composable
private fun ScamsTabPreview() {
    FinEduPreview {
        ScamsTab(
            schemes = PreviewSchemes,
            discussedIds = setOf("sms"),
            talkQuestions = listOf("«Если кто-то пишет, что ты что-то выиграл, — что сделаешь первым делом?»"),
            onSchemeToggle = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}
