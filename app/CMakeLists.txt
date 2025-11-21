
cmake_minimum_required(VERSION 3.10)
project(native-lib)

add_library(native-lib SHARED
    src/main/jni/native_lib.cpp
)

target_link_libraries(native-lib
    log
    jnigraphics
)
