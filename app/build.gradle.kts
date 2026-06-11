import java.util.Properties

plugins {

    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
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
			val localProperties = Properties()
			val localPropertiesFile = rootProject.file("local.properties")
			if (localPropertiesFile.exists()) {
				localProperties.load(localPropertiesFile.inputStream())
			}

			keyAlias = System.getenv("KEY_ALIAS") ?: localProperties.getProperty("keystore.key.alias")
			keyPassword = System.getenv("KEY_PASSWORD") ?: localProperties.getProperty("keystore.key.password")

			val envKeyFile = System.getenv("KEY_FILE")
			val localKeyFile = localProperties.getProperty("keystore.path")

			if (envKeyFile != null) {
				storeFile = file(envKeyFile)
			} else if (localKeyFile != null) {
				storeFile = file(localKeyFile)
			} else {
				val defaultPath = "/home/wade/.ssh/androidapk.jks"
				if (file(defaultPath).exists()) {
					storeFile = file(defaultPath)
				}
			}

			storePassword = System.getenv("STORE_PASSWORD") ?: localProperties.getProperty("keystore.password")
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

base {
    archivesName = "com.wade.ocr-${android.defaultConfig.versionName}"
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
    ksp("androidx.room:room-compiler:2.6.1")
    // Gson for JSON conversion of complex fields
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // ML Kit
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

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
