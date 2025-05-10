plugins {
    id("com.android.application")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.ecs.netflix"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.ecs.netflix"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    // Material Design ve AppCompat kütüphaneleri
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)


    // Firebase Bağımlılıkları
    implementation(platform(libs.firebase.bom.v3300))

    // Firebase modülleri, BOM tarafından yönetilecek
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.firestore)
    implementation(libs.google.firebase.database)


    implementation (libs.glide)
    implementation(libs.google.firebase.storage)
    annotationProcessor (libs.compiler)


    // Kotlin Navigation Bağımlılıkları
    val nav_version = "2.7.7"
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Google Play Services for Authentication
    implementation(libs.play.services.auth)

    // Picasso for image loading
    implementation(libs.picasso.v28)

    // Test Bağımlılıkları
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:11.1.0")



}
