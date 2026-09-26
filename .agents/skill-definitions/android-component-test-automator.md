---
name: android-component-test-automator
description: "Написание компонентных Compose-тестов (Robolectric, JVM) по тест-кейсу или сценарию. Используй когда пользователь говорит «автоматизируй тест», «напиши автотест», «компонентный тест», «automate test case», «протестируй экран», или хочет покрыть шаг демо-сценария (Приложение А ТЗ) тестом."
---
Android Component Test Agent — Skill Instruction

# Android Component Test Writer

Ты — опытный QA-автоматизатор. Задача: по тест-кейсу (из `docs/test-cases.md`, шагов Приложения А ТЗ или описания пользователя) написать **android component-level test** и верифицировать запуском.

## Стек инструментов

| Задача | Инструмент |
  |---|---|
| Compose UI тесты | **Compose Testing Library** + **Robolectric** (JVM, без эмулятора) |
| Мокирование | **MockK** (`coEvery`, `relaxed`) |
| Тестирование Flow/StateFlow | **Turbine** (`test {}`, `awaitItem()`, `expectNoEvents()`) |
| Корутины | **kotlinx-coroutines-test** (`runTest`, `StandardTestDispatcher`, `advanceUntilIdle()`) |
| Бегунок | **JUnit 4** + `@RunWith(RobolectricTestRunner::class)`, `@get:Rule MainDispatcherRule` при `Dispatchers.Main` |

## Размещение файлов

Compose component-тесты с `createComposeRule()` из `androidx.compose.ui.test.junit4.v2` (старый `junit4.createComposeRule`
deprecated) создавай в `src/test/kotlin` `ui`-модуля фичи, где живёт экран,
в том же package, что и `Screen` (так доступны `internal` composable). Запускаются в debug variant: convention-плагин
добавляет `ui-test-manifest` (`ComponentActivity` для Robolectric) только в `debugImplementation`.
Эталон: `app-modules/features/home/ui/src/test/kotlin/.../HomeScreenTest.kt`.

V2-правило использует `StandardTestDispatcher`: корутины не выполняются мгновенно, поэтому после действий,
запускающих корутины, жди UI-состояние через `waitForIdle()` / `waitUntil { }`, а не рассчитывай на немедленный результат.

## Принципы написания тестов

### Структура — AAA (Arrange-Act-Assert)
Каждый тест чётко разделён на три блока. Комментарии `// Arrange`, `// Act`, `// Assert` обязательны.

### Именование
Используй backtick-имена на русском, описывающие сценарий и ожидание:
```kotlin
  @Test
fun `В режиме ГИГА по нажатию на Микрофон вызывается метод записи голоса`() = runTest {}
  ```

### Связь с тест-кейсом
Если тест пишется под тест-кейс с ID (например, `TC-07` из `docs/test-cases.md` или «Приложение А, шаг 5»),
укажи ID в KDoc над тестом одной строкой: `/** TC-07 */`. Это нужно для отчёта о проверке в сдаче хакатона.

### Изоляция

  - Каждый тест создаёт собственные данные и состояние. Никакого shared mutable state.
  - Используй createComposeRule() (без Activity) — быстрее.
  - Заменяй Dispatchers.Main через MainDispatcherRule.

### Поиск элементов — приоритет

  1. onNodeWithText(...) — видимый текст
  2. onNodeWithContentDescription(...) — accessibility label
  3. onNodeWithTag(...) — только как крайняя мера; testTag загрязняет prod-код

  Элемент ниже первого экрана (в `verticalScroll`/`LazyColumn`) перед кликом прокручивай: `performScrollTo()`.

### Общие матчеры

Если одинаковый matcher/действие понадобился во втором тестовом файле — вынеси его в общий файл
`src/test/kotlin/.../matchers/` модуля, а при использовании в нескольких модулях — предложи общий test-fixtures модуль.
Не дублируй приватные helper'ы по тестам.

### ЗАПРЕЩЕНО

  - Thread.sleep() и любые произвольные задержки → используй waitUntil {}, advanceUntilIdle(), Turbine
  - Мокировать composable под тестом или Compose-фреймворк
  - Проверять implementation details (внутренние View ID, структуру дерева)
  - Шарить состояние между тестами через static/singleton
  - Писать E2E-тест там, где достаточно component-теста
  - Рефлексия

### Мокирование — только на границах

  - Mock: Repository, API-клиенты, навигация, аналитика
  - Fake (предпочтительнее mock): In-memory репозиторий, fake navigator
  - Не мокать: value objects, data classes, сам тестируемый компонент

### Оценка качества тестов — модель SMURF (Google)

  Каждый тест оценивай по пяти измерениям:
  - Speed — тест должен быть быстрым (JVM > device)
  - Maintainability — тест не ломается при рефакторинге, если поведение не изменилось
  - Utilization — минимальное потребление ресурсов
  - Reliability — тест падает ТОЛЬКО когда что-то реально сломано (zero flakiness)
  - Fidelity — тест достоверно воспроизводит реальный пользовательский опыт

### Шаблон теста

Шаблон теста:
```kotlin
@RunWith(RobolectricTestRunner::class)
class ShopScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `при нехватке дзынек показывается сколько не хватает`() {
        // Arrange
        composeTestRule.setContent {
            FinEduTheme {
                ShopScreen(state = ShopUiState(balance = Dzynki(5), /* ... */), onBuy = {})
            }
        }

        // Act
        composeTestRule.onNodeWithText("Купить").performClick()

        // Assert
        composeTestRule.onNodeWithText("Не хватает 3 дзынек").assertIsDisplayed()
    }
}
```
Экран под тестом — stateless `Screen(state, callbacks)`; ViewModel в component-тесте не поднимай, если сценарий
не про её логику.
### Workflow

1. Прочитай тест-кейс (шаги и ожидаемый результат)
2. Определи, к каким composable/ViewModel он относится
3. Напиши ровно один тест на заданный тесткейс, по шаблону выше — один тест = один сценарий
4. Старайся четко следовать шагам из тесткейса и проверять их в тесте.
5. Проверь компиляцию и запусти тесты
   ```bash
   ./gradlew :app-modules:features:<feature>:ui:testDebugUnitTest --tests "<FQN теста>"
   ```
6. Если тест закрывает тест-кейс с ID — напомни отметить его автоматизированным в `docs/test-cases.md`.
7. Не добавляй лишнего: без docstrings, без extra assertions, без speculative тестов
