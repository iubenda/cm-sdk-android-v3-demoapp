import org.gradle.api.GradleException
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.compose.compiler)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

fun requireReleaseProperty(name: String): String {
    val fromKeystore = keystoreProperties.getProperty(name)
    if (!fromKeystore.isNullOrBlank()) return fromKeystore
    val fromProject = project.findProperty(name)?.toString()
    if (!fromProject.isNullOrBlank()) return fromProject
    throw GradleException(
        "Release builds require '$name' in Kotlin/keystore.properties. See keystore.properties.example."
    )
}

val debugAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val debugAdmobInterstitialUnitId = "ca-app-pub-3940256099942544/1033173712"

val adiRegistrationSource = rootProject.file("adi-registration.properties")
val adiRegistrationAssetDir = file("src/main/assets")
val adiRegistrationAsset = adiRegistrationAssetDir.resolve("adi-registration.properties")

tasks.register("copyAdiRegistrationToken") {
    onlyIf { adiRegistrationSource.exists() }
    doLast {
        adiRegistrationAssetDir.mkdirs()
        adiRegistrationSource.copyTo(adiRegistrationAsset, overwrite = true)
        logger.lifecycle(
            "Copied adi-registration.properties into release assets for Play ownership verification."
        )
    }
}

tasks.register("deleteAdiRegistrationToken") {
    doLast {
        if (adiRegistrationAsset.exists()) {
            adiRegistrationAsset.delete()
            logger.lifecycle("Removed adi-registration.properties from app assets.")
        }
    }
}

android {
    namespace = "net.consentmanager.kmm.cmpsdkdemoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.consentmanager.kmm.cmdemoappkotlin"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["admobAppId"] = debugAdmobAppId
        buildConfigField(
            "String",
            "ADMOB_INTERSTITIAL_UNIT_ID",
            "\"$debugAdmobInterstitialUnitId\""
        )
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = debugAdmobAppId
            buildConfigField(
                "String",
                "ADMOB_INTERSTITIAL_UNIT_ID",
                "\"$debugAdmobInterstitialUnitId\""
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
                manifestPlaceholders["admobAppId"] =
                    keystoreProperties.getProperty("admobAppId") ?: debugAdmobAppId
                buildConfigField(
                    "String",
                    "ADMOB_INTERSTITIAL_UNIT_ID",
                    "\"${keystoreProperties.getProperty("admobInterstitialUnitId") ?: debugAdmobInterstitialUnitId}\""
                )
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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

afterEvaluate {
    tasks.named("mergeReleaseAssets").configure {
        dependsOn("copyAdiRegistrationToken")
    }
}

tasks.matching { it.name == "bundleRelease" || it.name == "assembleRelease" }.configureEach {
    doFirst {
        if (!keystorePropertiesFile.exists()) {
            throw GradleException(
                "Release builds require Kotlin/keystore.properties. See keystore.properties.example."
            )
        }
        val storeFilePath = keystoreProperties.getProperty("storeFile")
            ?: throw GradleException("keystore.properties is missing storeFile.")
        val storeFile = rootProject.file(storeFilePath)
        if (!storeFile.exists()) {
            throw GradleException(
                "Keystore file not found: ${storeFile.absolutePath}. " +
                    "Generate one with: keytool -genkeypair -v -storetype PKCS12 " +
                    "-keystore upload-keystore.jks -alias upload -keyalg RSA -keysize 2048 " +
                    "-validity 10000"
            )
        }
        listOf("storePassword", "keyAlias", "keyPassword").forEach { key ->
            val value = keystoreProperties.getProperty(key)
            if (value.isNullOrBlank() || value == "CHANGE_ME") {
                throw GradleException("keystore.properties must set a real value for '$key'.")
            }
        }
        if (!adiRegistrationSource.exists()) {
            logger.warn(
                "Kotlin/adi-registration.properties not found. " +
                    "Play package-name verification APK requires assets/adi-registration.properties. " +
                    "See adi-registration.properties.example."
            )
        }
        requireReleaseProperty("admobAppId")
        requireReleaseProperty("admobInterstitialUnitId")
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
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.ads)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(platform(libs.firebase.bom))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.cmsdkv3)
}
