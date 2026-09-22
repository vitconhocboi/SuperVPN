import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.dagger.hilt)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
    alias(libs.plugins.android.room)
}

android {
    namespace = "com.tici.vpn.proxy.master"
    compileSdk = libs.versions.targetSdk.get().toInt()

    ndkVersion = "26.1.10909125"

    defaultConfig {
        applicationId = "com.tici.vpn.proxy.master"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 46
        versionName = "1.46"
//
//        resourceConfigurations.addAll(
//            listOf("en", "vi", "ar", "hi", "id", "ms", "ru", "pt", "fr", "de", "es", "ja", "ko")
//        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        externalNativeBuild {
            cmake {
                cppFlags("")
//                arguments("-DANDROID_STL=c++_shared")
            }
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
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
            isMinifyEnabled = true
            buildConfigField("boolean", "FIREBASE_DEBUG", "false")


            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = true
            buildConfigField("boolean", "FIREBASE_DEBUG", "false")

            buildConfigField("boolean", "LOG_ENABLED", "true")

            isShrinkResources = true
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // Define product flavors
    flavorDimensions.add("root")
    productFlavors {
        create("dev") {
          /*  applicationIdSuffix = ".dev"*/
        }

        create("prod") {

        }
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

    bundle {
        language {
            enableSplit = false
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.hilt.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
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
    implementation("com.android.billingclient:billing:7.1.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation(libs.androidx.room.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.multidex)

    // Add zlib dependency for compression functionality
    implementation("com.jcraft:jzlib:1.1.3")
    implementation(group = ":tun2socks", name="tun2socks", ext = "aar")
    implementation(libs.okhttp)
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    //Lottie Animation
    implementation(libs.lottie)

    //BlurView
    implementation(libs.blurview)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)
    implementation(libs.firebase.messaging)

    //retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation (libs.okhttp)
    implementation(libs.dotsindicator)
    //implementation(libs.circularprogressbar)

    // Moshi
    implementation (libs.moshi.kotlin)
    implementation (libs.moshi)
    implementation (libs.moshi.adapters)
    ksp(libs.moshi.kotlin.codegen)
    // Thêm dòng này để hỗ trợ desugaring Java 8+ API trên Android cũ
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(project(":BaseUI"))

    implementation(project(":core:analytics"))
    implementation(project(":core:baseui"))
    implementation(project(":core:config"))
    implementation(project(":core:preference"))
    implementation(project(":core:utilities"))
    implementation(project(":core:billing"))
    implementation(project(":core:dimens"))
    implementation(project(":core:rate"))
}
