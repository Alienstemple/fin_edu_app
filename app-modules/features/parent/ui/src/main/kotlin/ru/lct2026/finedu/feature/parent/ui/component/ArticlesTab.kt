package ru.lct2026.finedu.feature.parent.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.parent.ui.R
import ru.lct2026.finedu.productcore.domain.model.Article
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Вкладка «Статьи»: список. Статья без текста помечена «Скоро» и не открывается. */
@Composable
internal fun ArticlesTab(articles: List<Article>, onArticleClick: (Article) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = stringResource(R.string.parent_articles_lead), style = MaterialTheme.typography.bodyLarge)
        articles.forEach { article -> ArticleRow(article = article, onClick = { onArticleClick(article) }) }
    }
}

@Composable
private fun ArticleRow(article: Article, onClick: () -> Unit) {
    val isReady = article.paragraphs.isNotEmpty()
    val soonDescription = stringResource(R.string.parent_articles_soon_a11y)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .glass()
            .clickable(enabled = isReady, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { if (!isReady) stateDescription = soonDescription }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = article.rubric,
                style = MaterialTheme.typography.labelLarge,
                color = FinEduTheme.colors.gold
            )
            Text(text = article.title, style = MaterialTheme.typography.titleMedium)
            SecondaryText(pluralStringResource(R.plurals.parent_articles_minutes, article.minutes, article.minutes))
        }
        if (isReady) {
            Icon(painter = painterResource(CoreR.drawable.ic_chevron_right), contentDescription = null)
        } else {
            Text(
                text = stringResource(R.string.parent_articles_soon),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .glass(shape = MaterialTheme.shapes.small, style = GlassStyle.Strong)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

/** Полная статья: абзацы и блок «Попробуйте дома». */
@Composable
internal fun ArticleContent(article: Article, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SecondaryText(
            stringResource(
                R.string.parent_article_meta,
                article.rubric,
                pluralStringResource(R.plurals.parent_articles_minutes, article.minutes, article.minutes)
            )
        )
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )
        article.paragraphs.forEach { Text(text = it, style = MaterialTheme.typography.bodyLarge) }
        if (article.tips.isNotEmpty()) {
            ParentCard {
                SectionTitle(stringResource(R.string.parent_article_tips))
                article.tips.forEachIndexed { index, tip -> NumberedLine(number = index + 1, text = tip) }
            }
        }
        FinButton(
            text = stringResource(R.string.parent_article_to_list),
            onClick = onClose,
            style = FinButtonStyle.Secondary
        )
    }
}

@Composable
internal fun NumberedLine(number: Int, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = FinEduTheme.colors.gold
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

internal val PreviewArticles = listOf(
    Article(
        id = "waiting",
        rubric = "Психология",
        title = "Почему дети не умеют ждать",
        minutes = 6,
        paragraphs = listOf(
            "Спойлер: умеют, просто пока не очень. Ожидание — навык, а не черта характера.",
            "Ждать проще, когда ожидание видно."
        ),
        tips = listOf("Сделайте ожидание видимым.", "Начинайте с коротких целей.")
    ),
    Article("pocket_money", "Практика", "Карманные деньги: сколько и как", 7, emptyList(), emptyList())
)

@Preview
@Composable
private fun ArticlesTabPreview() {
    FinEduPreview {
        ArticlesTab(articles = PreviewArticles, onArticleClick = {}, modifier = Modifier.padding(20.dp))
    }
}

@Preview(heightDp = 1000)
@Composable
private fun ArticleContentPreview() {
    FinEduPreview {
        ArticleContent(article = PreviewArticles.first(), onClose = {}, modifier = Modifier.padding(20.dp))
    }
}
