pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "fin_edu_app"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":app",
    ":app-modules:product-core:domain",
    ":app-modules:product-core:data",
    ":app-modules:product-core:navigation:api",
    ":app-modules:product-core:ui",
    ":app-modules:features:onboarding:domain",
    ":app-modules:features:onboarding:data",
    ":app-modules:features:onboarding:ui",
    ":app-modules:features:hero:domain",
    ":app-modules:features:hero:data",
    ":app-modules:features:hero:ui",
    ":app-modules:features:home:domain",
    ":app-modules:features:home:data",
    ":app-modules:features:home:ui",
    ":app-modules:features:budget:domain",
    ":app-modules:features:budget:data",
    ":app-modules:features:budget:ui",
    ":app-modules:features:shop:domain",
    ":app-modules:features:shop:data",
    ":app-modules:features:shop:ui",
    ":app-modules:features:savings:domain",
    ":app-modules:features:savings:data",
    ":app-modules:features:savings:ui",
    ":app-modules:features:quests:domain",
    ":app-modules:features:quests:data",
    ":app-modules:features:quests:ui",
    ":app-modules:features:period:domain",
    ":app-modules:features:period:data",
    ":app-modules:features:period:ui",
    ":app-modules:features:parent:domain",
    ":app-modules:features:parent:data",
    ":app-modules:features:parent:ui"
)
