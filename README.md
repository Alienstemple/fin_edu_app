# Питомец Финни (fin_edu_app)

Android-приложение по финансовой грамотности для детей 7–11 лет: виртуальный питомец Дзынь, игровая валюта
«дзыньки», бюджет, покупки, накопления на цель. Хакатон ЛЦТ 2026, задача Департамента финансов Москвы.

## Требования окружения

- JDK 17+ (проверено на 21).
- Android SDK с платформой 36 (compileSdk). Если платформы нет, Gradle скачает её сам при первой сборке.
- Версии всех зависимостей и плагинов — в `gradle/libs.versions.toml`.

## Быстрый старт

1. Откройте проект в Android Studio из корня репозитория.
2. Дождитесь Gradle Sync (используйте `./gradlew`, версия Gradle зафиксирована в wrapper).
3. Запуск debug-сборки:
    - Через Android Studio (конфигурация `app`).
    - Или из терминала:
      ```bash
      ./gradlew :app:installDebug
      ```

## AI agents

Настройка Claude Code и Codex описана в [docs/agents.md](docs/agents.md).
Общие инструкции для агентов лежат в [AGENTS.md](AGENTS.md), canonical skills — в `.agents/skill-definitions`.

## Архитектура

Многомодульный проект: Kotlin, Jetpack Compose, Hilt, Navigation Compose (type-safe маршруты).
Каждая фича разбита на три Gradle-модуля `domain` / `data` / `ui`. Цель — подготовить перевод на Kotlin
Multiplatform: `domain` уже чистый Kotlin и переносится в `commonMain` без изменений, `data` опирается на
библиотеки с KMP-версиями (Room, DataStore), а платформенным остаётся только `ui`.

```text
app/                              — точка входа: Application, MainActivity, NavHost; собирает ui + data всех фич
app-modules/
    product-core/                 — общее ядро
        domain/                   — общие модели и игровая экономика (Dzynki, ...), чистый Kotlin/JVM
        navigation/api/           — FinEduRoute и контракт FeatureNavigationContribution
        ui/                       — FinEduTheme, общие компоненты (FinButton, ...), превью
    features/<feature>/
        domain/                   — модели фичи, интерфейсы репозиториев, use case'ы. Чистый Kotlin
        data/                     — реализации репозиториев, Room/DataStore/assets, Hilt-привязки
        ui/                       — Compose-экраны, ViewModel, UiState, регистрация в навигации
build-logic/                      — convention-плагины Gradle
config/detekt/                    — конфиг detekt
```

Фичи (номера экранов — из `decomposition.md`):

| Фича         | Экраны                         |
|--------------|--------------------------------|
| `onboarding` | 1. Онбординг / Помощь          |
| `hero`       | 2. Создание героя              |
| `home`       | 3. Главный                     |
| `budget`     | 4. План бюджета                |
| `shop`       | 5. Магазин                     |
| `savings`    | 6. Копилка                     |
| `quests`     | 7. Задания, 8. Задание         |
| `period`     | 9. Итоги периода               |
| `parent`     | 10. Раздел для взрослого       |

Эталон полного потока `domain → data → ui` — фича `home` (`HomeRepository` → `HomeRepositoryImpl` →
`HomeViewModel`). Остальные фичи пока заглушки: только `ui`, `domain` и `data` пустые.

### Граф зависимостей

```text
            app
          /     \
   feature:ui   feature:data
          \     /
       feature:domain
             |
    product-core:domain
```

- `ui` → `domain` своей фичи, `product-core:ui`, `product-core:navigation:api`.
- `data` → `domain` своей фичи.
- `domain` → `product-core:domain` (реэкспортируется через `api`), `kotlinx-coroutines-core`.
- `app` подключает `ui` и `data` каждой фичи и связывает их в одном Hilt-графе.
- `ui` **не видит** `data` на уровне Gradle — случайный импорт реализации не скомпилируется.
- Фичи не зависят друг от друга. Переходы — через `FinEduRoute`. Общие модели двух фич переезжают
  в `product-core:domain`.

Навигация: каждый `ui`-модуль реализует `FeatureNavigationContribution` и кладёт его в Hilt-сет
(`@Binds @IntoSet`). `MainActivity` получает весь сет и строит один `NavHost`.

Модуль `*/api` (как `navigation/api`) содержит только публичный контракт: интерфейсы, модели и value-классы для
потребителей, без реализаций, Hilt-модулей и инфраструктурного кода.

### Convention-плагины

| Плагин                               | Для чего                                                                 |
|--------------------------------------|--------------------------------------------------------------------------|
| `convention.root-project`            | Корень: detekt и spotless во всех модулях, агрегированный отчёт Kover    |
| `convention.android-application`     | `:app`: SDK, Compose, BuildConfig                                        |
| `convention.kotlin-feature-domain`   | `features/*/domain`: чистый Kotlin + `product-core:domain` + coroutines  |
| `convention.android-feature-data`    | `features/*/data`: Android library + Hilt, без Compose                   |
| `convention.android-feature-ui`      | `features/*/ui`: Compose + Hilt + product-core, JVM/Robolectric тесты    |
| `convention.android-feature-api`     | `*/api`: Android library без Compose и Hilt                              |
| `convention.android-library-compose` | Общие UI-модули (`product-core:ui`)                                      |
| `convention.kotlin-jvm`              | Чистый Kotlin (`product-core:domain`)                                    |
| `convention.hilt`                    | KSP + Hilt                                                               |

### Структура пакетов внутри модуля

Kotlin package повторяет путь модуля: `ru.lct2026.finedu.feature.<feature>.<domain|data|ui>`.
Не создавайте пустой пакет, если он в конкретной фиче не нужен.

```text
feature/<feature>/domain  →  ru.lct2026.finedu.feature.<feature>.domain/
                                 model/          — доменные модели фичи
                                 repository/     — интерфейсы репозиториев
                                 usecase/        — use case'ы (только если есть логика сверх проброса)

feature/<feature>/data    →  ru.lct2026.finedu.feature.<feature>.data/
                                 repository/     — *RepositoryImpl
                                 local/          — Room entity/DAO, DataStore
                                 content/        — JSON DTO и загрузка из assets
                                 mapper/         — entity/DTO ↔ domain
                                 di/             — @Binds репозиториев

feature/<feature>/ui      →  ru.lct2026.finedu.feature.<feature>.ui/
                                 <Name>Screen.kt, <Name>ViewModel.kt, <Name>UiState.kt,
                                 <Feature>NavigationContribution.kt
                                 component/      — composable-компоненты экрана, если их много
                                 di/             — @Binds @IntoSet навигации, @Provides use case'ов
```

Назначение модулей:

- `domain` — бизнес-логика и контракты фичи: domain models, repository interfaces, use case'ы, правила игры.
  Без Android SDK, Compose, Room и **без DI-аннотаций** (`@Inject`, `@Module`) — это условие переноса в
  `commonMain`. Use case'ы создаются через `@Provides` в `ui/di`. Общая игровая экономика — в `product-core:domain`.
- `data` — источники данных: `RepositoryImpl`, Room, DataStore, загрузка JSON-контента из assets, мапперы.
  Реализации `internal`, наружу видны только через интерфейсы `domain` и Hilt-привязки.
- `ui` — `Screen` (stateless), `Route` (достаёт ViewModel), `ViewModel`, UI state/event, `NavigationContribution`.
  Работает только с `domain`.

#### Строгие правила изоляции слоёв

Нарушение этих правил — **архитектурная ошибка**, не подлежащая review approval. Большую часть правил уже
гарантирует граф Gradle-зависимостей; не обходите его.

**Entity/DTO не выходят за пределы `data`.**
Room entity, JSON DTO (`*Entity`, `*Dto`, `*Json`) не появляются в `domain`, `ui` или в сигнатурах интерфейсов
репозиториев.

**`domain` не зависит ни от `data`, ни от `ui`, ни от Android.**

**`ui` не зависит от `data`.** Не добавляйте `implementation(projects...data)` в `ui`-модуль.

**Маппинг entity/DTO ↔ domain — исключительно в `data`.**

**UI-модели не уходят в `domain` или `data`.**
Типы только для отображения (`*UiState`, `*UiModel`, `*Item`) живут исключительно в `ui`.

Тесты зеркалируют production package своего модуля.

## ViewModel и состояние экрана

Базовые классы — в `product-core:ui` (`ru.lct2026.finedu.productcore.ui.viewmodel` / `.event`):

| Класс                         | Назначение                                                                      |
|-------------------------------|---------------------------------------------------------------------------------|
| `StatefulViewModel<S>`        | Базовая ViewModel экрана: состояние `S` + одноразовые события                    |
| `StatelessViewModel`          | ViewModel без состояния, только события                                          |
| `StateHolder<S>`              | `stateFlow`, `currentState`, `updateState { }`, `setState()`                     |
| `Event`, `EventQueue`         | Одноразовые события: копятся, пока экран не подписан, доставляются один раз      |
| `ObserveEvents(...)`          | Подписка экрана на события с учётом lifecycle                                    |
| `CloseScreenEvent`            | Стандартное событие «закрыть экран»                                              |

Правила:

- Каждый экран с логикой — `XxxViewModel : StatefulViewModel<XxxUiState>`. Состояние меняется только через
  `updateState { copy(...) }` (атомарно от текущего) или `setState(...)`; `MutableStateFlow` в фичах не заводим.
- `XxxUiState` — `@Immutable` `data class` или `sealed interface`; ветвление по нему — exhaustive `when`.
- Действия пользователя — публичные методы `onXxxClick()` / `onXxxChanged(value)`; экран передаёт их ссылками.
- Одноразовые эффекты (навигация, закрытие, сообщение) — `offerEvent(...)`, не поле в состоянии.
  События фичи — `data class`/`data object`, реализующие `Event`, в `ui`-модуле фичи.
- Экран делится на `XxxRoute` (достаёт ViewModel через `hiltViewModel()`, собирает `stateFlow`, вызывает
  `ObserveEvents`) и stateless `XxxScreen(state, callbacks)` — его показывают превью и component-тесты.

```kotlin
@HiltViewModel
internal class ShopViewModel @Inject constructor(
    private val buyItem: BuyItemUseCase,
) : StatefulViewModel<ShopUiState>(ShopUiState()) {

    fun onBuyClick(itemId: String) {
        viewModelScope.launch {
            when (val result = buyItem(itemId)) {
                is PurchaseResult.Success -> updateState { copy(balance = result.balance) }
                is PurchaseResult.NotEnoughMoney -> offerEvent(ShopEvent.ShowShortage(result.missing))
            }
        }
    }
}

@Composable
internal fun ShopRoute(onBack: () -> Unit, viewModel: ShopViewModel = hiltViewModel()) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            CloseScreenEvent -> onBack()
            is ShopEvent.ShowShortage -> { /* показать подсказку */ }
        }
    }
    ShopScreen(state = state, onBuyClick = viewModel::onBuyClick)
}
```

Эталон в коде — `features/home/ui` (`HomeViewModel`, `HomeRoute`, `HomeScreen`).

## Coding Standards

- Соблюдайте четырехпробельные отступы, максимальная длина строки — 120.
- `UpperCamelCase` для классов, `lowerCamelCase` для функций.
- Именуйте `res/` с префиксами `ic_`, `bg_` и другими стандартными паттернами Android.
- Изображения: предпочтение векторным, для растровых используйте WEBP.
- Соблюдайте правила Detekt, Android Lint и Spotless (ktlint).
- Не используйте Deprecated компоненты в новом коде.
- Для ветвления по `sealed class`/`sealed interface` и `enum` не используйте `as?`/type casts; используйте exhaustive `when`.
- Рефлексия в Kotlin-коде запрещена (и в тестах).
- Все тексты интерфейса — в `strings.xml`, игровой контент — в JSON. Контент отдельно от кода (требование ТЗ).

## Compose

- если `@Composable` функция создает и возвращает объект – она ничего не добавляет в композицию
- если `@Composable` функция добавляет что-то в композицию – она ничего не возвращает (внутри метода не используется `return`)
- приложение только в тёмной теме; шрифт — Commissioner
- цвета, шрифты и радиусы — только из `FinEduTheme` (`MaterialTheme.colorScheme` / `.typography` / `.shapes`);
  токены макета, которых нет в Material (мешочки, золото, стекло), — `FinEduTheme.colors`
- фон экранов — `FinEduBackground` (подключён в `MainActivity`), экраны рисуются с прозрачным контейнером;
  карточки — `Modifier.glass()`
- превью — `@Preview` внутри `FinEduPreview { }`

## Доступность

Требования ТЗ для детей 7–11 лет:

- основной текст не меньше 16sp, поддержка крупного системного шрифта;
- зона нажатия не меньше 48dp (`MinTouchTarget`, `FinButton`);
- смысл не передаётся только цветом — добавляйте иконку или подпись;
- заголовки экранов помечены `semantics { heading() }`, значимые иконки имеют `contentDescription`.

## Проверки

```bash
./gradlew spotlessApply
./gradlew detekt
./gradlew lintVitalRelease
./gradlew test
./gradlew koverHtmlReport        # покрытие: build/reports/kover/html/index.html
```

## Тесты

- Unit-тесты: `src/test/kotlin` модуля (логика — в `domain`, ViewModel — в `ui`), JUnit4 (`org.junit.Test`), MockK, kotlinx-coroutines-test, Turbine.
- Compose component-тесты с `createComposeRule()` (`androidx.compose.ui.test.junit4.v2`): `src/test/kotlin`
  `ui`-модуля фичи, `@RunWith(RobolectricTestRunner::class)`. Запускаются в debug variant: `ui-test-manifest` подключается только
  в `debugImplementation`. Не подключайте его в `releaseImplementation`.
- E2E UI-тесты: `app/src/androidTest/kotlin`, нужен эмулятор или устройство:
  ```bash
  ./gradlew :app:connectedDebugAndroidTest
  ```
- Тест-кейсы: [docs/test-cases.md](docs/test-cases.md).

## Git Workflow

- Перед PR запускайте `spotlessApply` (не забудьте добавить автоформатированные файлы к PR), `detekt`, `lintVitalRelease`:
  ```shell
  ./gradlew spotlessApply && ./gradlew detekt && ./gradlew lintVitalRelease
  ```
- Branch naming: `feature/<short_context>`, `fix/<short_context>`.
- Для PR используйте подход `1 PR = 1 commit`: перед публикацией или после review-fix объединяйте связанные
  изменения в один самодостаточный commit.
- Commit message пишется на русском в формате:
  ```text
  <краткое описание изменения>

  # Что сделано
  <чуть подробнее: что изменилось и зачем>

  # Как сделано
  <чуть подробнее: ключевой подход, механизм или важные технические нюансы>
  ```
  Первая строка должна быть короткой и самодостаточной. Если commit message набирается через editor и Git скрывает
  строки с `#` как комментарии, используйте `git commit --cleanup=whitespace`.
- Target branch: `main`.

## Релизная сборка

1. Сгенерируйте ключ (один раз, хранить вне репозитория):
   ```bash
   keytool -genkeypair -v -keystore ../keys/fin_edu_release.jks -alias fin_edu \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Скопируйте `keystore.properties.example` в `keystore.properties` и заполните. Файл в `.gitignore`.
3. Соберите подписанный APK:
   ```bash
   ./gradlew :app:assembleRelease
   ```
   Без `keystore.properties` собирается неподписанный `app-release-unsigned.apk`.

Версия приложения: `versionCode` / `versionName` в `app/build.gradle.kts`.
