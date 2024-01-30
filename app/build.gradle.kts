import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.vnteam.dronecontroller"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vnteam.dronecontroller"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a")
        }
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
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // MSDK related so library
    packaging {
        resources.pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
        resources.pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
    }

    // MSDK related so library
    packaging {
        jniLibs.keepDebugSymbols.add("*/*/libconstants.so")
        jniLibs.keepDebugSymbols.add("*/*/libdji_innertools.so")
        jniLibs.keepDebugSymbols.add("*/*/libdjibase.so")
        jniLibs.keepDebugSymbols.add("*/*/libDJICSDKCommon.so")
        jniLibs.keepDebugSymbols.add("*/*/libDJIFlySafeCore-CSDK.so")
        jniLibs.keepDebugSymbols.add("*/*/libdjifs_jni-CSDK.so")
        jniLibs.keepDebugSymbols.add("*/*/libDJIRegister.so")
        jniLibs.keepDebugSymbols.add("*/*/libdjisdk_jni.so")
        jniLibs.keepDebugSymbols.add("*/*/libDJIUpgradeCore.so")
        jniLibs.keepDebugSymbols.add("*/*/libDJIUpgradeJNI.so")
        jniLibs.keepDebugSymbols.add("*/*/libDJIWaypointV2Core-CSDK.so")
        jniLibs.keepDebugSymbols.add("*/*/libdjiwpv2-CSDK.so")
        jniLibs.keepDebugSymbols.add("*/*/libFlightRecordEngine.so")
        jniLibs.keepDebugSymbols.add("*/*/libvideo-framing.so")
        jniLibs.keepDebugSymbols.add("*/*/libwaes.so")
        jniLibs.keepDebugSymbols.add("*/*/libagora-rtsa-sdk.so")
        jniLibs.keepDebugSymbols.add("*/*/libc++.so")
        jniLibs.keepDebugSymbols.add("*/*/libc++_shared.so")
        jniLibs.keepDebugSymbols.add("*/*/libmrtc_28181.so")
        jniLibs.keepDebugSymbols.add("*/*/libmrtc_agora.so")
        jniLibs.keepDebugSymbols.add("*/*/libmrtc_core.so")
        jniLibs.keepDebugSymbols.add("*/*/libmrtc_core_jni.so")
        jniLibs.keepDebugSymbols.add("*/*/libmrtc_data.so")
        jniLibs.keepDebugSymbols.add("*/*/libmrtc_log.so")
        jniLibs.keepDebugSymbols.add("*/*/libmrtc_onvif.so")
        jniLibs.keepDebugSymbols.add("*/*/libmrtc_rtmp.so")
        jniLibs.keepDebugSymbols.add("*/*/libmrtc_rtsp.so")
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:+")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // DJI SDK
    implementation("com.dji:dji-sdk-v5-aircraft:5.7.1")
    compileOnly("com.dji:dji-sdk-v5-aircraft-provided:5.7.1")
    runtimeOnly("com.dji:dji-sdk-v5-networkImp:5.7.1")
    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    //Camera
    implementation("androidx.camera:camera-view:1.3.1")
}