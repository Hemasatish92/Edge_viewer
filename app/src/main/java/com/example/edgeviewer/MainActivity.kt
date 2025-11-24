
package com.example.edgeviewer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var textureView: TextureView
    private lateinit var fpsText: TextView
    private lateinit var toggleBtn: Button

    private lateinit var camera: Camera2Controller
    private lateinit var renderer: GLRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Native library load removed to allow building without the native module.
        // If you restore the native build, re-add: System.loadLibrary("native-lib")
        setContentView(R.layout.activity_main)

        textureView = findViewById(R.id.textureView)
        fpsText = findViewById(R.id.fpsText)
        toggleBtn = findViewById(R.id.toggleBtn)

        toggleBtn.setOnClickListener {
            NativeBridge.setMode(1)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
            return
        }

        startSystem()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSystem()
            }
        }
    }

    private fun startSystem() {
        textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                val surface = Surface(st)

                renderer = GLRenderer(surface, w, h) { fps ->
                    runOnUiThread { fpsText.text = "FPS: %.1f".format(fps) }
                }
                renderer.start()

                camera = Camera2Controller(this@MainActivity, w, h) { data, width, height ->
                    NativeBridge.processFrame(data, width, height)
                    renderer.requestRender()
                }
                camera.startCamera(surface)
            }
            override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {}
            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean { if (::renderer.isInitialized) renderer.stop(); return true }
            override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
        }
    }
}
