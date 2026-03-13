plugins {
    id("com.android.library")
}

android {
    compileSdk = 35
    namespace = "com.yarolegovich.discretescrollview"

    defaultConfig {
        minSdk = 14
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.annotation)
    debugImplementation(libs.androidx.appcompat)
}