
cmake_minimum_required(VERSION 3.10)
project(native-lib)

add_library(native-lib SHARED
    src/main/jni/native_lib.cpp
)

# Basic OpenCV Android SDK integration scaffold.
# Place the OpenCV Android SDK under `app/opencv` (e.g. opencv-4.x.x-android-sdk/sdk/native)
# This CMake will attempt to include headers and import the prebuilt `libopencv_java4.so`

set(OPENCV_SDK_ROOT ${CMAKE_SOURCE_DIR}/opencv/sdk/native CACHE PATH "Path to OpenCV Android SDK native folder")

if(EXISTS ${OPENCV_SDK_ROOT}/jni/include)
    include_directories(${OPENCV_SDK_ROOT}/jni/include)
elseif(EXISTS ${OPENCV_SDK_ROOT}/include)
    include_directories(${OPENCV_SDK_ROOT}/include)
endif()

if(DEFINED ANDROID_ABI)
    # Try to locate prebuilt OpenCV shared lib for the current ABI
    set(OPENCV_LIB_PATH "${OPENCV_SDK_ROOT}/libs/${ANDROID_ABI}/libopencv_java4.so")
    if(NOT EXISTS ${OPENCV_LIB_PATH})
        # try alternative location used by some OpenCV packages
        set(OPENCV_LIB_PATH "${OPENCV_SDK_ROOT}/../libs/${ANDROID_ABI}/libopencv_java4.so")
    endif()
    if(EXISTS ${OPENCV_LIB_PATH})
        add_library(opencv_java4 SHARED IMPORTED)
        set_target_properties(opencv_java4 PROPERTIES IMPORTED_LOCATION "${OPENCV_LIB_PATH}")
        target_link_libraries(native-lib
            opencv_java4
            log
            jnigraphics
        )
    else()
        message(WARNING "OpenCV library not found for ABI '${ANDROID_ABI}' at: ${OPENCV_LIB_PATH}. Native build will link without OpenCV."
        )
        target_link_libraries(native-lib
            log
            jnigraphics
        )
    endif()
else()
    message(WARNING "ANDROID_ABI not defined; building native-lib without linking OpenCV."
    )
    target_link_libraries(native-lib
        log
        jnigraphics
    )
endif()
