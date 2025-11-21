
package com.example.edgeviewer

object NativeBridge {
    // Stubbed Kotlin implementations so the app can run without the native library.
    // If you later enable the native build, you can replace these with `external` JNI bindings.

    @Volatile
    private var modeInternal: Int = 0

    fun processFrame(data: ByteArray, width: Int, height: Int) {
        // No-op: real processing is performed by native code when available.
    }

    fun getProcessedBuffer(): ByteArray {
        // Return an empty buffer so renderer has nothing to draw until native is available.
        return ByteArray(0)
    }

    fun setMode(mode: Int) {
        modeInternal = mode
    }
}
