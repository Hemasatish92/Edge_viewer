
package com.example.edgeviewer

object NativeBridge {
    // Pure-Kotlin image processing fallback. Accepts NV21 frames (Y + interleaved VU),
    // computes a simple Sobel edge map, and exposes an RGBA byte buffer for rendering.

    @Volatile
    private var modeInternal: Int = 1

    private val lock = Any()
    private var outBuf: ByteArray = ByteArray(0)
    private var outW = 0
    private var outH = 0

    fun setMode(mode: Int) {
        modeInternal = mode
    }

    fun processFrame(data: ByteArray, width: Int, height: Int) {
        // Quick sanity checks
        if (data.isEmpty() || width <= 0 || height <= 0) return

        // NV21: Y plane first (w*h), then interleaved VU (w*h/2)
        val ySize = width * height
        if (data.size < ySize) return

        // Extract Y (grayscale) plane
        val y = ByteArray(ySize)
        System.arraycopy(data, 0, y, 0, ySize)

        // If mode == 1, apply edge detection; else convert to RGBA passthrough
        val rgba = ByteArray(width * height * 4)

        if (modeInternal == 1) {
            // Simple Sobel operator on Y plane
            val gx = IntArray(width * height)
            val gy = IntArray(width * height)

            for (yy in 1 until height - 1) {
                val yym1 = yy - 1
                val yyp1 = yy + 1
                for (xx in 1 until width - 1) {
                    val i = yy * width + xx

                    val v00 = y[yym1 * width + (xx - 1)].toInt() and 0xFF
                    val v01 = y[yym1 * width + xx].toInt() and 0xFF
                    val v02 = y[yym1 * width + (xx + 1)].toInt() and 0xFF

                    val v10 = y[yy * width + (xx - 1)].toInt() and 0xFF
                    val v11 = y[yy * width + xx].toInt() and 0xFF
                    val v12 = y[yy * width + (xx + 1)].toInt() and 0xFF

                    val v20 = y[yyp1 * width + (xx - 1)].toInt() and 0xFF
                    val v21 = y[yyp1 * width + xx].toInt() and 0xFF
                    val v22 = y[yyp1 * width + (xx + 1)].toInt() and 0xFF

                    val sx = -v00 + v02 - 2 * v10 + 2 * v12 - v20 + v22
                    val sy = -v00 - 2 * v01 - v02 + v20 + 2 * v21 + v22

                    gx[i] = sx
                    gy[i] = sy
                }
            }

            for (yy in 0 until height) {
                for (xx in 0 until width) {
                    val i = yy * width + xx
                    val mag = if (xx == 0 || yy == 0 || xx == width - 1 || yy == height - 1) {
                        0
                    } else {
                        val sx = gx[i]
                        val sy = gy[i]
                        var m = Math.hypot(sx.toDouble(), sy.toDouble()).toInt()
                        if (m > 255) m = 255
                        m
                    }

                    val base = i * 4
                    rgba[base] = mag.toByte()
                    rgba[base + 1] = mag.toByte()
                    rgba[base + 2] = mag.toByte()
                    rgba[base + 3] = 0xFF.toByte()
                }
            }

        } else {
            // Passthrough: expand Y to RGBA
            for (i in 0 until ySize) {
                val v = y[i]
                val base = i * 4
                rgba[base] = v
                rgba[base + 1] = v
                rgba[base + 2] = v
                rgba[base + 3] = 0xFF.toByte()
            }
        }

        synchronized(lock) {
            outBuf = rgba
            outW = width
            outH = height
        }
    }

    fun getProcessedBuffer(): ByteArray {
        synchronized(lock) {
            return if (outBuf.isNotEmpty()) outBuf.copyOf() else ByteArray(0)
        }
    }
}
