package ru.lct2026.finedu.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal val JAVA_VERSION = JavaVersion.VERSION_17
internal val KOTLIN_JVM_TARGET = JvmTarget.JVM_17

/**
 * Общие настройки Android-модуля: SDK, Java/Kotlin target, unit-тесты с ресурсами (для Robolectric).
 */
internal fun Project.configureKotlinAndroid(extension: CommonExtension) {
    extension.apply {
        compileSdk = libs.version("android-compileSdk").toInt()
        defaultConfig.minSdk = libs.version("android-minSdk").toInt()
        compileOptions.sourceCompatibility = JAVA_VERSION
        compileOptions.targetCompatibility = JAVA_VERSION
        testOptions.unitTests.isIncludeAndroidResources = true
    }
    configureKotlinCompile()
}

internal fun Project.configureKotlinCompile() {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(KOTLIN_JVM_TARGET)
    }
}

internal fun Project.configureCompose(extension: CommonExtension) {
    pluginManager.apply(libs.pluginId("kotlin-compose"))
    extension.buildFeatures.compose = true

    val bom = libs.lib("androidx-compose-bom")
    dependencies.apply {
        add("implementation", dependencies.platform(bom))
        add("implementation", libs.lib("androidx-compose-ui"))
        add("implementation", libs.lib("androidx-compose-material3"))
        add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
        add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))

        add("testImplementation", dependencies.platform(bom))
        add("testImplementation", libs.lib("androidx-compose-ui-test-junit4"))
        // ui-test тянет старый espresso, несовместимый с Android 36 под Robolectric.
        add("testImplementation", libs.lib("test-espresso-core"))
        // ComponentActivity для createComposeRule() под Robolectric. Только debug — не протаскиваем в release.
        add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))
    }
}

internal fun Project.addUnitTestDependencies() {
    tasks.withType<Test>().configureEach {
        // Модули-заглушки пока без тестов — не валим `testDebugUnitTest` на пустом наборе.
        failOnNoDiscoveredTests.set(false)
        // Robolectric (Android 36+) на JDK 17+ лезет во внутренности FileDescriptor.
        jvmArgs(
            "--add-opens=java.base/java.io=ALL-UNNAMED",
            "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED"
        )
    }

    dependencies.apply {
        add("testImplementation", libs.lib("test-junit"))
        add("testImplementation", libs.lib("test-mockk"))
        add("testImplementation", libs.lib("test-kotlinx-coroutines"))
        add("testImplementation", libs.lib("test-turbine"))
    }
}
