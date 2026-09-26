package ru.lct2026.finedu.buildlogic

import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Применяется только в корневом `build.gradle.kts`:
 * - Detekt и Spotless (ktlint) во всех модулях;
 * - Kover: агрегированный отчёт о покрытии в корне (`./gradlew koverHtmlReport`).
 */
class RootProjectConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        check(this == rootProject) { "convention.root-project применяется только в корневом проекте" }

        // Каталог берём у корня: у subprojects на этом этапе extension `libs` ещё не зарегистрирован.
        val catalog = libs
        val koverPluginId = catalog.pluginId("kover")
        pluginManager.apply(koverPluginId)
        configureSpotless(this)

        subprojects {
            val subproject = this
            pluginManager.apply(catalog.pluginId("detekt"))
            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                config.setFrom(rootProject.file("config/detekt/detekt.yml"))
                source.setFrom("src/main/kotlin", "src/test/kotlin", "src/testDebug/kotlin", "src/androidTest/kotlin")
            }
            // detekt 1.23 собран под свою версию Kotlin — не даём Gradle поднять её до версии проекта.
            configurations.matching { it.name == "detekt" }.configureEach {
                resolutionStrategy.eachDependency {
                    if (requested.group == "org.jetbrains.kotlin") useVersion(DETEKT_KOTLIN_VERSION)
                }
            }

            pluginManager.withPlugin(catalog.pluginId("kotlin-jvm")) { enableKover(subproject, koverPluginId) }
            pluginManager.withPlugin(catalog.pluginId("android-library")) { enableKover(subproject, koverPluginId) }
        }
    }

    private fun enableKover(subproject: Project, koverPluginId: String) {
        subproject.pluginManager.apply(koverPluginId)
        val root = subproject.rootProject
        root.dependencies.add("kover", root.dependencies.project(mapOf("path" to subproject.path)))
    }

    private fun configureSpotless(root: Project) {
        root.pluginManager.apply(root.libs.pluginId("spotless"))
        val ktlintVersion = root.libs.version("ktlint")
        root.extensions.configure<SpotlessExtension> {
            kotlin {
                target("**/src/**/*.kt")
                targetExclude("**/build/**")
                ktlint(ktlintVersion)
            }
            kotlinGradle {
                target("**/*.gradle.kts")
                targetExclude("**/build/**")
                ktlint(ktlintVersion)
            }
        }
    }

    private companion object {
        const val DETEKT_KOTLIN_VERSION = "2.0.21"
    }
}
