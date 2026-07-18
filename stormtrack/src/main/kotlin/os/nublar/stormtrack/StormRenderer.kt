package os.nublar.stormtrack

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

/**
 * Offscreen OpenGL renderer for the StormTrack proof-of-concept: a low-poly
 * island with a rotating, stacked-cloud storm column. Renders into an FBO and
 * hands back BGRA pixel rows for Compose to display as an ImageBitmap.
 *
 * Must be used on a thread where [GlContext.makeCurrent] has been called.
 */
class StormRenderer(private val width: Int, private val height: Int) {

    private val fbo: Int
    private val colorTex: Int
    private val depthRbo: Int
    private val program: Int
    private val discVao: Int
    private val discVbo: Int
    private val discVertexCount: Int
    private val uMvp: Int
    private val uColor: Int
    private val pixelBuffer: ByteBuffer = BufferUtils.createByteBuffer(width * height * 4)

    init {
        fbo = glGenFramebuffers()
        colorTex = glGenTextures()
        depthRbo = glGenRenderbuffers()

        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glBindTexture(GL_TEXTURE_2D, colorTex)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTex, 0)

        glBindRenderbuffer(GL_RENDERBUFFER, depthRbo)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRbo)

        check(glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE) {
            "StormTrack FBO is incomplete"
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        program = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        uMvp = glGetUniformLocation(program, "uMvp")
        uColor = glGetUniformLocation(program, "uColor")

        val (vao, vbo, count) = buildDiscGeometry(segments = 64)
        discVao = vao
        discVbo = vbo
        discVertexCount = count

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    /**
     * Renders one frame at [timeSeconds] and returns BGRA pixel data,
     * vertically flipped so it matches ImageBitmap's top-left origin.
     */
    fun renderFrame(timeSeconds: Float): ByteArray {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glViewport(0, 0, width, height)
        glClearColor(0.03f, 0.06f, 0.07f, 1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glUseProgram(program)

        val aspect = width.toFloat() / height.toFloat()
        val projection = Mat4.perspective(Math.toRadians(45.0).toFloat(), aspect, 0.1f, 100f)
        val camAngle = timeSeconds * 0.18f
        val camDist = 2.6f
        val eyeX = kotlin.math.sin(camAngle) * camDist
        val eyeZ = kotlin.math.cos(camAngle) * camDist
        val view = Mat4.lookAt(eyeX, 1.5f, eyeZ, 0f, 0.15f, 0f, 0f, 1f, 0f)
        val projView = Mat4.multiply(projection, view)

        fun draw(model: FloatArray, r: Float, g: Float, b: Float, a: Float) {
            val mvp = Mat4.multiply(projView, model)
            glUniformMatrix4fv(uMvp, false, mvp)
            glUniform4f(uColor, r, g, b, a)
            glBindVertexArray(discVao)
            glDrawArrays(GL_TRIANGLE_FAN, 0, discVertexCount)
        }

        // Ocean plate
        draw(Mat4.scale(1.35f, 1f, 1.35f), 0.06f, 0.16f, 0.22f, 1f)
        // Island
        draw(Mat4.scale(0.62f, 1f, 0.62f), 0.16f, 0.28f, 0.18f, 1f)
        // Inner terrain hint
        draw(
            Mat4.multiply(
                Mat4.translation(0f, 0.02f, 0f),
                Mat4.scale(0.42f, 1f, 0.42f),
            ),
            0.22f, 0.36f, 0.24f, 1f,
        )

        // Storm column: stacked translucent discs rotating around the island.
        val stormAngle = timeSeconds * 0.7f
        val stormX = kotlin.math.sin(stormAngle) * 0.55f
        val stormZ = kotlin.math.cos(stormAngle) * 0.55f
        val spin = timeSeconds * 2.4f
        val rings = 9
        for (i in 0 until rings) {
            val t = i / (rings - 1f)
            val y = 0.06f + t * 0.85f
            val radius = 0.42f - t * 0.24f
            val alpha = 0.34f - t * 0.18f
            val model = Mat4.multiply(
                Mat4.translation(stormX, y, stormZ),
                Mat4.multiply(
                    Mat4.rotationY(spin + i * 0.35f),
                    Mat4.scale(radius, 1f, radius),
                ),
            )
            draw(model, 0.82f, 0.85f, 0.88f, alpha)
        }

        // Storm center marker (projected path / eye)
        draw(
            Mat4.multiply(
                Mat4.translation(stormX, 0.03f, stormZ),
                Mat4.scale(0.09f, 1f, 0.09f),
            ),
            0.90f, 0.32f, 0.32f, 0.9f,
        )

        glBindVertexArray(0)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo)
        glReadBuffer(GL_COLOR_ATTACHMENT0)
        glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_BYTE, pixelBuffer)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0)

        val rowBytes = width * 4
        val out = ByteArray(width * height * 4)
        val tmp = ByteArray(rowBytes)
        for (y in 0 until height) {
            val srcRow = height - 1 - y
            pixelBuffer.position(srcRow * rowBytes)
            pixelBuffer.get(tmp)
            System.arraycopy(tmp, 0, out, y * rowBytes, rowBytes)
        }
        pixelBuffer.clear()
        return out
    }

    fun destroy() {
        glDeleteBuffers(discVbo)
        glDeleteVertexArrays(discVao)
        glDeleteProgram(program)
        glDeleteRenderbuffers(depthRbo)
        glDeleteTextures(colorTex)
        glDeleteFramebuffers(fbo)
    }

    private fun buildDiscGeometry(segments: Int): Triple<Int, Int, Int> {
        // Center vertex + ring + closing vertex, triangle fan in the XZ plane.
        val count = segments + 2
        val data = FloatArray(count * 3)
        data[0] = 0f; data[1] = 0f; data[2] = 0f
        for (i in 0..segments) {
            val angle = (i * Math.PI * 2.0 / segments).toFloat()
            data[(i + 1) * 3 + 0] = kotlin.math.cos(angle)
            data[(i + 1) * 3 + 1] = 0f
            data[(i + 1) * 3 + 2] = kotlin.math.sin(angle)
        }
        val vao = glGenVertexArrays()
        val vbo = glGenBuffers()
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0L)
        glBindVertexArray(0)
        return Triple(vao, vbo, count)
    }

    private fun buildProgram(vertexSrc: String, fragmentSrc: String): Int {
        fun compile(type: Int, src: String): Int {
            val shader = glCreateShader(type)
            glShaderSource(shader, src)
            glCompileShader(shader)
            check(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_TRUE) {
                "Shader compile failed: ${glGetShaderInfoLog(shader)}"
            }
            return shader
        }
        val vs = compile(GL_VERTEX_SHADER, vertexSrc)
        val fs = compile(GL_FRAGMENT_SHADER, fragmentSrc)
        val prog = glCreateProgram()
        glAttachShader(prog, vs)
        glAttachShader(prog, fs)
        glBindAttribLocation(prog, 0, "aPos")
        glLinkProgram(prog)
        check(glGetProgrami(prog, GL_LINK_STATUS) == GL_TRUE) {
            "Program link failed: ${glGetProgramInfoLog(prog)}"
        }
        glDeleteShader(vs)
        glDeleteShader(fs)
        return prog
    }

    companion object {
        private const val VERTEX_SHADER = """
#version 150
in vec3 aPos;
uniform mat4 uMvp;
void main() {
    gl_Position = uMvp * vec4(aPos, 1.0);
}
"""
        private const val FRAGMENT_SHADER = """
#version 150
out vec4 FragColor;
uniform vec4 uColor;
void main() {
    FragColor = uColor;
}
"""
    }
}
