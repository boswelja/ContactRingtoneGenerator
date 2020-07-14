// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
        maven(url ="https://dl.bintray.com/kotlin/kotlin-eap")
    }
    dependencies {
        classpath(BuildPlugins.androidGradlePlugin)
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath(BuildPlugins.safeArgsGradlePlugin)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url ="https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

tasks.register("clean").configure {
    delete("build")
}