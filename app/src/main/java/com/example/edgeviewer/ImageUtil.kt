
package com.example.edgeviewer

import android.media.Image

object ImageUtil {
    fun yuv420ToNV21(img: Image): ByteArray {
        val y = img.planes[0].buffer
        val u = img.planes[1].buffer
        val v = img.planes[2].buffer

        val ySize = y.remaining()
        val uSize = u.remaining()
        val vSize = v.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        y.get(nv21, 0, ySize)

        var pos = ySize
        val vBytes = ByteArray(vSize)
        val uBytes = ByteArray(uSize)
        v.get(vBytes)
        u.get(uBytes)

        for (i in vBytes.indices) {
            nv21[pos++] = vBytes[i]
            nv21[pos++] = uBytes[i]
        }
        return nv21
    }
}
