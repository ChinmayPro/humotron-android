import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.compose)
    id("androidx.navigation.safeargs.kotlin")
    //id("com.google.gms.google-services")
}

android {
    namespace = "com.humotron.app"
    compileSdk = 36

    val googleProperties = Properties()
    val googlePropertiesFile = rootProject.file("googlekey.properties")
    if (googlePropertiesFile.exists()) {
        googleProperties.load(googlePropertiesFile.inputStream())
    }

    val googleClientId = googleProperties.getProperty("GOOGLE_CLIENT_ID") ?: ""
    val googleClientSecret = googleProperties.getProperty("GOOGLE_CLIENT_SECRET") ?: ""

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.humotron.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
        buildConfigField("String", "GOOGLE_CLIENT_SECRET", "\"$googleClientSecret\"")
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

    lint {
        disable += "NullSafeMutableLiveData"
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
        compose = true
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
    applicationVariants.all {
        outputs.all {
            val appName = rootProject.name
            val dateTime = SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(Date())

            val outputFileName =
                "${appName}_v${versionName}(${versionCode})_${buildType.name}_${dateTime}.apk"
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                outputFileName
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
    implementation(libs.ccp)

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
    debugImplementation(libs.bundle.core)
    releaseImplementation(libs.pluto.no.op)
    releaseImplementation(libs.bundle.core.no.op)
    implementation(libs.timber)

    // -------------------- Testing --------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // -------------------- Firebase (Commented) ------------
    //implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    //implementation("com.google.firebase:firebase-auth")

    // -------------------- Others --------------------
    implementation(libs.lottie)
    implementation(libs.play.billing.ktx)

    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.stripe:stripe-android:21.0.0")
    
    // Minimal Compose dependencies required by Stripe SDK
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")

}