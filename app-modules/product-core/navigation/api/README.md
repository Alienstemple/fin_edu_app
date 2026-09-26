# :app-modules:product-core:navigation:api

Контракт навигации между фичами.

## Публичные контракты

### `FinEduRoute`

`sealed interface` с type-safe маршрутами (`@Serializable`) всех экранов MVP: `Onboarding`, `Hero`, `Home`,
`Budget`, `Shop`, `Savings`, `Quests`, `Quest(questId)`, `PeriodSummary`, `Parent`.

- Переход на экран другой фичи — только через `navController.navigate(FinEduRoute.X)`. Прямые зависимости
  между feature-модулями запрещены.
- Новый экран = новый маршрут здесь + обновление этого README.
- Аргументы маршрута — только примитивы и строки (id), не доменные модели.

### `FeatureNavigationContribution`

```kotlin
interface FeatureNavigationContribution {
    fun NavGraphBuilder.register(navController: NavController)
}
```

Каждый `ui`-модуль фичи реализует интерфейс и регистрирует свои `composable<FinEduRoute.X> { ... }`.
Реализация кладётся в Hilt-сет:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal interface ShopNavigationModule {
    @Binds
    @IntoSet
    fun bindNavigation(impl: ShopNavigationContribution): FeatureNavigationContribution
}
```

`:app` получает `Set<FeatureNavigationContribution>` и собирает `NavHost`.

## Инварианты

- Каждый маршрут регистрирует ровно одна фича. Двойная регистрация — ошибка.
- Модуль реэкспортирует `androidx.navigation:navigation-compose` через `api(...)`.
