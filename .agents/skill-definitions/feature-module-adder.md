---
name: feature-module-adder
description: "Создание новой фичи (модули domain/data/ui) или нового экрана в существующей фиче с регистрацией в навигации. Используй когда пользователь просит «добавь экран», «создай фичу», «новый модуль», «заведи модуль под раздел»."
---

# Feature Module Adder

Skill добавляет фичу или экран в многомодульную структуру проекта. Каждая фича — три Gradle-модуля
`domain` / `data` / `ui`. Эталон полного потока — `app-modules/features/home`.

## Контекст и обязательное чтение

- `README.md`, разделы **Архитектура**, **Граф зависимостей** и **Структура пакетов внутри модуля**.
- `app-modules/product-core/navigation/api/README.md` — контракт навигации.
- Фича `home` целиком: `domain` (модель + интерфейс репозитория), `data` (реализация + Hilt), `ui` (ViewModel + экран).

## Решение: новая фича или экран в существующей

- Экран логически принадлежит существующему разделу (как «Задание» внутри «Заданий») → добавь экран в `ui` этой фичи.
- Новый самостоятельный раздел → новая фича `app-modules/features/<feature>/{domain,data,ui}`.
- Модель нужна двум фичам → она переезжает в `product-core:domain`, а не становится зависимостью фича → фича.

## Шаги: новая фича

1. `settings.gradle.kts` — добавь в `include(...)`:
   ```kotlin
   ":app-modules:features:<feature>:domain",
   ":app-modules:features:<feature>:data",
   ":app-modules:features:<feature>:ui",
   ```
2. `features/<feature>/domain/build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.convention.kotlin.feature.domain)
   }
   ```
   Если `Flow` торчит в публичных интерфейсах — добавь `api(libs.kotlinx.coroutines.core)`.
3. `features/<feature>/data/build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.convention.android.feature.data)
   }

   android {
       namespace = "ru.lct2026.finedu.feature.<feature>.data"
   }

   dependencies {
       implementation(projects.appModules.features.<feature>.domain)
   }
   ```
4. `features/<feature>/ui/build.gradle.kts` — то же, но плагин `convention.android.feature.ui` и namespace `...ui`.
   Convention-плагины уже подключают Compose, Hilt, `product-core:*` и тестовые зависимости. Не дублируй их.
5. `app/build.gradle.kts`:
   ```kotlin
   implementation(projects.appModules.features.<feature>.ui)
   implementation(projects.appModules.features.<feature>.data)
   ```
6. Маршрут: добавь `@Serializable` объект/класс в `FinEduRoute` (`product-core/navigation/api`) и обнови README
   этого модуля.
7. Код (package = путь модуля, `ru.lct2026.finedu.feature.<feature>.<domain|data|ui>`):
   - `domain`: `model/`, `repository/<Name>Repository.kt` (интерфейс), `usecase/` — только если есть логика.
     **Без** `@Inject` и других DI-аннотаций.
   - `data`: `repository/<Name>RepositoryImpl.kt` (`internal`, `@Inject constructor`), `di/<Feature>DataModule.kt`
     с `@Binds` интерфейса.
   - `ui`: `<Name>UiState.kt` (`@Immutable`), `<Name>ViewModel.kt` (`@HiltViewModel`, наследует
     `StatefulViewModel<<Name>UiState>`, см. README → **ViewModel и состояние экрана**), `<Name>Screen.kt` (`<Name>Route` с
     `hiltViewModel()` + stateless `<Name>Screen` + `@Preview`), `<Feature>NavigationContribution.kt`,
     `di/<Feature>NavigationModule.kt` (`@Binds @IntoSet`), `di/` — `@Provides` для use case'ов из `domain`,
     `src/main/res/values/strings.xml` (префикс `<screen>_`).
8. Переход на экран из других фич — через `onNavigate(FinEduRoute.<Name>)`.
9. Тесты: логика `domain` — JVM Unit-тесты; ViewModel — в `ui/src/test` с fake-репозиторием (см. `HomeViewModelTest`).

Фича-заглушка без данных: `domain` и `data` остаются с одним `build.gradle.kts`, код не создаётся.

## Шаги: экран в существующей фиче

Пункты 6, 7 (новый `Screen`/`ViewModel` + ещё один `composable<...>` в существующем `NavigationContribution`) и 8.

## Запреты

- `ui` не зависит от `data` (ни своей, ни чужой фичи).
- Фича не зависит от другой фичи ни в каком слое.
- В `domain` нет Android, Compose, Room, Hilt/`javax.inject` — иначе он не переедет в `commonMain`.
- Entity/DTO не выходят из `data`, `UiState` не уходит из `ui`.
- Не регистрируй один маршрут в двух модулях.

## Verification

- `./gradlew :app:assembleDebug`
- `./gradlew spotlessApply detekt`
- `./gradlew lintVitalRelease`
- `./gradlew test`
- Ручная проверка: экран открывается, «Назад» возвращает на предыдущий.
