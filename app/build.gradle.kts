plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.navigation.safeargs.kotlin)
}

android {
    namespace = "com.littleapp.todonote"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.littleapp.todonote"
        minSdk = 24
        targetSdk = 37
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
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference.ktx)           //Shared Preference
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //Layout
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    //Lifecycle + ViewModel & LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.common.java8)
    //Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    //Room
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    //Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    //Dagger - Hilt
    implementation(libs.hilt.android)     //Dagger Hilt
    ksp(libs.hilt.android.compiler)      //Dagger Hilt Compiler
    //Other's
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.jakewharton.timber)
    ksp(libs.kotlin.metadata.jvm)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}