plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.timurvg.screenlocker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.timurvg.screenlocker"
        minSdk = 24  // Минимум API 24 для TYPE_APPLICATION_OVERLAY
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true  // Включаем ViewBinding
    }
}

dependencies {
    // Базовые зависимости Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)

    // Для работы с вибрацией
    implementation(libs.androidx.core)

    // Для Foreground Service
    implementation(libs.androidx.lifecycle.service)

    // Для обработки тапов
    implementation(libs.androidx.activity.ktx)

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}