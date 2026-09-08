plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val ciRunNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0

android {
    namespace = "com.mindlizzard.feetstudio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mindlizzard.feetstudio.v5"
        minSdk = 29
        targetSdk = 35
        versionCode = 507200 + ciRunNumber
        versionName = if (ciRunNumber > 0) "5.7.2.$ciRunNumber" else "5.7.2"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("feetStudioDev") {
            storeFile = rootProject.file("keys/feet-studio-dev.jks")
            storePassword = "feetstudio-dev-2026"
            keyAlias = "feet-studio-dev"
            keyPassword = "feetstudio-dev-2026"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("feetStudioDev")
        }

        release {
            signingConfig = signingConfigs.getByName("feetStudioDev")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.01.01"))

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
