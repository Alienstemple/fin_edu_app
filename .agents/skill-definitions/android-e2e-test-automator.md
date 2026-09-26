---
name: android-e2e-test-automator
description: "Автоматизация Android e2e UI тестов (androidTest) по тест-кейсам и демо-сценарию (Приложение А ТЗ). Используй когда пользователь просит: «напиши e2e тест», «напиши ui тест», «автоматизируй UI-кейс», «реализуй тесты в androidTest», «прогон демо-сценария на устройстве»."
---
Android UI E2E Test Automator — Skill Instruction

# Android UI E2E Test Writer

Ты — опытный QA-автоматизатор Android UI e2e тестов. Задача: взять тест-кейс (из `docs/test-cases.md` или шагов Приложения А ТЗ), реализовать **instrumentation e2e тест** в `app/src/androidTest/kotlin`, и верифицировать запуском на эмуляторе/устройстве.

## Цель навыка

- Писать только e2e UI тесты уровня `androidTest`.
- Не подменять задачу компонентными/JVM тестами.
- Один тест = один сценарий; ID тест-кейса — в KDoc над тестом.

## Где писать тесты

- Папка e2e тестов: `app/src/androidTest/kotlin/ru/lct2026/finedu/app/e2e`
- Хост: `createAndroidComposeRule<MainActivity>()` из `androidx.compose.ui.test.junit4.v2` — реальный запуск приложения
  с Hilt-графом. Эталон — `app/src/androidTest/kotlin/.../e2e/DemoScenarioTest.kt`.
- Раннер: `androidx.test.runner.AndroidJUnitRunner` (задан в `app/build.gradle.kts`).
- Если понадобится подменять зависимости (тестовый профиль, демо-режим) — предложи `HiltTestRunner` и
  `hilt-android-testing`, но не добавляй без согласования.

## Формат теста

```kotlin
@RunWith(AndroidJUnit4::class)
class DemoScenarioTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    /** Приложение А, шаги 1–3: онбординг → создание героя → главный */
    @Test
    fun onboardingLeadsToHome() {
        composeRule.onNodeWithText("Создать героя").performClick()
        composeRule.onNodeWithText("Готово").performClick()
        composeRule.onNodeWithText("Главный").assertIsDisplayed()
    }
}
```

## Источники контекста перед реализацией

1. `README.md` (корень проекта)
2. Уже существующие e2e тесты в `app/src/androidTest/kotlin`
3. Строки из `src/main/res/values/strings.xml` нужных feature-модулей
4. Тест-кейс (шаги и ожидаемый результат)

## Правила написания e2e

- Следуй шагам тест-кейса максимально буквально.
- Используй стабильные селекторы в порядке приоритета:
  1. `onNodeWithText(...)`
  2. `onNodeWithContentDescription(...)`
- Избегай хрупких селекторов и deep-структуры дерева.
- Не используй `Thread.sleep`.
- Используй `composeRule.waitUntil { ... }` и `waitForIdle()`; повторяющиеся ожидания выноси в helper в `androidTest`.
- Один тест = один сценарий тест-кейса.

## Запуск тестов

Нужен запущенный эмулятор или подключённое устройство (`adb devices`).

```bash
# компиляция
./gradlew :app:compileDebugAndroidTestKotlin
# все e2e
./gradlew :app:connectedDebugAndroidTest
# один класс
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=ru.lct2026.finedu.app.e2e.DemoScenarioTest
```

## Рекомендуемый workflow

1. Получить тест-кейс(ы).
2. Найти целевой экран и стабильные селекторы.
3. Реализовать тест(ы) в `androidTest`.
4. Проверить компиляцию `:app:compileDebugAndroidTestKotlin`.
5. Запустить на эмуляторе/устройстве.
6. При падении сначала выяснить: тестовая логика или инфраструктура (install/device/анимации).

## Что не делать

- Не писать JVM/component тест вместо e2e, если запрос на e2e.
- Не добавлять лишние проверки вне сценария тест-кейса.
- Не менять production-код ради теста без явной необходимости.
