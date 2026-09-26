package ru.lct2026.finedu.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Модуль `domain` фичи: модели, интерфейсы репозиториев, use case'ы.
 * Чистый Kotlin без Android и без DI-аннотаций — готов к переносу в `commonMain` при переходе на KMP.
 */
class KotlinFeatureDomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("convention.kotlin-jvm")

        dependencies.apply {
            add("api", project(":app-modules:product-core:domain"))
            add("implementation", libs.lib("kotlinx-coroutines-core"))
        }
    }
}
