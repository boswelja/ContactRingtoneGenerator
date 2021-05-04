object Libraries {
    object Versions {
        const val appCompat = "1.2.0-rc01"
        const val constraintLayout = "2.0.0-beta8"
        const val fragment = "1.3.0-alpha06"
        const val lifecycle = "2.3.0-alpha05"
        const val navigation = "2.3.0"
        const val preference = "1.1.1"

        const val material = "1.3.0-alpha01"

        const val timber = "4.7.1"

        const val ffmpeg = "4.4"
    }

    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val fragment = "androidx.fragment:fragment-ktx:${Versions.fragment}"

    const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    const val navigationUi = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"

    const val preference = "androidx.preference:preference-ktx:${Versions.preference}"

    const val googleMaterial = "com.google.android.material:material:${Versions.material}"

    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    const val ffmpeg = "com.arthenica:ffmpeg-kit-audio:${Versions.ffmpeg}"
}

object DebugLibraries {
    object Versions {
        const val fragment = Libraries.Versions.fragment
    }
    const val fragment = "androidx.fragment:fragment-testing:${Versions.fragment}"
}

object AndroidTestLibraries {
    object Versions {
        const val navigation = Libraries.Versions.navigation
    }

    const val navigation = "androidx.navigation:navigation-testing:${Versions.navigation}"
}
