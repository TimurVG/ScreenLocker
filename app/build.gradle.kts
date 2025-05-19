plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace 'com.example.screenlocker'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.screenlocker"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
                targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
    buildFeatures {
        viewBinding true
    }
}

dependencies {
    // Core dependencies
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'

    // Material Design
    implementation 'com.google.android.material:material:1.11.0'

    // Lifecycle components
    implementation 'androidx.lifecycle:lifecycle-service:2.6.2'

    // WindowManager
    implementation 'androidx.window:window:1.2.0'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'

    // Для работы с вибрацией
    implementation 'androidx.core:core:1.12.0'

    // Для работы с разрешениями
    implementation 'pub.devrel:easypermissions:3.0.0'

    // Для отладки утечек памяти (опционально)
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
}