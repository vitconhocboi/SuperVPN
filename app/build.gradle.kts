import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
}

android {
    namespace = "com.tici.vpn.proxy.master"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tici.vpn.proxy.master"
        minSdk = 27
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        externalNativeBuild {
            cmake {
                cppFlags("")
                arguments("-DANDROID_STL=c++_shared")
            }
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a","x86_64") // Specify architectures
        }

        val date = Date()
        val formattedDate = SimpleDateFormat("MM_dd").format(date)
//        val calendar = GregorianCalendar.getInstance()

//        val hour = calendar.get(Calendar.HOUR_OF_DAY)
//        val minute = calendar.get(Calendar.MINUTE)
        //val fix = 1
        setProperty("archivesBaseName", "SuperVPN_Code_" + versionCode + "_Name_" + versionName + "_${formattedDate}")

    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("boolean", "ADS_ON", "true")
            buildConfigField("boolean", "FIREBASE_DEBUG", "false")

            buildConfigField("String", "app_ads_id_new", "\"ca-app-pub-3940256099942544~3347511713\"")
            buildConfigField("String", "ads_native_id_new", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ads_banner_id_new", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "ads_full_without_video_new", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ads_full_with_video_new", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "app_ads_open_resume_id_new", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "ads_reward_id_new", "\"ca-app-pub-3940256099942544/5224354917\"")

            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = true
            buildConfigField("boolean", "ADS_ON", "true")
            buildConfigField("boolean", "FIREBASE_DEBUG", "false")

            buildConfigField("String", "app_ads_id_new", "\"ca-app-pub-4253116256630907~7625139262\"")
            buildConfigField("String", "ads_native_id_new", "\"ca-app-pub-4253116256630907/4890887109\"")
            buildConfigField("String", "ads_banner_id_new", "\"ca-app-pub-4253116256630907/3619247978\"")
            buildConfigField("String", "ads_full_without_video_new", "\"ca-app-pub-4253116256630907/2676064914\"")
            buildConfigField("String", "ads_full_with_video_new", "\"ca-app-pub-4253116256630907/2676064914\"")
            buildConfigField("String", "app_ads_open_resume_id_new", "\"ca-app-pub-4253116256630907/3257954190\"")
            buildConfigField("String", "ads_reward_id_new", "\"ca-app-pub-4253116256630907/6423594072\"")

            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    sourceSets {
        sourceSets.getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(libs.hilt.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(project(":BaseUI"))
    implementation(project(":libads"))
    implementation (libs.timber)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("com.android.billingclient:billing:6.2.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation(libs.androidx.room.ktx)
    implementation(libs.hilt.android)
    implementation (libs.play.services.ads)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.multidex)
    // Add zlib dependency for compression functionality
    implementation("com.jcraft:jzlib:1.1.3")
    implementation(group = ":tun2socks", name="tun2socks", ext = "aar")
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.okhttp)
    implementation("com.google.android.gms:play-services-ads-identifier:18.2.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    //retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation (libs.okhttp)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

hilt {
    enableExperimentalClasspathAggregation = true
}