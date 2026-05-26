plugins {
    alias(libs.plugins.android.application)
    // Add Google Services plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.wolfitness"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wolfitness"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}




dependencies {
    // Keep your existing dependencies using version catalog
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Add Firebase dependencies directly (not through version catalog)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("androidx.work:work-runtime:2.8.1")
    //implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Add Picasso for image loading
    implementation("com.squareup.picasso:picasso:2.8")

    // Keep your existing test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}