import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.parcelize)
}


android {
    namespace = "com.nzdeveloper.androidclock.alarmtimer.stopwatch"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.nzdeveloper.androidclock.alarmtimer.stopwatch"
        minSdk = 23
        targetSdk = 36
        versionCode = 3
        versionName = "0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Optimization enabled for Amazon Appstore release build
            // Reduces APK size and obfuscates code
            isMinifyEnabled = true
            isShrinkResources = true
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

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    sourceSets {
        getByName("debug") {
            java.srcDir("build/generated/ksp/debug/kotlin")
            java.srcDir("build/generated/ksp/debug/java")
        }
        getByName("release") {
            java.srcDir("build/generated/ksp/release/kotlin")
            java.srcDir("build/generated/ksp/release/java")
        }
    }

}

dependencies {

    // ==============================
    // 🧱 Core Android Libraries
    // ==============================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ==============================
    // 🧪 Testing
    // ==============================
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ==============================
    // 🔄 Lifecycle (ViewModel + LiveData)
    // ==============================
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    // Fragment KTX (Required to use 'by viewModels()')
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // Lifecycle Runtime (Required for lifecycleScope.launch)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // ==============================
    // 📸 CameraX
    // ==============================
//    implementation("androidx.camera:camera-core:1.5.3")
//    implementation("androidx.camera:camera-camera2:1.5.3")
//    implementation("androidx.camera:camera-lifecycle:1.5.3")
//    implementation("androidx.camera:camera-view:1.5.3")

    // ==============================
    // 🎨 UI / UX Libraries
    // ==============================
    implementation("com.intuit.sdp:sdp-android:1.1.1")         // Scalable DP
    implementation("com.intuit.ssp:ssp-android:1.1.1")         // Scalable SP
    implementation("com.tbuonomo:dotsindicator:5.1.0")         // ViewPager dots
    implementation("com.airbnb.android:lottie:6.7.1")          // Animations
    implementation("com.facebook.shimmer:shimmer:0.5.0")       // Loading shimmer
    implementation("com.github.skydoves:powermenu:2.2.4")      // Dropdown menus


    // ==============================
    // 💉 Dependency Injection (Hilt)
    // ==============================
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)

    // ==============================
    // 🗄️ Room Database
    // ==============================
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)


    // GSON for saving complex lists to TinyDB
    implementation("com.google.code.gson:gson:2.10.1")


}