
package com.example.edgeviewer

import android.opengl.EGL14
import android.opengl.GLES20
import android.view.Surface
import java.nio.ByteBuffer
import kotlin.concurrent.thread
import java.util.concurrent.atomic.AtomicBoolean
import java.nio.ByteOrder
import java.nio.FloatBuffer

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

        // Create simple shader program for textured quad
        val vs = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        val fs = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """.trimIndent()

        fun loadShader(type: Int, src: String): Int {
            val sh = GLES20.glCreateShader(type)
            GLES20.glShaderSource(sh, src)
            GLES20.glCompileShader(sh)
            return sh
        }

        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, loadShader(GLES20.GL_VERTEX_SHADER, vs))
        GLES20.glAttachShader(prog, loadShader(GLES20.GL_FRAGMENT_SHADER, fs))
        GLES20.glLinkProgram(prog)

        val verts = floatArrayOf(
            -1f, -1f,
             1f, -1f,
            -1f,  1f,
             1f,  1f
        )
        val texc = floatArrayOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
        )

        val vb: FloatBuffer = ByteBuffer.allocateDirect(verts.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vb.put(verts).position(0)
        val tb: FloatBuffer = ByteBuffer.allocateDirect(texc.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        tb.put(texc).position(0)

        val posLoc = GLES20.glGetAttribLocation(prog, "aPosition")
        val texLoc = GLES20.glGetAttribLocation(prog, "aTexCoord")
        val uniTex = GLES20.glGetUniformLocation(prog, "uTexture")

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
                GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, w, h, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bb)
            }

            GLES20.glViewport(0, 0, w, h)
            GLES20.glClearColor(0f, 0f, 0f, 1f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            GLES20.glUseProgram(prog)
            GLES20.glEnableVertexAttribArray(posLoc)
            GLES20.glEnableVertexAttribArray(texLoc)
            GLES20.glVertexAttribPointer(posLoc, 2, GLES20.GL_FLOAT, false, 0, vb)
            GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, 0, tb)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0])
            GLES20.glUniform1i(uniTex, 0)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            GLES20.glDisableVertexAttribArray(posLoc)
            GLES20.glDisableVertexAttribArray(texLoc)

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
