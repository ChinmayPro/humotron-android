import com.android.build.gradle.ProguardFiles.getDefaultProguardFile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    //id("com.google.gms.google-services")
}

android {
    namespace = "com.humotron.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.humotron.app"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
        }
    }
}

dependencies {

    // -------------------- Android Core ------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.swiperefreshlayout)

    // -------------------- Navigation --------------------
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // -------------------- UI Components -----------------
    implementation(libs.androidx.recyclerview)
    implementation(libs.switchbutton.library)
    implementation(libs.number.picker)
    implementation(libs.wheelview.xd)
    implementation(libs.cardstackview)
    implementation(libs.shimmer)

    // -------------------- Charts --------------------
    implementation(libs.mpandroidchart)
    implementation(libs.mpandroidchart)

    // -------------------- Image Loading ----------------
    implementation(libs.glide)

    // -------------------- Browser --------------------
    implementation(libs.androidx.browser)

    // -------------------- Authentication ---------------
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // -------------------- Google API Client / Gmail API -
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.gmail)
    implementation(libs.google.http.client.android)
    implementation(libs.google.http.client.gson)

    // -------------------- OTP --------------------
    implementation(libs.kevinschildhorn.otpview)

    // -------------------- Room Database ----------------
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // -------------------- SQLCipher --------------------
    //implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation(libs.sqlcipher.android)

    // -------------------- Networking --------------------
    implementation(libs.retrofit2.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // -------------------- Dependency Injection ----------
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // -------------------- Permissions --------------------
    implementation(libs.permissionx)

    // -------------------- Lifecycle --------------------
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // -------------------- Internal Modules ---------------
    implementation(project(":ecgAlgo"))
    implementation(project(":oemAuth"))
    implementation(project(":sdkAAR"))
    implementation(project(":sleepAlgo"))
    implementation(project(":libraryDiscreteScrollView"))
    //Band
    implementation(project(":blesdk_2208"))

    // -------------------- Debug Tools --------------------
    debugImplementation(libs.pluto)
    debugImplementation(libs.network)
    debugImplementation(libs.logger)
    debugImplementation(libs.exceptions)

    // -------------------- Testing --------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // -------------------- Firebase (Commented) ------------
    //implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    //implementation("com.google.firebase:firebase-auth")

    // -------------------- Others --------------------
    implementation(libs.lottie)

    implementation("com.google.android.flexbox:flexbox:3.0.0")
}