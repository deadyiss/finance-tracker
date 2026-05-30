// Top-level build file. Plugin versions are declared here and applied per-module.
// NOTE: These versions are a coherent, recent set. If Android Studio's Gradle Sync
// reports a newer/required version, update the numbers here and in app/build.gradle.kts.
plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}
