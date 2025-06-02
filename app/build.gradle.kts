plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id ("kotlin-parcelize")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin") // plugin Hilt
}

android {
    namespace = "com.restusofyan.crimealert_mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.restusofyan.crimealert_mobile"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        buildConfigField ("String", "BASE_URL", "\"http://192.168.1.10:3000/api/\"")
//        buildConfigField ("String", "BASE_URL", "\"http://10.0.2.2:3000/api/\"")
        buildConfigField ("String", "BASE_URL", "\"http://20.11.0.124/api/\"")

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
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
        viewBinding = true
        mlModelBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    tambahan plugin
    implementation (libs.circleimageview)
    implementation (libs.glide)
    implementation (libs.play.services.maps.v1820)
    implementation (libs.play.services.location)
    implementation (libs.material.v1100)
    implementation (libs.tensorflow.lite.task.audio)
    implementation (libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite)
    implementation (libs.tensorflow.lite.metadata)
    implementation (libs.androidx.lifecycle.runtime.ktx)

    implementation (libs.material.v1110)

    //tambahan plugin untuk koneksi backend
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.lottie)
    implementation(libs.androidx.viewpager2)

    implementation(libs.dagger.hilt.android)
    kapt(libs.hilt.compiler)

    implementation("io.socket:socket.io-client:2.0.1") {
        exclude(group = "org.json", module = "json")
    }
}