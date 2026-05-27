plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

import java.util.Properties

val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties()
if (secretsFile.exists()) {
    secrets.load(secretsFile.inputStream())
}

fun secretOrEnv(key: String): String? = System.getenv(key) ?: secrets.getProperty(key)

android {
    namespace = "com.konnecta.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.konnecta.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Shared across all flavors
        buildConfigField("String", "ONESIGNAL_APP_ID", "\"${secretOrEnv("ONESIGNAL_APP_ID") ?: ""}\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${secretOrEnv("GOOGLE_CLIENT_ID") ?: ""}\"")
        buildConfigField("Double", "WEATHER_LAT", secretOrEnv("WEATHER_LAT") ?: "41.2856")
        buildConfigField("Double", "WEATHER_LNG", secretOrEnv("WEATHER_LNG") ?: "1.2504")
    }

    signingConfigs {
        create("release") {
            val keystorePath = secretOrEnv("KEYSTORE_FILE")
            if (keystorePath != null) {
                storeFile = rootProject.file(keystorePath)
                storePassword = secretOrEnv("KEYSTORE_PASSWORD")
                keyAlias = secretOrEnv("KEY_ALIAS")
                keyPassword = secretOrEnv("KEY_PASSWORD") ?: secretOrEnv("KEYSTORE_PASSWORD")
            }
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "SUPABASE_URL", "\"${secretOrEnv("DEV_SUPABASE_URL") ?: secretOrEnv("SUPABASE_URL") ?: ""}\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"${secretOrEnv("DEV_SUPABASE_ANON_KEY") ?: secretOrEnv("SUPABASE_ANON_KEY") ?: ""}\"")
            buildConfigField("String", "BASE_URL", "\"${secretOrEnv("DEV_BASE_URL") ?: secretOrEnv("BASE_URL") ?: ""}\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "SUPABASE_URL", "\"${secretOrEnv("SUPABASE_URL") ?: ""}\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"${secretOrEnv("SUPABASE_ANON_KEY") ?: ""}\"")
            buildConfigField("String", "BASE_URL", "\"${secretOrEnv("BASE_URL") ?: ""}\"")
        }
    }

    buildTypes {
        debug {
            // uses Android default debug keystore
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        xmlReport = true
        htmlReport = true
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.1"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:compose-auth")
    implementation("io.github.jan-tennert.supabase:compose-auth-ui")

    // Google Auth
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Ktor (Required for Supabase 3.x)
    implementation("io.ktor:ktor-client-android:3.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // OneSignal
    implementation("com.onesignal:OneSignal:5.1.6")

    // Coil (Image Loading)
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
