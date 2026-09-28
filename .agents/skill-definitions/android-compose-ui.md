---
name: android-compose-ui
description: Верстка новых Compose-экранов и компонентов в точном соответствии с макетами, используя тему FinEduTheme и общие компоненты product-core:ui. Используй когда пользователь просит «сверстай экран», «сделай компонент», «ревью Compose UI», «перенеси макет в Compose».
---

# Compose UI Skill

## 1. Порядок работы

1. Открыть макет (Figma MCP или картинку/описание от пользователя), изучить все состояния
   (idle, loading, empty, error, «не хватает дзынек») и edge cases.
2. Проверить, есть ли готовые компоненты в `ru.lct2026.finedu.productcore.ui.components` — не изобретать своё.
3. Сверить с макетом: цвет → `MaterialTheme.colorScheme.xxx` или `FinEduTheme.colors.xxx` (мешочки, золото, стекло),
   шрифт → `MaterialTheme.typography.xxx`, радиус → `MaterialTheme.shapes.xxx`.
   Нового токена нет в теме — добавить его в `product-core/ui/theme`, а не хардкодить в фиче.
4. Написать stateless `XxxScreen(state, callbacks, modifier)`, добавить превью с `@Preview`.

## 2. Тема и токены

Используй **только** `FinEduTheme` (`ru.lct2026.finedu.productcore.ui.theme`), внутри — `MaterialTheme`.

```kotlin
// Правильно
Text(
    text = title,
    style = MaterialTheme.typography.titleLarge,
    color = MaterialTheme.colorScheme.onSurface,
)

// Не использовать
Text(text = title, fontSize = 14.sp, color = Color(0xFF7B4FD6))
```

Приложение **только в тёмной теме** (макет — тёмные экраны Design-канваса, см. `docs/implementation-plan.md`).
Шрифт — Commissioner. Токены макета, которых нет в Material, — `FinEduTheme.colors`: `needs` / `wants` / `savings`
(три мешочка), `gold`, `selection`, стекло и градиенты. Фон экранов рисует `FinEduBackground` в `MainActivity`,
поэтому `Scaffold` экрана — с `containerColor = Color.Transparent`. Карточки — `Modifier.glass()`,
иконки — `ic_*` из `product-core:ui` (линейные, tint в Compose).

## 3. Требования ТЗ к UI (дети 7–11 лет)

- Основной текст — не меньше 16sp: `bodyMedium` и крупнее. `bodySmall`/`labelSmall` для значимого текста не используем.
- Зона нажатия — не меньше 48dp (`MinTouchTarget`). `FinButton` уже соблюдает это.
- Смысл не передаётся только цветом: к цвету добавляй иконку или подпись («Нужное», «Хочу», «Копилка»).
- Поддержка крупного шрифта системы: не фиксируй высоту текстовых контейнеров, используй `heightIn(min = ...)`.
- Никаких таймеров-давилок, красных «срочно!», «последний шанс» — манипулятивные механики запрещены ТЗ.
- После любого действия — понятная обратная связь: что изменилось, почему, что дальше.

## 4. Кнопки

```kotlin
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle

FinButton(
    text = stringResource(R.string.shop_buy),
    onClick = onBuyClick,
)
FinButton(
    text = stringResource(R.string.common_back),
    onClick = onBack,
    style = FinButtonStyle.Secondary,
)
```

Не вызывай `androidx.compose.material3.Button` напрямую в фичах. Нужен новый вид кнопки — добавь вариант
в `FinButtonStyle`.

## 5. Строки и ресурсы

- Все тексты — в `src/main/res/values/strings.xml` своего модуля, префикс имени = экран (`shop_title`).
- Игровой контент (задания, товары, цели, словарик) — в JSON-контенте, не в `strings.xml` и не в коде.
- Изображения: векторные (`ic_`, `bg_`), растр — только WEBP.

## 6. WindowInsets

Приложение edge-to-edge (`enableEdgeToEdge()` в `MainActivity`). Экран строится на `Scaffold` и применяет
`innerPadding`. Для нижних панелей без `Scaffold`:

```kotlin
Column(
    modifier = Modifier
        .navigationBarsPadding()
        .imePadding(),
) { ... }
```

## 7. Превью

```kotlin
import androidx.compose.ui.tooling.preview.Preview
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Preview
@Composable
private fun ShopScreenPreview() {
    FinEduPreview {
        ShopScreen(state = previewState, onBuy = {})
    }
}
```

## 8. Доступность (a11y)

```kotlin
// Заголовок экрана/секции
Modifier.semantics { heading() }

// Иконка со смыслом — contentDescription из strings.xml; декоративная — contentDescription = null
Icon(painter = ..., contentDescription = stringResource(R.string.a11y_coin))

// Объединить карточку товара в один элемент для TalkBack
Modifier.semantics(mergeDescendants = true) { }
```

## 9. Compose-правила проекта

- Если `@Composable` функция создает и возвращает объект — она ничего не добавляет в композицию.
- Если `@Composable` функция добавляет что-то в композицию — она ничего не возвращает.
- Первый необязательный параметр — `modifier: Modifier = Modifier`, применяется к корневому элементу.
- Состояние поднимается в ViewModel (`StatefulViewModel`); composable экрана — stateless.
- `XxxRoute` собирает `viewModel.stateFlow` через `collectAsStateWithLifecycle()` и обрабатывает события через
  `ObserveEvents(viewModel.events) { ... }`; `XxxScreen(state, callbacks)` ничего не знает о ViewModel.

## 10. Частые ошибки

| Неправильно                                   | Правильно                                          |
|-----------------------------------------------|----------------------------------------------------|
| Хардкод `Color(0xFF...)`                      | `MaterialTheme.colorScheme.xxx` / `FinEduTheme.colors.xxx` |
| `fontSize = 14.sp`                            | `MaterialTheme.typography.bodyMedium` (≥16sp)      |
| `material3.Button(onClick = {...})` в фиче    | `FinButton(...)`                                   |
| Кнопка/иконка-кнопка меньше 48dp              | `Modifier.heightIn(min = MinTouchTarget)`          |
| Текст в коде                                  | `stringResource(R.string.xxx)`                     |
| `@PreviewLightDark`, светлая тема             | `@Preview` (тема только тёмная)                    |
| Превью без обёртки                            | `FinEduPreview { ... }`                            |
| Статус только цветом                          | Цвет + иконка/подпись                              |
