package os.nublar.stormtrack

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/** Minimal column-major 4×4 matrix helpers for the StormTrack OpenGL renderer. */
object Mat4 {
    fun identity(): FloatArray = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f,
    )

    fun multiply(a: FloatArray, b: FloatArray): FloatArray {
        val out = FloatArray(16)
        for (col in 0..3) {
            for (row in 0..3) {
                var sum = 0f
                for (i in 0..3) {
                    sum += a[i * 4 + row] * b[col * 4 + i]
                }
                out[col * 4 + row] = sum
            }
        }
        return out
    }

    fun perspective(fovYRadians: Float, aspect: Float, near: Float, far: Float): FloatArray {
        val t = 1f / tan(fovYRadians / 2f)
        val m = FloatArray(16)
        m[0] = t / aspect
        m[5] = t
        m[10] = (far + near) / (near - far)
        m[11] = -1f
        m[14] = 2f * far * near / (near - far)
        return m
    }

    fun lookAt(
        eyeX: Float, eyeY: Float, eyeZ: Float,
        centerX: Float, centerY: Float, centerZ: Float,
        upX: Float, upY: Float, upZ: Float,
    ): FloatArray {
        var fx = centerX - eyeX
        var fy = centerY - eyeY
        var fz = centerZ - eyeZ
        val fl = kotlin.math.sqrt(fx * fx + fy * fy + fz * fz)
        fx /= fl; fy /= fl; fz /= fl

        var sx = fy * upZ - fz * upY
        var sy = fz * upX - fx * upZ
        var sz = fx * upY - fy * upX
        val sl = kotlin.math.sqrt(sx * sx + sy * sy + sz * sz)
        sx /= sl; sy /= sl; sz /= sl

        val ux = sy * fz - sz * fy
        val uy = sz * fx - sx * fz
        val uz = sx * fy - sy * fx

        return floatArrayOf(
            sx, ux, -fx, 0f,
            sy, uy, -fy, 0f,
            sz, uz, -fz, 0f,
            -(sx * eyeX + sy * eyeY + sz * eyeZ),
            -(ux * eyeX + uy * eyeY + uz * eyeZ),
            (fx * eyeX + fy * eyeY + fz * eyeZ),
            1f,
        )
    }

    fun translation(x: Float, y: Float, z: Float): FloatArray {
        val m = identity()
        m[12] = x; m[13] = y; m[14] = z
        return m
    }

    fun scale(x: Float, y: Float, z: Float): FloatArray {
        val m = identity()
        m[0] = x; m[5] = y; m[10] = z
        return m
    }

    fun rotationY(radians: Float): FloatArray {
        val c = cos(radians); val s = sin(radians)
        return floatArrayOf(
            c, 0f, -s, 0f,
            0f, 1f, 0f, 0f,
            s, 0f, c, 0f,
            0f, 0f, 0f, 1f,
        )
    }
}
