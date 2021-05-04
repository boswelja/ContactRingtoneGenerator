plugins {
    id(BuildPlugins.androidApplication)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinAndroidExtensions)
    id(BuildPlugins.kotlinKapt)
    id(BuildPlugins.safeArgs)
    id("com.diffplug.spotless") version "5.1.0"
}

android {
    compileSdk = AndroidSdk.compile

    defaultConfig {
        applicationId = "com.boswelja.contactringtonegenerator"
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target
        versionCode = 9
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.viewBinding = true
    buildFeatures.dataBinding = true
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(Libraries.kotlinStdlib)
    implementation(kotlin("reflect"))

    implementation(Libraries.coreKtx)
    implementation(Libraries.appCompat)
    implementation(Libraries.constraintLayout)
    implementation(Libraries.fragment)
    implementation(Libraries.navigationFragment)
    implementation(Libraries.navigationUi)
    implementation(Libraries.preference)
    implementation(Libraries.lifecycleViewModel)
    implementation(Libraries.LifecycleCommon)
    implementation(Libraries.LifecycleLiveData)

    implementation(Libraries.googleMaterial)

    implementation(Libraries.timber)

    implementation(Libraries.ffmpeg)

    debugImplementation(DebugLibraries.fragment)

    testImplementation(TestLibraries.arch)
    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.mockK)

    androidTestImplementation(AndroidTestLibraries.mockK)
    androidTestImplementation(AndroidTestLibraries.navigation)
    androidTestImplementation(AndroidTestLibraries.junit)
    androidTestImplementation(AndroidTestLibraries.rules)
    androidTestImplementation(AndroidTestLibraries.espresso)
}

spotless {
    kotlin {
        target("**/*.kt")

        ktlint("0.37.2")
        endWithNewline()
    }
    kotlinGradle {
        ktlint("0.37.2")
        endWithNewline()
    }
    format("xml") {
        target("**/*.xml")

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}
