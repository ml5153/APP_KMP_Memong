plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashLytics)
}

android {
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    namespace = "com.memong.aos"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.memong.aos"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        buildConfig = true
        compose = false
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        resources {
            pickFirsts += listOf(
                "META-INF/DEPENDENCIES"
            )
        }
    }

    signingConfigs {
        create("release") {
            keyAlias = "haru_memo_aos_release_key"
            storePassword = "harumemo!@12"
            keyPassword = "harumemo!@#123"
            storeFile = file("../../keystore/haru_memo/haru_memo_aos_release.keystore")
        }
    }

    android {
        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
                isDebuggable = true

                // 디버그 빌드에서는 강제 업데이트 비활성화 + 업데이트 타입도 오버라이드
                buildConfigField("boolean", "FORCE_UPDATE_OVERRIDE", "false")
                buildConfigField("int", "MIN_SUPPORTED_VERSION_OVERRIDE", "0")
                buildConfigField("int", "UPDATE_TYPE_OVERRIDE", "1") // 1 = IMMEDIATE, 2 = FLEXIBLE
                buildConfigField("String", "ADCASH_APP_ID", "\"9a39111d20e5467ba729ad5f5ddb9a0d\"")
                buildConfigField("String", "ADCASH_APP_SECRET", "\"191e43fe1a63477b\"")
                buildConfigField("String", "ADCASH_BOTTOM_BANNER_PID", "\"0db53989-fbf6-4b32-b546-e1417d16e718\"")
                buildConfigField("String", "ADCASH_FINISH_POPUP_PID", "\"4f6a61a3-b60a-4d65-beab-c3d4a73a85f8\"")
                buildConfigField("String", "ADCASH_FIND_PASSWORD_PID", "\"341911f8-18e4-4e8e-806d-3576d796e712\"")
            }

            getByName("release") {
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                signingConfig = signingConfigs.getByName("release")
                isMinifyEnabled = false
                isDebuggable = false

                // 운영 릴리즈 빌드에서는 Remote Config 값 사용
                buildConfigField("boolean", "FORCE_UPDATE_OVERRIDE", "false")
                buildConfigField("int", "MIN_SUPPORTED_VERSION_OVERRIDE", "0")
                buildConfigField("int", "UPDATE_TYPE_OVERRIDE", "0") // 0 = Remote Config 사용
                buildConfigField("String", "ADCASH_APP_ID", "\"9a39111d20e5467ba729ad5f5ddb9a0d\"")
                buildConfigField("String", "ADCASH_APP_SECRET", "\"191e43fe1a63477b\"")
                buildConfigField("String", "ADCASH_BOTTOM_BANNER_PID", "\"0db53989-fbf6-4b32-b546-e1417d16e718\"")
                buildConfigField("String", "ADCASH_FINISH_POPUP_PID", "\"4f6a61a3-b60a-4d65-beab-c3d4a73a85f8\"")
                buildConfigField("String", "ADCASH_FIND_PASSWORD_PID", "\"341911f8-18e4-4e8e-806d-3576d796e712\"")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // KMP Shared
    implementation(projects.shared)

    // Activity
    implementation(libs.androidx.activity)

    // Appcompat
    implementation(libs.androidx.appcompat)

    // Material
    implementation(libs.material)

    // Layout
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)

    // Flexbox
    implementation(libs.flexbox)

    // Glide
    implementation(libs.glide)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // photoView
    implementation(libs.photoView)

    // colorPicker
    implementation(libs.colorpicker)

    // threeTen
    implementation(libs.threeTen)

    // GoogleDrive
    implementation(libs.google.oauth.client.jetty)
    implementation(libs.play.services.auth)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.client.gson)
    implementation(libs.google.http.client.android)
    implementation(libs.google.api.services.drive)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config.ktx)

    // InApp Update
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Window Size
    implementation(libs.androidx.window)

    // AdCash
    implementation(libs.adcash.bom)
    implementation(libs.adcash.applovin)
    implementation(libs.adcash.adfit)
    implementation(libs.adcash.cauly)
    implementation(libs.adcash.facebook.audience)
    implementation(libs.adcash.pangle)
    implementation(libs.adcash.unity)
    implementation(libs.adcash.vungle)
    implementation(libs.adcash.mobwith)
    implementation(libs.adcash.admob)
    implementation(libs.adcash.nam)

    // Biometric
    implementation(libs.androidx.biometric)

    // AI
    implementation(libs.firebase.ai)

    // Firebase Crashlytics
    implementation(libs.firebase.crashlytics.ndk)
}