package ru.lct2026.finedu.app.e2e

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import ru.lct2026.finedu.app.MainActivity

/**
 * Сквозной сценарий Приложения А ТЗ (шаги 1–12) в демо-режиме. Один тест — один сценарий: шаги зависят друг от
 * друга, как на живой демонстрации. Тест начинает с чистого профиля и заканчивает удалением профиля (шаг 12),
 * поэтому его можно запускать повторно.
 */
@RunWith(AndroidJUnit4::class)
class DemoScenarioTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    /** Профиль удаляется до запуска Activity: приложение стартует с онбординга. */
    private val cleanProfile = object : ExternalResource() {
        override fun before() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            File(context.filesDir, "datastore/game.json").delete()
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(cleanProfile).around(composeRule)

    /** TC-01…TC-12. Приложение А, шаги 1–12: полный игровой цикл, перезапуск и раздел для взрослого. */
    @Test
    fun demoScenario() {
        firstLaunch()
        createProfile()
        distributeBudget()
        completeQuest()
        buyAndGetFeedback()
        saveForGoal()
        nextPeriod()
        restart()
        deleteProfileInParentZone()
    }

    /** 1. Первый запуск: знакомство с целью игры и тремя типами решений. */
    private fun firstLaunch() {
        click("Взять на руки")
        click("Дальше")
        click("Дальше")
        waitForText("Три мешочка")
        click("Дальше")
        click("Погнали")
        click("Каша для Дзыня")
        waitForText("Твой выбор: каша")
        click("Завести своего Дзыня")
    }

    /** 2–4. Локальный профиль без реальных данных, внешний вид питомца, демо-режим, стартовый бюджет. */
    private fun createProfile() {
        waitForText("Настоящие имена тут не нужны")
        click("Мята")
        click("Кепка")
        click("Демо-режим для проверки", scroll = true)
        waitForText("Неделя 1 из 5")
        waitForText("Демо-режим")
    }

    /** 5. Распределение по мешочкам (раскладка по умолчанию 150 / 120 / 30) и засчитанное задание. */
    private fun distributeBudget() {
        openBubble("Баланс")
        click("Разложить 300 дзынек")
        waitForText("Осталось разложить: 0 из 300")
        click("Зафиксировать план")
        waitForText("+20 за задание «Три мешочка — один план»")
        click("Зафиксировать план")
        waitForText("План зафиксирован")
        click("На главную")
    }

    /** 6. Задание «Это мошенники?» и награда в дзыньках. */
    private fun completeQuest() {
        tab("Задания")
        click("Это мошенники?", scroll = true)
        repeat(SCAM_MESSAGES - 1) {
            click("Подозрительно")
            click("Следующее сообщение")
        }
        click("Подозрительно")
        click("К итогу")
        waitForText("+30 дзынек", substring = true)
        click("Понятно")
    }

    /** 7, 9. Покупка из «Хочу», попытка при нехватке, покупка из «Нужного» — с обратной связью. */
    private fun buyAndGetFeedback() {
        tab("Магазин")
        click("Шляпа с пером")
        click("Купить")
        waitForText("Я так этого ждал")
        click("Ещё в магазин")
        click("Гирлянда")
        waitForText("Не хватает", substring = true)
        click("Выбрать дешевле")
        click("Нужное")
        click("Каша")
        click("Купить")
        waitForText("Мне тепло, спасибо")
        waitForText("Что изменилось")
        click("Вернуться в уголок")
    }

    /** 8. Цель и пополнение копилки. */
    private fun saveForGoal() {
        tab("Копилка")
        waitForText("Цель — Плед")
        click("Пополнить")
        click("Положить 10")
        waitForText("Я слышу, как в копилке звенит")
        click("Вернуться в уголок")
    }

    /** 10. Следующий период: итоги недели и опыт роста. */
    private fun nextPeriod() {
        openBubble("Завершить неделю")
        click("Завершить неделю")
        waitForText("Итоги недели")
        waitForText("Опыт за неделю", scroll = true)
        click("Следующая неделя")
        waitForText("Неделя 2 из 5")
    }

    /** 11. Повторный запуск: прогресс читается из хранилища. */
    private fun restart() {
        composeRule.activityRule.scenario.recreate()
        waitForText("Неделя 2 из 5")
    }

    /** 12. Раздел для взрослого: барьер и удаление тестового профиля. */
    private fun deleteProfileInParentZone() {
        composeRule.onNode(hasContentDescription("Для взрослых") and hasClickAction()).performClick()
        solveBarrier()
        click("Настройки")
        click("Удалить профиль", scroll = true)
        click("Удалить")
        waitForText("Взять на руки")
    }

    /** Барьер «a × b = ?»: ответ набирается на цифровой клавиатуре раздела. */
    private fun solveBarrier() {
        waitForText(" × ", substring = true)
        val example = composeRule.onAllNodes(hasText(" × ", substring = true)).fetchSemanticsNodes().first()
            .config[SemanticsProperties.Text].joinToString("")
        val (a, b) = example.substringBefore("=").split("×").map { it.trim().toInt() }
        (a * b).toString().forEach { digit ->
            composeRule.onNode(hasText("$digit") and hasClickAction()).performClick()
        }
        click("Готово")
        waitForText("Прогресс")
    }

    /** Вкладка нижней навигации (роль Tab, подпись — у самой вкладки или у её потомка). */
    private fun tab(name: String) {
        val matcher = (hasText(name) or hasAnyDescendant(hasText(name))) and
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        composeRule.waitUntil(TIMEOUT_MS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onAllNodes(matcher)[0].performClick()
        composeRule.waitForIdle()
    }

    /** Пузырёк на главном: подпись — в contentDescription, раскрытие — тапом. */
    private fun openBubble(description: String) {
        val matcher = hasContentDescription(description, substring = true) and hasClickAction()
        composeRule.waitUntil(TIMEOUT_MS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onAllNodes(matcher)[0].performClick()
        composeRule.waitForIdle()
    }

    private fun click(text: String, scroll: Boolean = false) {
        waitForText(text)
        node(text, clickable = true, scroll = scroll).performClick()
        composeRule.waitForIdle()
    }

    private fun waitForText(text: String, substring: Boolean = false, scroll: Boolean = false) {
        composeRule.waitUntil(TIMEOUT_MS) {
            composeRule.onAllNodes(hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty()
        }
        if (scroll) node(text, clickable = false, scroll = true)
    }

    /**
     * Первый подходящий узел, прокрученный в видимую область, если он внутри прокручиваемого контейнера: иначе нажатие
     * уходит за край экрана. [scroll] — для ленивых списков, где узел ещё не создан.
     */
    private fun node(text: String, clickable: Boolean, scroll: Boolean): SemanticsNodeInteraction {
        val matcher = if (clickable) hasText(text) and hasClickAction() else hasText(text)
        if (scroll) {
            val scrollables = composeRule.onAllNodes(hasScrollAction()).fetchSemanticsNodes()
            if (scrollables.isNotEmpty()) composeRule.onAllNodes(hasScrollAction())[0].performScrollToNode(matcher)
        }
        val inScrollable = matcher and hasAnyAncestor(hasScrollAction())
        if (composeRule.onAllNodes(inScrollable).fetchSemanticsNodes().isNotEmpty()) {
            return composeRule.onAllNodes(inScrollable)[0].also { it.performScrollTo() }
        }
        return composeRule.onAllNodes(matcher)[0]
    }

    private companion object {
        const val TIMEOUT_MS = 10_000L
        const val SCAM_MESSAGES = 3
    }
}
