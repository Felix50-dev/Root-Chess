import java.util.Properties

val localProperties = Properties()
localProperties.load(rootProject.file("local.properties").inputStream())

// Device-targeted builds: -PtargetAbis=arm64-v8a (comma-separated for several) limits
// both the packaged jniLibs and the Stockfish native build to those ABIs, shrinking the
// APK for sideloads. Omit the property for the full 4-ABI build.
val targetAbis = (project.findProperty("targetAbis") as? String)
    ?.split(",")?.map { it.trim() }
    ?: listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
    id("io.github.takahirom.roborazzi")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.chess"
    compileSdk = 35

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    defaultConfig {
        // TODO(owner): must match the applicationId registered in Play Console — Play
        // rejects com.example.*; change here (namespace stays com.example.chess, no
        // source refactor needed) before the first upload if you want a different id.
        applicationId = "dev.rootchess.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += targetAbis
        }

        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"${localProperties["GROQ_API_KEY"]}\""
        )

        // RevenueCat public Play API key (goog_...). Blank = billing disabled; the app
        // still builds and runs free-tier.
        buildConfigField(
            "String",
            "REVENUECAT_API_KEY",
            "\"${localProperties["REVENUECAT_API_KEY"] ?: ""}\""
        )

        // SIMULATE_PREMIUM=true in local.properties fakes an active premium entitlement
        // (debug builds only — ChessApplication gates it on BuildConfig.DEBUG).
        buildConfigField(
            "boolean",
            "SIMULATE_PREMIUM",
            (localProperties["SIMULATE_PREMIUM"]?.toString() ?: "false")
        )

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                targets.add("chess")  // This is the target from CMakeLists.txt
            }
        }
    }

    signingConfigs {
        // Keystore details live in local.properties (never committed):
        //   RELEASE_STORE_FILE=C:/path/to/upload-keystore.jks
        //   RELEASE_STORE_PASSWORD=...
        //   RELEASE_KEY_ALIAS=...
        //   RELEASE_KEY_PASSWORD=...
        create("release") {
            val storePath = localProperties["RELEASE_STORE_FILE"]?.toString()
            if (storePath != null) {
                storeFile = file(storePath)
                storePassword = localProperties["RELEASE_STORE_PASSWORD"]?.toString()
                keyAlias = localProperties["RELEASE_KEY_ALIAS"]?.toString()
                keyPassword = localProperties["RELEASE_KEY_PASSWORD"]?.toString()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // No keystore configured (CI, fresh checkout) → fall back to debug signing
            // so assembleRelease still builds; a Play upload requires the real keystore.
            signingConfig = if (localProperties["RELEASE_STORE_FILE"] != null)
                signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")  // Path to your CMakeLists.txt
        }
    }

    testOptions {
        // GamePlay logs via android.util.Log on nearly every step — let JVM unit tests
        // run through it with Log.* returning defaults instead of throwing "not mocked".
        unitTests.isReturnDefaultValues = true
        // Required by Robolectric/Roborazzi so screenshot tests can resolve app resources
        // (themes, drawables) when rendering composables on the host JVM.
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.jvmArgs("-Dnet.bytebuddy.experimental=true")
        }
    }

    sourceSets {
        getByName("main").jniLibs.srcDir("libs")  // Directory for native .so files
        // AGP only auto-packages add_library(SHARED) outputs, not the add_executable(chess)
        // Stockfish binary CMakeLists.txt builds — see packageChessNativeExecutables below.
        // Variant-specific dirs so debug and release each package their own build of the
        // binary (previously only debug was packaged; release shipped WITHOUT Stockfish).
        getByName("debug").jniLibs.srcDir(layout.buildDirectory.dir("generated/jniLibs/Debug"))
        getByName("release").jniLibs.srcDir(layout.buildDirectory.dir("generated/jniLibs/Release"))
    }
}

val chessAbis = targetAbis

// Copies the CMake-built 'chess' (Stockfish) executable for each ABI into a generated
// jniLibs dir as lib_chess.so. AGP only auto-packages add_library(SHARED) outputs, not
// add_executable outputs, so this step is required for every ABI and EVERY variant —
// the original debug-only task meant release builds shipped without Stockfish at all.
// cmakeTypes: AGP names the native build type Debug for debug variants, but Release or
// RelWithDebInfo (AGP-version-dependent) for release — match both.
fun registerPackageChessTask(variant: String, cmakeTypes: List<String>) =
    tasks.register("packageChessNativeExecutables$variant") {
        dependsOn("externalNativeBuild$variant")

        // Without this, Gradle has no way to know -PtargetAbis changed between
        // invocations and treats a prior run's output dir as still up-to-date, silently
        // skipping the copy below and leaving lib_chess.so missing (or stale) for the
        // ABI actually being built this time.
        inputs.property("chessAbis", chessAbis)

        val outputDir = layout.buildDirectory.dir("generated/jniLibs/$variant")
        outputs.dir(outputDir)

        doLast {
            chessAbis.forEach { abi ->
                val built = fileTree(layout.buildDirectory.dir("intermediates/cxx")) {
                    include(cmakeTypes.map { "**/$it/**/obj/$abi/chess" })
                }.files.maxByOrNull { it.lastModified() }
                    ?: error("No built 'chess' binary found for $abi ($variant)")
                val dest = outputDir.get().dir(abi).asFile.also { it.mkdirs() }
                built.copyTo(File(dest, "lib_chess.so"), overwrite = true)
            }
        }
    }

val packageChessDebug = registerPackageChessTask("Debug", listOf("Debug"))
val packageChessRelease = registerPackageChessTask("Release", listOf("Release", "RelWithDebInfo"))

// Hooked into merge<Variant>JniLibFolders (the task that actually reads the jniLibs source
// set directories), not preBuild — the CMake tasks themselves depend on preBuild via
// pre<Variant>Build, so depending on this task from preBuild would create a circular
// dependency. AGP registers its variant tasks lazily, so match-and-configure rather
// than tasks.named(...), which would fail if evaluated before the task exists.
tasks.matching { it.name == "mergeDebugJniLibFolders" }.configureEach {
    dependsOn(packageChessDebug)
}
tasks.matching { it.name == "mergeReleaseJniLibFolders" }.configureEach {
    dependsOn(packageChessRelease)
}

dependencies {

    implementation(project(":payments"))
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation ("androidx.navigation:navigation-compose:2.8.9")
    implementation ("com.google.accompanist:accompanist-permissions:0.30.0")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("com.airbnb.android:lottie-compose:6.4.0")
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")

    // Room - local persistence for puzzles
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    testImplementation ("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    // PreferenceDataStoreFactory on the JVM for AiReviewQuotaManager tests
    testImplementation("androidx.datastore:datastore-preferences-core:1.1.1")

    // Roborazzi — host-JVM Compose screenshot testing (Robolectric-based, no device needed)
    testImplementation(platform("androidx.compose:compose-bom:2025.02.00"))
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("org.robolectric:robolectric:4.17")
    // Pinned to 1.60.0 to match the Roborazzi Gradle plugin version — see root
    // build.gradle.kts comment for why (Kotlin 2.0.20 compatibility).
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.60.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.60.0")
    implementation(fileTree("libs") {
        include("*.jar", "*.so")
    })
}
