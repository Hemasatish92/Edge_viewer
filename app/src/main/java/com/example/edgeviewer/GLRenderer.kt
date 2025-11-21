
package com.example.edgeviewer

import android.opengl.EGL14
import android.opengl.GLES20
import android.view.Surface
import java.nio.ByteBuffer
import kotlin.concurrent.thread
import java.util.concurrent.atomic.AtomicBoolean

class GLRenderer(
    private val surface: Surface,
    private val w: Int,
    private val h: Int,
    private val fpsCB: (Float)->Unit
) {
    private val running = AtomicBoolean(false)

    fun start() {
        running.set(true)
        thread { loop() }
    }

    fun stop() { running.set(false) }

    fun requestRender() {}

    private fun loop() {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        EGL14.eglInitialize(display, null, 0, null, 0)

        val attrib = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<android.opengl.EGLConfig>(1)
        val num = IntArray(1)
        EGL14.eglChooseConfig(display, attrib, 0, configs, 0, 1, num, 0)

        val ctxAttrib = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        val ctx = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttrib, 0)

        val sur = EGL14.eglCreateWindowSurface(display, configs[0], surface, intArrayOf(EGL14.EGL_NONE), 0)
        EGL14.eglMakeCurrent(display, sur, sur, ctx)

        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)

        var frames = 0
        var acc = 0L
        var last = System.currentTimeMillis()

        while (running.get()) {
            val buf = NativeBridge.getProcessedBuffer()
            if (buf.isNotEmpty()) {
                val bb = ByteBuffer.allocateDirect(buf.size)
                bb.put(buf)
                bb.position(0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0])
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, w, h, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bb)
            }

            GLES20.glViewport(0, 0, w, h)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            EGL14.eglSwapBuffers(display, sur)

            val now = System.currentTimeMillis()
            acc += (now - last)
            frames++
            last = now

            if (acc >= 1000) {
                fpsCB(frames * 1000f / acc)
                frames = 0
                acc = 0
            }
        }
    }
}
