
package com.example.edgeviewer

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.*
import android.graphics.ImageFormat
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface

class Camera2Controller(
    private val ctx: Context,
    private val w: Int,
    private val h: Int,
    private val cb: (ByteArray, Int, Int)->Unit
) {
    private lateinit var handler: Handler

    @SuppressLint("MissingPermission")
    fun startCamera(surface: Surface) {

        val mgr = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val camId = mgr.cameraIdList[0]

        val thread = HandlerThread("cam-thread")
        thread.start()
        handler = Handler(thread.looper)

        val reader = ImageReader.newInstance(w, h, ImageFormat.YUV_420_888, 3)
        reader.setOnImageAvailableListener({
            val img = it.acquireLatestImage() ?: return@setOnImageAvailableListener
            val data = ImageUtil.yuv420ToNV21(img)
            cb(data, img.width, img.height)
            img.close()
        }, handler)

        mgr.openCamera(camId, object: CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) {
                val req = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                req.addTarget(surface)
                req.addTarget(reader.surface)

                device.createCaptureSession(
                    listOf(surface, reader.surface),
                    object: CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            session.setRepeatingRequest(req.build(), null, handler)
                        }
                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    }, handler
                )
            }
            override fun onDisconnected(d: CameraDevice) {}
            override fun onError(d: CameraDevice, err: Int) {}
        }, handler)
    }
}
