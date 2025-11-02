import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose) // компилятор Compose
    alias(libs.plugins.compose)        // MPP-зависимости Compose
    alias(libs.plugins.kotlin.cocoapods)
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin.get()

}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget()
    iosArm64()           // устройства (iPhone, iPad)
    iosSimulatorArm64()  // симуляторы на Apple Silicon
    iosX64()
    cocoapods {
        summary = "Shared module for VikiRobux"
        homepage = "https://example.com"
        version = "1.0"
        ios.deploymentTarget = "14.0"
        framework {
            baseName = "shared"
            isStatic = false
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.okio)
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.lifecycle.runtime.compose)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.compose.ui)
                implementation(libs.androidx.compose.material3)
                implementation(libs.androidx.activity.compose)
                implementation(libs.ktor.client.okhttp)
            }
        }
        val iosMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
    tasks.matching { it.name == "syncComposeResourcesForIos" }.configureEach { enabled = false }
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "shared"
            isStatic = false // можно true, если нужно статически
        }
    }
}

android {
    namespace = "com.faigenbloom.vikarobux"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
tasks.register<Sync>("packForXcode") {
    group = "build"

    val mode = System.getenv("CONFIGURATION") ?: "Debug"

    // Найти первый iOS таргет
    val iosTarget = kotlin.targets
        .withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
        .firstOrNull { it.konanTarget.family.isAppleFamily }
        ?: error("No iOS target found")

    // 🔥 Ищем framework, который мы создали выше
    val framework = iosTarget.binaries
        .filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.Framework>()
        .firstOrNull { it.buildType.name.equals(mode, ignoreCase = true) }
        ?: error("Framework for mode=$mode not found")

    dependsOn(framework.linkTaskProvider)
    from({ framework.outputDirectory })
    into(layout.buildDirectory.dir("xcode-frameworks"))
}