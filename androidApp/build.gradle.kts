plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.faigenbloom.vikarobux.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.faigenbloom.vikarobux.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures { compose = true }

    // при kotlin-compose плагине это можно не указывать,
    // но если IDE ругается — оставь:
    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}
kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
}
