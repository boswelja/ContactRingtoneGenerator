const val kotlinVersion = "1.4-M3"

object BuildPlugins {
    object Versions {
        const val buildToolsVersion = "4.2.0-alpha04"
        const val safeArgsVersion = Libraries.Versions.navigation
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.buildToolsVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val safeArgsGradlePlugin = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.safeArgsVersion}"

    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val kotlinKapt = "kotlin-kapt"
    const val safeArgs = "androidx.navigation.safeargs.kotlin"
}

object AndroidSdk {
    const val min = 23
    const val compile = 30
    const val target = compile
}

object Libraries {
    object Versions {
        const val coreKtx = "1.3.0"
        const val navigation = "2.3.0"
        const val fragment = "1.2.5"
        const val lifecycle = "2.2.0"
        const val appCompat = "1.1.0"
        const val constraintLayout = "1.1.3"
        const val preference = "1.1.1"

        const val material = "1.3.0-alpha01"

        const val timber = "4.7.1"

        const val ffmpeg = "4.3.1-LTS"
    }

    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"

    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val fragment = "androidx.fragment:fragment-ktx:${Versions.fragment}"

    const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    const val navigationUi = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"

    const val preference = "androidx.preference:preference-ktx:${Versions.preference}"

    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val LifecycleCommon = "androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}"
    const val LifecycleLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"

    const val googleMaterial = "com.google.android.material:material:${Versions.material}"

    const val timber = "com.jakewharton.timber:${Versions.timber}"

    const val ffmpeg = "com.arthenica:mobile-ffmpeg-audio:${Versions.ffmpeg}"
}

object DebugLibraries {
    object Versions {
        const val fragment = Libraries.Versions.fragment
    }
    const val fragment = "androidx.fragment:fragment-testing:${Versions.fragment}"
}

object TestLibraries {
    object Versions {
        const val arch = "2.1.0"
        const val junit = "4.13"
        const val mockK = "1.10.0"
    }

    const val arch = "androidx.arch.core:core-testing:${Versions.arch}"
    const val junit = "junit:junit:${Versions.junit}"
    const val mockK = "io.mockk:mockk:${Versions.mockK}"
}

object AndroidTestLibraries {
    object Versions {
        const val awaitility = "4.0.3"
        const val mockK = TestLibraries.Versions.mockK
        const val navigation = Libraries.Versions.navigation
        const val junit = "1.1.1"
        const val rules = "1.2.0"
        const val espresso = "3.2.0"
    }

    const val awaitility = "org.awaitility:awaitility-kotlin:${Versions.awaitility}"
    const val mockK = "io.mockk:mockk-android:${Versions.mockK}"
    const val navigation = "androidx.navigation:navigation-testing:${Versions.navigation}"
    const val junit = "androidx.test.ext:junit:${Versions.junit}"
    const val rules = "androidx.test:rules:${Versions.rules}"
    const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
}