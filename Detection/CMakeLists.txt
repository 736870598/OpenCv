# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

include_directories(src/main/cpp/include)
#编译源文件
file(GLOB my_source_path  src/main/cpp/*.cpp src/main/cpp/*.c)
add_library( detection-lib SHARED ${my_source_path} )

add_library( lib_opencv SHARED IMPORTED)
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

target_link_libraries( # Specifies the target library.
            detection-lib
            android
            log
            jnigraphics
            lib_opencv )