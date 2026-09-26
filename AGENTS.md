# Behavioral guidelines (Гайд от Карпатого)

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

## 5. Language
Отвечай пользователю на русском

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

# Repository Guidelines

Этот документ помогает агентам эффективно работать в репозитории.
Здесь собраны практики, дополняющие README и профильные инструкции.
Поддерживайте актуальность файла: при изменениях процессов обновляйте соответствующие разделы.

## Контекст продукта

- «Питомец Финни» — прототип Android-приложения по финансовой грамотности для детей 7–11 лет (хакатон ЛЦТ 2026,
  задача Департамента финансов Москвы). Маскот — Дзынь, игровая валюта — «дзыньки».
- Ограничения ТЗ, которые нельзя нарушать кодом: нет реальных денег и платежей, рекламы, внутриигровых покупок,
  чатов, рейтингов, сбора персональных данных, сетевых запросов и манипулятивных механик (таймеры-давилки,
  «потеряешь питомца»). Питомец не умирает, ошибка не обнуляет прогресс.
- Тексты для детей: короткие фразы, простые вычисления. Все тексты — в ресурсах или JSON-контенте, не в коде.
- Если задача противоречит этим ограничениям — остановись и сообщи пользователю.

## Code
Никогда не используй механизмы рефлексии в коде на kotlin: ни в продакшене ни в тестах.
Если видишь использование рефлексии в коде обрати на это внимание пользователя и приведи данную секцию правил.
Для ветвления по `sealed class`/`sealed interface` и `enum` не используй `as?`/type casts; используй exhaustive `when`.
При добавлении новых файлов всегда следуй структуре ### Структура пакетов внутри модуля из `README.md`
Каждая фича — три модуля `domain` / `data` / `ui` (см. `README.md`, раздел **Архитектура**). Проект готовится к
переводу на Kotlin Multiplatform, поэтому:
- в `domain` — только чистый Kotlin: без Android SDK, Compose, Room и без DI-аннотаций (`@Inject`, `@Module`);
- `ui` никогда не зависит от `data`; фичи не зависят друг от друга;
- ViewModel экрана наследует `StatefulViewModel` из `product-core:ui`, одноразовые эффекты — через `offerEvent`
  (правила — раздел **ViewModel и состояние экрана** в `README.md`); свои базовые ViewModel/`MutableStateFlow` не заводим;
- игровая логика (бюджет, покупки, накопления, питомец, периоды) — в `domain` (общая — в
  `:app-modules:product-core:domain`), чтобы её можно было покрыть JVM Unit-тестами.

## Supported Agents

- Поддерживаемые агенты: Claude Code и Codex.
- Cursor в этом репозитории не поддерживается: не добавляйте `.cursor` правила и не рассчитывайте на Cursor-specific поведение.
- `CLAUDE.md` импортирует этот файл через `@AGENTS.md`; Codex читает `AGENTS.md` напрямую.
- Настройка окружения для агентов описана в `docs/agents.md`.

## Tooling & MCP

- MCP-конфиги хранятся в репозитории:
  - Claude Code: `.mcp.json`
  - Codex: `.codex/config.toml`
- Локальные секреты и токены храните только в `.env`, созданном из `.env.example`. `.env` не коммитится.
- При изменении MCP-серверов обновляйте оба конфига и `docs/agents.md`, если меняются переменные окружения или шаги установки.
- **context7 MCP**: основной канал для SDK и API-документации. Формулируйте запросы конкретно, указывая версии и
  интересующие модули (версии — в `gradle/libs.versions.toml`). При получении устаревших данных переформулируйте
  запрос или уточните версию.
- **Figma MCP**: используйте для выгрузки спецификаций из Figma, включая размеры, иконки и экспорт ассетов.

## Skills

- Canonical skills хранятся в `.agents/skill-definitions/<skill-name>.md`.
- Agent-specific файлы в `.claude/skills` и `.codex/skills` — только wrappers для discoverability. Подробные инструкции в них не дублируйте.
- Если меняете skill, сначала обновите canonical файл в `.agents/skill-definitions`. Wrapper меняйте только если изменился `name` или `description`.

Доступные repo skills:

| Skill                                                           | Когда использовать                                                         |
|-----------------------------------------------------------------|----------------------------------------------------------------------------|
| `.agents/skill-definitions/android-compose-ui.md`                | Compose-верстка или ревью Compose UI по макетам с темой `FinEduTheme`.     |
| `.agents/skill-definitions/feature-module-adder.md`             | Создание новой фичи (domain/data/ui) или экрана с регистрацией в навигации. |
| `.agents/skill-definitions/android-unit-test-automator.md`      | Оценка необходимости и написание Unit-тестов для Kotlin/Android сущностей. |
| `.agents/skill-definitions/android-unit-test-reviewer.md`       | Ревью Unit-тестов для Kotlin/Android сущностей.                            |
| `.agents/skill-definitions/android-component-test-automator.md` | Написание Compose component-тестов (Robolectric) по тест-кейсу/сценарию.   |
| `.agents/skill-definitions/android-component-test-reviewer.md`  | Ревью Compose component-тестов.                                            |
| `.agents/skill-definitions/android-e2e-test-automator.md`       | Написание Android e2e UI tests в `app/src/androidTest`.                    |
| `.agents/skill-definitions/android-e2e-test-reviewer.md`        | Ревью Android e2e UI tests, flaky/infra риски.                             |

## Documentation

- При необходимости запрашивайте актуальную документацию SDK, плагинов и API через context7 MCP.
- Coding Standards, Compose conventions и правила доступности берите из `README.md`.
- Структуру модулей `domain`/`data`/`ui` и пакетов внутри них берите из раздела `Структура пакетов внутри модуля`
  в `README.md`.
- Для изменений в `app-modules/product-core` дополнительно действует `app-modules/product-core/AGENTS.md`.

## Testing

- На проекте принято покрывать код Unit-тестами. Игровая экономика в `domain` покрывается обязательно.
- В Kotlin-тестах используйте единую аннотацию `org.junit.Test` (JUnit4).
  Не используйте `kotlin.test.Test` и `org.junit.jupiter.api.Test`.
- Обычные JVM Unit-тесты не требуют runner'а. Тесты, которым нужен Robolectric, помечайте
  `@RunWith(RobolectricTestRunner::class)`.
- Для нового кода покрытие тестами должно быть не ниже 80%.
- Покрытие проверяйте через Kover (агрегированный отчёт по всем модулям):
  - `./gradlew koverHtmlReport`
  - `./gradlew koverXmlReport`
- Compose-код Unit-тестами тестировать не требуется.
- По возможности не используйте Robolectric; предпочитайте обычные Unit-тесты.
- Исключение: Compose component-тесты с `createComposeRule()` (`androidx.compose.ui.test.junit4.v2`) пишутся
  в `src/test/kotlin` `ui`-модуля фичи и используют Robolectric, как описано в `README.md`.
- Не нужно тестировать конструкторы дата классов и сериализацию дата классов через `kotlinx-serialization`.

## Workflow

1. **Перед стартом**
    - Соберите контекст.
    - Обязательно прочитайте корневой `README.md`
    - По необходимости запросите свежую документацию через MCP.
2. **Во время работы**
    - Готовьте план, если задача не тривиальна; обновляйте его после выполнения шагов.
    - Для UI правок сверяйтесь с макетами, фиксируйте используемые компоненты и состояния.
    - UI компоненты должны быть реализованы в точном соответствии с макетами.
3. **Перед сдачей**
    - Запустите релевантные проверки.
    - Базовый набор Gradle-задач для проверки:
      - `./gradlew spotlessApply`
      - `./gradlew detekt`
      - `./gradlew lintVitalRelease`
      - `./gradlew test`
    - Подготовьте краткий отчёт о проделанной работе, перечислите затронутые файлы и команды, которые нужно выполнить
      для валидации.
    - Оформите результаты в финальном сообщении и при необходимости добавьте материалы (скриншоты, ссылки на макеты).
    - Если просят сделать коммит - обязательно прочитай гайды как делать коммит в `README.md`
    - Перед коммитом проверь, меняет ли коммит текущие договоренности в 'README.md', если меняет - предложи пользователю обновленный текст 'README.md'.
    - Перед коммитом проверь, противоречит ли коммит текущим договоренностям в 'README.md', если противоречит - сообщи об этом пользователю.

## Communication

- Соблюдайте дружелюбный и деловой тон. Сообщения должны быть краткими, структурированными и самодостаточными.
- Используйте английские термины для устоявшихся сущностей (`Gradle`, `CI`, `PR`, `Compose`, `MCP`).
- При необходимости задавайте уточняющие вопросы, но избегайте лишних запросов, если контекст уже достаточен.
