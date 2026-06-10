import java.util.Properties

plugins {

    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}
kapt {
    correctErrorTypes = true
    javacOptions {
        option("-source", "11")
        option("-target", "11")
    }
}


android {
    namespace = "com.wade.ocr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wade.ocr"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }
signingConfigs {
    create("release") {
        keyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: "androidapk"
        keyPassword = System.getenv("SIGNING_KEY_PASSWORD") ?: "jjchen"
        val keyFile = System.getenv("SIGNING_KEY_FILE")
        storeFile = if (keyFile != null) file(keyFile) else file("/home/wade/.ssh/androidapk.jks")
        storePassword = System.getenv("SIGNING_STORE_PASSWORD") ?: "jjchen"
    }
}

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Use Java toolchain to enforce compatible JDK version
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }

    // Kotlin compilation target set to Java 11
    kotlinOptions {
        jvmTarget = "11"
    }



    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    // Room persistence library
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    // Gson for JSON conversion of complex fields
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // ML Kit
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")

    // Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // CameraX
    val camerax_version = "1.3.1"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Disable lintVitalRelease task to prevent failure due to missing R class
tasks.whenTaskAdded {
    if (name == "lintVitalRelease") {
        enabled = false
    }
}
