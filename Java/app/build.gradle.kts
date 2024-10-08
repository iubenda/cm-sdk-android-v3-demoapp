plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "net.consentmanager.kmm.cmpsdkdemoapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.consentmanager.kmm.cmpsdkdemoapp"
        minSdk = 28
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("net.consentmanager.sdkv3:cmsdkv3:0.2.0-alpha")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}
