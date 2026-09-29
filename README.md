# Питомец Финни (fin_edu_app)

Android-приложение по финансовой грамотности для детей 7–11 лет: виртуальный питомец Дзынь, игровая валюта
«дзыньки», бюджет, покупки, накопления на цель. Хакатон ЛЦТ 2026, задача Департамента финансов Москвы.

Каждую неделю ребёнок получает 300 дзынек и раскладывает их по трём мешочкам — «Нужное», «Хочу», «Копилка».
Покупки, пополнение копилки и задания меняют показатели Дзыня (Заряд, Вайб, Спокойствие), а разумные решения за
неделю — растят его: Малыш → Шустрик → Мастер мешочка. Накопленное становится вещами в уголке питомца. Проигрыша
нет: питомец не болеет и не умирает, ошибка не обнуляет прогресс.

Версия **1.0.0**. Работает без интернета, без реальных денег, рекламы, покупок, чатов и сбора персональных данных.

## Состав репозитория

| Путь | Что там |
|---|---|
| `app/` | точка входа: `MainActivity`, навигация, e2e-тест `DemoScenarioTest` |
| `app-modules/product-core/domain` | игровая экономика: `GameEngine`, `SavingsEngine`, `QuestEngine`, модели |
| `app-modules/product-core/data` | профиль в DataStore, игровой контент `assets/content/*.json` |
| `app-modules/product-core/ui` | тема, общие компоненты, рисунки питомца и уголка |
| `app-modules/features/*` | экраны: `onboarding`, `hero`, `home`, `budget`, `shop`, `savings`, `quests`, `period`, `parent`, `learn` |
| `docs/` | план, тест-кейсы, материалы сдачи (`docs/submission`: документация, презентация, видео, RuStore) |
| `docs/licenses/` | лицензии шрифтов |

## Демонстрационный режим

На экране создания питомца — ссылка «Демо-режим для проверки». Профиль помечается как демо (плашка на главном),
неделя закрывается кнопкой «Завершить неделю» в любой момент, без ожидания календарных сроков: пять недель сюжета
проходятся подряд. Все задания доступны сразу.

Сквозной сценарий Приложения А (шаги 1–12) автоматизирован: `./gradlew :app:connectedDebugAndroidTest`
(нужен эмулятор или устройство). Тест-кейсы и результаты проверки — [docs/test-cases.md](docs/test-cases.md).

## Сброс и удаление профиля

Главный → замок «Для взрослых» → пример на умножение → «Настройки»:
- «Сбросить прогресс» — игра вернётся к первой неделе; питомец, имя и настройки остаются;
- «Удалить профиль» — все данные стираются, игра начнётся с онбординга.

Оба действия — с подтверждением. Альтернатива для проверки: «Настройки Android → Приложения → Питомец Финни →
Очистить данные». Данные хранятся только на устройстве (`files/datastore/game.json`).

## Реализованные требования

Все обязательные требования ТЗ 2.5 и минимум контента ТЗ 2.6. Матрица соответствия со ссылками на экраны, модули и
тесты — в сопроводительном документе `docs/submission/`. Коротко:
- онбординг, локальный профиль без персональных данных, питомец 3 × 3 вида, 3 показателя, 3 стадии роста;
- доход с источником, план по трём мешочкам, покупки из «Нужного» и «Хочу», нехватка с тремя выходами;
- копилка, цели, срок накопления, снятие с превью «было → станет»;
- 6 заданий по 3 темам (включая «Это развод?» про мошенников), обратная связь после каждого действия;
- итоги недели «план против факта», рост питомца, прогресс и словарик, шортсы и лента;
- раздел для взрослого за барьером: прогресс, статьи, памятка, бонус, настройки доступности, сброс и удаление;
- сохранение прогресса, демо-режим, только тёмная тема, крупный шрифт, спокойный режим, режим 7–8 лет.

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
        domain/                   — общие модели и игровая экономика (GameEngine, ...), чистый Kotlin/JVM
        data/                     — профиль в DataStore, игровой контент из assets/content/*.json
        navigation/api/           — FinEduRoute и контракт FeatureNavigationContribution
        ui/                       — FinEduTheme, общие компоненты (FinButton, ...), превью
    features/<feature>/
        domain/                   — модели фичи, интерфейсы репозиториев, use case'ы. Чистый Kotlin
        data/                     — реализации репозиториев, Room/DataStore/assets, Hilt-привязки
        ui/                       — Compose-экраны, ViewModel, UiState, регистрация в навигации
build-logic/                      — convention-плагины Gradle
config/detekt/                    — конфиг detekt
```

Фичи и экраны:

| Фича         | Экраны                                                                  |
|--------------|-------------------------------------------------------------------------|
| `onboarding` | Знакомство: коробка, три сторис, первое решение                         |
| `hero`       | Создание питомца: шёрстка, убор, имена, демо-режим                      |
| `home`       | Главный: уголок, показатели, баланс, цель, задание недели               |
| `budget`     | План недели: раскладка по мешочкам, план против факта                   |
| `shop`       | Магазин: покупка, «Подожду», нехватка                                   |
| `savings`    | Копилка: цели для уголка, пополнение, снятие, ритуал цели               |
| `quests`     | Задания, «Никто: / Реклама:», «Это развод?», челлендж недели             |
| `period`     | Итоги недели, рост стадии                                               |
| `parent`     | Для взрослого: барьер, прогресс, статьи, памятка, настройки             |
| `learn`      | Словарик, шортс, лента                                                  |

Общее состояние игры читают и меняют ViewModel через `GameRepository` и чистые функции движков
(`GameEngine`, `SavingsEngine`, `QuestEngine`); контент — через `ContentRepository`. Фичевые `data`-модули пустые:
своего хранилища фичам не нужно.

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
- `app` подключает `ui` и `data` каждой фичи и `product-core:data` и связывает их в одном Hilt-графе.
- Общее состояние игры (`GameRepository`) и контент (`ContentRepository`) — интерфейсы в `product-core:domain`,
  реализации в `product-core:data`. Фичи получают их через Hilt и не хранят игровое состояние сами.
- Игровой контент (товары, цели, задания, сюжет, словарик, шортсы, материалы для взрослого) — JSON в
  `product-core/data/src/main/assets/content/`. Новый товар или задание — новая запись без правки кода;
  `ContentParserTest` проверяет, что файлы разбираются.
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
- общие компоненты — `product-core:ui/components`: `FinTopBar`, `FinBottomBar`, `DzynkiAmount`, `BagChip`,
  `TubeIndicator`, `SpeechBubble`, `AmountStepper`, `ConfirmDialog`, `FeedbackSheet` (обратная связь после любого
  действия); цвет, иконка и подпись мешочков и показателей — `Bag.color` / `iconRes` / `labelRes` из `GameStyle.kt`
- Lottie-анимации — только через `FinLottie` из `product-core:ui/components`, файлы JSON — в `res/raw` модуля
  (`anim_` в имени). Загрузку по URL не используем: приложение работает без сети
- рисунки — `product-core:ui/illustration`: `PetView` (питомец и его эмоции `PetMood`), `RoomScene` (уголок).
  Координаты в них — геометрия SVG из макета, поэтому пакет исключён из правила detekt `MagicNumber`
- настройки взрослого «Крупный шрифт» и «Спокойный режим» — параметры `FinEduTheme(largeFont, reduceMotion)`;
  анимации проверяют `FinEduTheme.reduceMotion`

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
   Результат: `app/build/outputs/apk/release/app-release.apk` (minSdk 26 — Android 8.0+, R8 включён).
   Без `keystore.properties` собирается неподписанный `app-release-unsigned.apk`.
4. Установите на устройство: `adb install -r app/build/outputs/apk/release/app-release.apk`.

Версия приложения: `versionCode` / `versionName` в `app/build.gradle.kts`.
