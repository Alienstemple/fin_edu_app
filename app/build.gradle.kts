import java.util.Properties

plugins {
    alias(libs.plugins.convention.android.application)
    alias(libs.plugins.convention.hilt)
}

// Параметры подписи релиза лежат вне git: скопируйте keystore.properties.example в keystore.properties.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

android {
    namespace = "ru.lct2026.finedu.app"

    defaultConfig {
        applicationId = "ru.lct2026.finedu"
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystoreProperties.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    lint {
        lintConfig = rootProject.file("lint.xml")
        abortOnError = true
        checkDependencies = true
    }
}

dependencies {
    implementation(projects.appModules.productCore.domain)
    implementation(projects.appModules.productCore.data)
    implementation(projects.appModules.productCore.ui)
    implementation(projects.appModules.productCore.navigation.api)

    implementation(projects.appModules.features.onboarding.ui)
    implementation(projects.appModules.features.onboarding.data)
    implementation(projects.appModules.features.hero.ui)
    implementation(projects.appModules.features.hero.data)
    implementation(projects.appModules.features.home.ui)
    implementation(projects.appModules.features.home.data)
    implementation(projects.appModules.features.budget.ui)
    implementation(projects.appModules.features.budget.data)
    implementation(projects.appModules.features.shop.ui)
    implementation(projects.appModules.features.shop.data)
    implementation(projects.appModules.features.savings.ui)
    implementation(projects.appModules.features.savings.data)
    implementation(projects.appModules.features.quests.ui)
    implementation(projects.appModules.features.quests.data)
    implementation(projects.appModules.features.period.ui)
    implementation(projects.appModules.features.period.data)
    implementation(projects.appModules.features.parent.ui)
    implementation(projects.appModules.features.parent.data)
    implementation(projects.appModules.features.learn.ui)
    implementation(projects.appModules.features.learn.data)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.espresso.core)
}
