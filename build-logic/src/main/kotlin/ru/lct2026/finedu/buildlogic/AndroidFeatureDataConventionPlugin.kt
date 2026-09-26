package ru.lct2026.finedu.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Модуль `data` фичи: реализации репозиториев из `domain`, хранение (Room/DataStore), контент из assets,
 * Hilt-привязки. Без Compose. Видит `domain` своей фичи (подключается явно в build.gradle.kts).
 */
class AndroidFeatureDataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(libs.pluginId("android-library"))
        pluginManager.apply("convention.hilt")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
        }

        dependencies.apply {
            add("implementation", project(":app-modules:product-core:domain"))
            add("implementation", libs.lib("kotlinx-coroutines-core"))
        }
        addUnitTestDependencies()
    }
}
