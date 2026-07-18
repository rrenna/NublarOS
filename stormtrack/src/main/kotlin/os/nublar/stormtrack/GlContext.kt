package os.nublar.stormtrack

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.OSMesa.*
import java.nio.ByteBuffer

/**
 * Owns the offscreen OpenGL context for StormTrack.
 *
 * Uses OSMesa (Mesa's software offscreen renderer) rather than a GLFW window:
 * a Compose Desktop app can't host a GLFW window on macOS (GLFW requires the
 * process's first thread, which AWT/Skiko already owns). OSMesa renders
 * entirely into CPU memory with no window and no thread-affinity constraints,
 * which is exactly what the offscreen-to-ImageBitmap pipeline needs.
 */
object GlContext {
    private var context: Long = 0L
    private var defaultBuffer: ByteBuffer? = null

    /** Creates the OSMesa context. Safe to call from any thread, but only once. */
    @Synchronized
    fun ensureInitialized() {
        if (context != 0L) return
        val attribs = BufferUtils.createIntBuffer(16).put(
            intArrayOf(
                OSMESA_FORMAT, OSMESA_BGRA,
                OSMESA_DEPTH_BITS, 24,
                OSMESA_STENCIL_BITS, 0,
                OSMESA_ACCUM_BITS, 0,
                OSMESA_PROFILE, OSMESA_COMPAT_PROFILE,
                OSMESA_CONTEXT_MAJOR_VERSION, 3,
                OSMESA_CONTEXT_MINOR_VERSION, 2,
                0,
            ),
        )
        attribs.flip()
        context = OSMesaCreateContextAttribs(attribs, 0L)
        check(context != 0L) { "Failed to create OSMesa context" }
    }

    /**
     * Makes the context current on the calling thread. [width]/[height] size
     * the dummy default framebuffer OSMesa requires; actual rendering targets
     * our own FBO.
     */
    fun makeCurrent(width: Int, height: Int) {
        check(context != 0L) { "GlContext not initialized" }
        val buffer = BufferUtils.createByteBuffer(width * height * 4)
        check(OSMesaMakeCurrent(context, buffer, GL_UNSIGNED_BYTE, width, height)) {
            "OSMesaMakeCurrent failed"
        }
        defaultBuffer = buffer
        GL.createCapabilities()
    }

    @Synchronized
    fun destroy() {
        if (context != 0L) {
            OSMesaDestroyContext(context)
            context = 0L
            defaultBuffer = null
        }
    }
}
