plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.arjunsudheer.gymbuddy"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.arjunsudheer.gymbuddy"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            ndk {
                debugSymbolLevel = "FULL"
            }
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.RequiresOptIn")
    }
    jvmToolchain(21)
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
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.material.icons.extended)
    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.room.compiler)
    implementation(libs.androidx.room.ktx) // For coroutines support
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // For collectAsState
    implementation(libs.androidx.lifecycle.common.java8)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
}
