package ru.lct2026.finedu.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Android library с Compose: для общих UI-модулей (`product-core:ui`).
 */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(libs.pluginId("android-library"))

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            configureCompose(this)
        }
        addUnitTestDependencies()
    }
}
