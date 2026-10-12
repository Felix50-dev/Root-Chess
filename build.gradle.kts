// Top-level build file where you can add configuration options common to all sub-projects/modules.
 plugins {
    id("com.android.application") version "8.8.0" apply false
    id("com.android.library") version "8.8.0" apply false
    id("com.android.test") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    // Generates & measures Baseline Profiles (see :baselineprofile module). Applied to
    // this module and to :app, which consumes the generated profile.
    id("androidx.baselineprofile") version "1.3.4" apply false
    // Pinned to 1.60.0 (not latest): 1.61.0+ is built with Kotlin 2.3.21, whose module
    // metadata binary version this project's Kotlin 2.0.20 compiler cannot read
    // ("Module was compiled with an incompatible version of Kotlin"). 1.60.0 is the
    // newest release still built with Kotlin 2.0.21 (compatible with our 2.0.20).
    id("io.github.takahirom.roborazzi") version "1.60.0" apply false
    alias(libs.plugins.compose.compiler) apply false
}