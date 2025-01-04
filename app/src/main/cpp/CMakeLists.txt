# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.4.1)

# Set the required C++ standard
set(CMAKE_CXX_STANDARD 17)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("chess")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_executable(${CMAKE_PROJECT_NAME}
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        ${CMAKE_SOURCE_DIR}/nnue/features/half_ka_v2_hm.cpp
        ${CMAKE_SOURCE_DIR}/nnue/network.cpp
        ${CMAKE_SOURCE_DIR}/nnue/nnue_misc.cpp
        ${CMAKE_SOURCE_DIR}/syzygy/tbprobe.cpp
        ${CMAKE_SOURCE_DIR}/benchmark.cpp
        ${CMAKE_SOURCE_DIR}/bitboard.cpp
        ${CMAKE_SOURCE_DIR}/engine.cpp
        ${CMAKE_SOURCE_DIR}/evaluate.cpp
        ${CMAKE_SOURCE_DIR}/main.cpp
        ${CMAKE_SOURCE_DIR}/memory.cpp
        ${CMAKE_SOURCE_DIR}/misc.cpp
        ${CMAKE_SOURCE_DIR}/movegen.cpp
        ${CMAKE_SOURCE_DIR}/movepick.cpp
        ${CMAKE_SOURCE_DIR}/movepick.cpp
        ${CMAKE_SOURCE_DIR}/native-lib.cpp
        ${CMAKE_SOURCE_DIR}/position.cpp
        ${CMAKE_SOURCE_DIR}/score.cpp
        ${CMAKE_SOURCE_DIR}/search.cpp
        ${CMAKE_SOURCE_DIR}/thread.cpp
        ${CMAKE_SOURCE_DIR}/timeman.cpp
        ${CMAKE_SOURCE_DIR}/tt.cpp
        ${CMAKE_SOURCE_DIR}/tune.cpp
        ${CMAKE_SOURCE_DIR}/uci.cpp
        ${CMAKE_SOURCE_DIR}/ucioption.cpp
        )

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log)



# Include directories for header files like incbin/incbin.h
target_include_directories(${CMAKE_PROJECT_NAME}
        PRIVATE ${CMAKE_SOURCE_DIR}/nnue
        PRIVATE ${CMAKE_SOURCE_DIR}/incbin
        PRIVATE ${CMAKE_SOURCE_DIR}/syzygy
        PRIVATE ${CMAKE_SOURCE_DIR}
        )

# Add the directory where the .nnue files are located
set(NNUE_DIR "${CMAKE_SOURCE_DIR}/syzygy")
target_compile_definitions(chess PRIVATE NNUE_DIR="${NNUE_DIR}")