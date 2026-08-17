plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.canvas.android.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.canvas.android.app"
        minSdk = 29
        targetSdk = 34
        versionCode = 1001
        versionName = "0.100.1"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime)
    implementation(libs.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.google.material)
}
