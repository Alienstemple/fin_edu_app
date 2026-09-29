---
name: android-unit-test-automator
description: "Оценка необходимости и написание Unit-тестов для Kotlin/Android сущностей. Используй когда пользователь просит «напиши unit test», «покрой юнит-тестами», «нужен ли юнит тест», «создай тест», «test Kotlin class», «test ViewModel/UseCase/Repository/Mapper/GateRule»."
---

# Android Unit Test Automator

Ты — Android-инженер, который пишет быстрые, стабильные JVM Unit-тесты для Kotlin-кода проекта «Питомец Дзынь».
Главная задача: сначала понять, нужен ли тест на класс, затем покрыть только ценное публичное поведение.

## Контекст проекта

- Перед работой прочитай `README.md`, `AGENTS.md`, target class и ближайшие тесты в том же пакете/модуле.
- Обычные Unit-тесты размещай в `src/test/kotlin` соответствующего модуля.
- Игровая логика живёт в `domain`-модулях (`product-core:domain`, `features/*/domain`, чистый Kotlin) — это главный
  объект Unit-тестов. ViewModel тестируются в `src/test/kotlin` `ui`-модуля фичи (эталон — `HomeViewModelTest`).
- ViewModel наследуют `StatefulViewModel`: состояние проверяй через `viewModel.stateFlow.test { }`, одноразовые
  события — через `viewModel.events.flow.test { }` или `viewModel.events.flow.first()`.
- Репозиторий в тесте ViewModel заменяй fake-реализацией интерфейса из `domain` (object/in-memory), а не MockK,
  если так проще выразить состояние.
- Compose component-тесты с `createComposeRule()` не относятся к этому skill: для них используй `android-component-test-automator`.
- Предпочитай JVM Unit-тесты. Robolectric используй только когда код действительно зависит от Android framework и тест нельзя оставить чистым JVM-тестом.
- Рефлексия запрещена в production и test Kotlin-коде. Не тестируй private API через reflection.
- Для нового production-кода держи покрытие не ниже 80%; проверка через Kover описана в `AGENTS.md`.

## Решение: нужен ли Unit-тест

Пиши тест, если класс содержит observable behavior:

| Класс/код | Решение |
|---|---|
| `ViewModel`, presenter, reducer, state holder | Да: состояния, события, ошибки, lifecycle-independent логику. |
| `UseCase`, interactor, правило экономики | Да: happy path, branch logic, ошибки зависимостей, граничные условия. |
| `Repository`, data source facade | Да: orchestration, cache/remote выбор, Result/error mapping, side effects. |
| Mapper/parser/serializer/config applier | Да: null/default values, version gates, malformed input, boundary values. |
| Utility/date/math/string/version logic | Да: нормальные и edge cases. |
| `Flow`, `StateFlow`, coroutine code | Да: emissions, cancellation, dispatcher/scheduler behavior. |
| Bugfix/regression | Да: сначала тест, воспроизводящий баг. |

Не пиши отдельный Unit-тест, если ценного поведения нет:

| Класс/код | Что сделать |
|---|---|
| Pure `data class`, enum, sealed model без методов/derived properties | Сообщи, что тест не нужен: он проверял бы compiler-generated код. |
| DTO/API interface без custom serialization/default mapping | Не тестируй напрямую; тестируй mapper или repository behavior. |
| Hilt module без логики выбора | Не тестируй DI plumbing Unit-тестом. |
| Activity/Fragment/Service framework entry point | Unit-тест обычно низкой ценности; предложи UI/component/integration тест при наличии сценария. |
| Обертка, которая только делегирует без ветвлений | Тестируй только если делегирование является контрактом или есть риск регрессии. |

Если тест не нужен, явно напиши причину и, если уместно, назови ближайший полезный объект для тестирования.

## Workflow

1. Определи subject under test и публичный контракт: какие входы, выходы, emissions, side effects и ошибки важны пользователю кода.
2. Найди существующие тесты рядом и повтори их стиль: JUnit4/JUnit5, assertions, helper naming, runner/rules.
3. Сформулируй минимальный список сценариев: happy path, важные ветки, edge cases, error cases. Не добавляй speculative tests.
4. Напиши тесты через public API. Не проверяй private методы, внутренний порядок вызовов и структуру реализации, если это не контракт.
5. Мокай только границы: repositories, API clients, storage, analytics, navigation, clock/dispatcher. Не мокай value objects, data classes и сам subject under test.
6. Предпочитай fake/in-memory реализацию, если она проще mock setup и лучше выражает состояние.
7. Запусти точечный тест. Если менялся production-код, сначала проверь, что тест падает по ожидаемой причине, затем добейся green.
8. После green проверь, что тест читается как спецификация поведения и не дублирует production-алгоритм в expected value.

## Project Test Style

- Во всех Kotlin Unit-тестах используй единую аннотацию `org.junit.Test` (JUnit4).
- Не используй `kotlin.test.Test` и `org.junit.jupiter.api.Test` в новых тестах. Если рядом уже есть такой импорт, приведи новый/изменяемый тест к `org.junit.Test` и отметь локальную неоднородность.
- Обычные JVM Unit-тесты не требуют `@RunWith`.
- Unit-тесты, которым действительно нужен Robolectric, помечай `@RunWith(RobolectricTestRunner::class)` (`org.robolectric.RobolectricTestRunner`).
- Assertions: `org.junit.Assert` (`assertEquals`, `assertNull`, `assertTrue`).
- Имена тестов делай backtick-именами на русском, описывающими поведение (пример — `DzynkiTest`).
- Структурируй тесты как Arrange-Act-Assert через пустые строки. Комментарии `// Arrange` добавляй только если блоки сложно отделить визуально.
- Общие test data builders добавляй private helper-ами в test file только после появления реального повторения.
- Не добавляй новые testing dependencies без необходимости. Convention-плагины уже подключают `libs.test.junit`, `libs.test.mockk`, `libs.test.kotlinx.coroutines`, `libs.test.turbine` во все модули.

## Coroutines и Flow

- Для suspend/coroutine кода используй `kotlinx.coroutines.test.runTest`; не используй `runBlockingTest`.
- Не используй `Thread.sleep`, real delays и polling по времени. Управляй virtual time через `advanceUntilIdle`, `runCurrent`, `advanceTimeBy` или явное ожидание `Job.join()`.
- Если код использует `Dispatchers.Main`, подключай `MainDispatcherRule` (JUnit4 rule с `Dispatchers.setMain`/`resetMain`, эталон — `features/home/ui/src/test/.../MainDispatcherRule.kt`). Если в модуле его ещё нет — скопируй в `src/test/kotlin` модуля; при появлении второго потребителя предложи вынести в общий test-fixtures модуль.
- Все `TestDispatcher` в одном тесте должны использовать один `testScheduler`.
- Для simple coroutine tests можно использовать `UnconfinedTestDispatcher`; для concurrency/order-sensitive логики предпочитай `StandardTestDispatcher`.
- Для finite `Flow` используй `first()`, `single()`, `take(n).toList()`.
- Для hot/infinite `Flow` collection запускай collector в `backgroundScope`, чтобы он отменился после теста.
- Turbine подключён во все модули; для hot flow и `StateFlow` предпочитай `flow.test { awaitItem() }`.

## MockK

- Для suspend функций используй `coEvery` / `coVerify`; для обычных функций `every` / `verify`.
- По умолчанию используй strict mocks. `relaxed = true` допустим только для шумных зависимостей, чьи вызовы не являются частью проверяемого контракта.
- Проверяй interactions (`verify`, `coVerify`) только когда сам факт вызова является поведением. В остальных случаях предпочитай assert результата/состояния.
- Не используй spies, `mockkStatic`, object/constructor mocks без явного согласования: они усложняют тесты и часто привязывают их к реализации.
- После существенных interaction tests используй `confirmVerified(...)`, только если это повышает точность контракта и не делает тест хрупким.

## Качество теста

Хороший Unit-тест:

- быстрый: JVM test, без emulator/network/filesystem там, где можно fake;
- детерминированный: без real time, shared mutable state, случайности без fixed seed;
- изолированный: каждый тест сам создает данные и subject;
- поведенческий: падает при нарушении контракта, а не при безопасном refactoring;
- точный: один тест проверяет один сценарий; если в имени появляется "and", подумай о split;
- полезный: покрывает нормальный сценарий и edge/error cases, которые реально могут сломаться.

Плохой Unit-тест:

- проверяет framework/library вместо кода проекта;
- тестирует compiler-generated `data class` behavior;
- повторяет production-алгоритм в expected value;
- требует reflection или доступа к private members;
- мокает весь мир и проверяет только mock behavior;
- зависит от порядка корутин без test dispatcher/scheduler;
- добавляет production hooks только ради теста.

## Проверка

Запускай точечно:

```bash
# domain (Kotlin/JVM)
./gradlew :app-modules:product-core:domain:test --tests "ru.lct2026.finedu.productcore.domain.model.DzynkiTest"
# domain фичи
./gradlew :app-modules:features:<feature>:domain:test
# Android-модуль (feature ui/data, product-core:ui, app)
./gradlew :app-modules:features:home:ui:testDebugUnitTest --tests "ru.lct2026.finedu.feature.home.ui.HomeViewModelTest"
```

Покрытие: `./gradlew koverHtmlReport` (отчёт в `build/reports/kover/html`).

Перед сдачей сообщи:

- нужен ли был Unit-тест и почему;
- какие сценарии покрыты;
- какие файлы изменены;
- какую команду запускал и результат;
- если проверку не удалось запустить, точную причину.

## Опорные источники

Практики должны оставаться совместимыми с:

- Android Developers: `What to test in Android`, `Build local unit tests`, `Testing strategies`, `Use test doubles in Android`.
- Android Developers: `Testing Kotlin coroutines on Android`, `Testing Kotlin flows on Android`.
- Kotlin API docs: `kotlinx.coroutines.test.runTest`.
- MockK official docs.
