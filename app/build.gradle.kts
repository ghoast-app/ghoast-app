plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Firebase
}

android {

    namespace = "com.ghoast"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ghoast"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "MAPS_API_KEY", "\"${properties["MAPS_API_KEY"]}\"")

    }
    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Compose
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.play.services.location)
    implementation(libs.androidx.compose.material3)
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Firebase Auth
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")

    // Firebase BOM (διαχειρίζεται εκδόσεις Firebase)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Google Play Services (για google-services.json)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // JUnit για unit tests
    testImplementation("junit:junit:4.13.2")

    // AndroidX Test για instrumented tests (αν χρειαστείς)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    // ✅ Coil για φόρτωση εικόνων
    implementation("io.coil-kt:coil-compose:2.5.0")

    dependencies {
        implementation ("com.google.android.gms:play-services-maps:18.1.0")
        implementation ("com.google.maps.android:maps-compose:2.11.4")
    }
    implementation("com.google.maps.android:maps-compose:4.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:2.11.4")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.0")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("androidx.compose.ui:ui-text:1.5.0") // ή παρόμοια
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:maps-compose:2.11.4")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation ("com.google.firebase:firebase-messaging")
    implementation ("com.google.firebase:firebase-functions-ktx:20.4.0")
    implementation ("com.android.billingclient:billing:6.1.0")
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.32.0")
    implementation("androidx.compose.foundation:foundation:1.6.0")

    }
