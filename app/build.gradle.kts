plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.must_connect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.must_connect"
        minSdk = 29
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

        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.backendless:backendless:6.3.2")
    implementation ("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.backendless:backendless:6.3.2")
    implementation ("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("com.google.code.gson:gson:2.8.9")




}