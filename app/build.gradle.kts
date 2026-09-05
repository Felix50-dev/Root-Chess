import java.util.Properties

val localProperties = Properties()
localProperties.load(rootProject.file("local.properties").inputStream())

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
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
        applicationId = "com.example.chess"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"${localProperties["GROQ_API_KEY"]}\""
        )

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                targets.add("chess")  // This is the target from CMakeLists.txt
            }
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

    sourceSets {
        getByName("main").jniLibs.srcDir("libs")  // Directory for native .so files
        // AGP only auto-packages add_library(SHARED) outputs, not the add_executable(chess)
        // Stockfish binary CMakeLists.txt builds — see packageChessNativeExecutables below.
        getByName("main").jniLibs.srcDir(layout.buildDirectory.dir("generated/jniLibs"))
    }
}

val chessAbis = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

// Copies the CMake-built 'chess' (Stockfish) executable for each ABI into a generated
// jniLibs dir as lib_chess.so. AGP only auto-packages add_library(SHARED) outputs, not
// add_executable outputs, so this step is required for every ABI — previously done
// manually and committed to git for only 2 of the 4 ABIs (and had already gone stale).
val packageChessNativeExecutables = tasks.register("packageChessNativeExecutables") {
    // AGP 8.x uses externalNativeBuildDebug to build all ABIs for the debug variant
    dependsOn("externalNativeBuildDebug")

    val outputDir = layout.buildDirectory.dir("generated/jniLibs")
    outputs.dir(outputDir)

    doLast {
        chessAbis.forEach { abi ->
            val built = fileTree(layout.buildDirectory.dir("intermediates/cxx")) {
                include("**/Debug/**/obj/$abi/chess")
            }.singleFile
            val dest = outputDir.get().dir(abi).asFile.also { it.mkdirs() }
            built.copyTo(File(dest, "lib_chess.so"), overwrite = true)
        }
    }
}

// Hooked into mergeDebugJniLibFolders (the task that actually reads the jniLibs source
// set directories), not preBuild — the CMake tasks themselves depend on preBuild via
// preDebugBuild, so depending on this task from preBuild would create a circular
// dependency. AGP registers its variant tasks lazily, so match-and-configure rather
// than tasks.named(...), which would fail if evaluated before the task exists.
tasks.matching { it.name == "mergeDebugJniLibFolders" }.configureEach {
    dependsOn(packageChessNativeExecutables)
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
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
    implementation(fileTree("libs") {
        include("*.jar", "*.so")
    })
}
