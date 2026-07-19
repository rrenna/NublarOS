package os.nublar.stormtrack

import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure-Kotlin software 3D renderer for the StormTrack proof-of-concept.
 *
 * Why software instead of OpenGL/LWJGL: a Compose Desktop process can't host
 * GLFW on macOS (it demands the process's first thread, which AWT/Skiko owns),
 * LWJGL's AWT canvas isn't supported on macOS, and LWJGL publishes no OSMesa
 * artifact for windowless rendering. A small CPU rasterizer — perspective
 * projection, z-buffer, flat shading, alpha-blended cloud layers — is real 3D,
 * has zero native dependencies, runs identically on every platform, and its
 * low-res pixelated output is exactly the early-90s workstation look this
 * module is going for.
 */
class SoftwareRenderer3D(private val width: Int, private val height: Int) {

    private val bgra = ByteArray(width * height * 4)
    private val zbuf = FloatArray(width * height)

    private data class V3(val x: Float, val y: Float, val z: Float)
    private class Tri(val a: V3, val b: V3, val c: V3, val argb: Long, val translucent: Boolean)

    /** Renders one frame at [timeSeconds]; returns BGRA pixel rows (top-left origin). */
    fun render(timeSeconds: Float): ByteArray {
        clearBuffer()
        val tris = ArrayList<Tri>(2048)
        buildScene(tris, timeSeconds)

        val aspect = width.toFloat() / height.toFloat()
        val projection = Mat4.perspective(Math.toRadians(45.0).toFloat(), aspect, 0.1f, 100f)
        val camAngle = timeSeconds * 0.18f
        val camDist = 2.6f
        val eyeX = sin(camAngle) * camDist
        val eyeZ = cos(camAngle) * camDist
        val view = Mat4.lookAt(eyeX, 1.5f, eyeZ, 0f, 0.15f, 0f, 0f, 1f, 0f)
        val mvp = Mat4.multiply(projection, view)

        val opaque = ArrayList<ProjectedTri>(tris.size)
        val translucent = ArrayList<ProjectedTri>(tris.size / 4)
        for (tri in tris) {
            val p = project(tri, mvp) ?: continue
            if (tri.translucent) translucent.add(p) else opaque.add(p)
        }
        // Painter's order for the alpha pass: far to near.
        translucent.sortByDescending { (it.sz0 + it.sz1 + it.sz2) / 3f }

        for (p in opaque) rasterize(p, blend = false)
        for (p in translucent) rasterize(p, blend = true)
        return bgra
    }

    private class ProjectedTri(
        val sx0: Float, val sy0: Float, val sz0: Float,
        val sx1: Float, val sy1: Float, val sz1: Float,
        val sx2: Float, val sy2: Float, val sz2: Float,
        val argb: Long,
    )

    private fun project(tri: Tri, m: FloatArray): ProjectedTri? {
        val xs = FloatArray(3); val ys = FloatArray(3); val zs = FloatArray(3)
        val verts = arrayOf(tri.a, tri.b, tri.c)
        for (i in 0..2) {
            val v = verts[i]
            val x = m[0] * v.x + m[4] * v.y + m[8] * v.z + m[12]
            val y = m[1] * v.x + m[5] * v.y + m[9] * v.z + m[13]
            val z = m[2] * v.x + m[6] * v.y + m[10] * v.z + m[14]
            val w = m[3] * v.x + m[7] * v.y + m[11] * v.z + m[15]
            if (w <= 0.0001f) return null // behind the camera — drop the triangle
            xs[i] = (x / w * 0.5f + 0.5f) * width
            ys[i] = (0.5f - y / w * 0.5f) * height
            zs[i] = z / w
        }
        return ProjectedTri(
            xs[0], ys[0], zs[0],
            xs[1], ys[1], zs[1],
            xs[2], ys[2], zs[2],
            tri.argb,
        )
    }

    private fun rasterize(p: ProjectedTri, blend: Boolean) {
        val minX = max(0, floor(min(p.sx0, min(p.sx1, p.sx2))).toInt())
        val maxX = min(width - 1, floor(max(p.sx0, max(p.sx1, p.sx2))).toInt())
        val minY = max(0, floor(min(p.sy0, min(p.sy1, p.sy2))).toInt())
        val maxY = min(height - 1, floor(max(p.sy0, max(p.sy1, p.sy2))).toInt())
        if (minX > maxX || minY > maxY) return

        val area = edge(p.sx0, p.sy0, p.sx1, p.sy1, p.sx2, p.sy2)
        if (area == 0f) return

        val sa = ((p.argb shr 24) and 0xFF).toInt()
        val sr = ((p.argb shr 16) and 0xFF).toInt()
        val sg = ((p.argb shr 8) and 0xFF).toInt()
        val sb = (p.argb and 0xFF).toInt()

        for (y in minY..maxY) {
            val py = y + 0.5f
            var idx = y * width + minX
            for (x in minX..maxX) {
                val px = x + 0.5f
                val w0 = edge(p.sx1, p.sy1, p.sx2, p.sy2, px, py)
                val w1 = edge(p.sx2, p.sy2, p.sx0, p.sy0, px, py)
                val w2 = edge(p.sx0, p.sy0, p.sx1, p.sy1, px, py)
                if ((w0 >= 0f && w1 >= 0f && w2 >= 0f) || (w0 <= 0f && w1 <= 0f && w2 <= 0f)) {
                    val z = (w0 * p.sz0 + w1 * p.sz1 + w2 * p.sz2) / area
                    if (z < zbuf[idx]) {
                        zbuf[idx] = z
                        val o = idx * 4
                        if (blend && sa < 255) {
                            val inv = 255 - sa
                            bgra[o] = ((sb * sa + (bgra[o].toInt() and 0xFF) * inv) / 255).toByte()
                            bgra[o + 1] = ((sg * sa + (bgra[o + 1].toInt() and 0xFF) * inv) / 255).toByte()
                            bgra[o + 2] = ((sr * sa + (bgra[o + 2].toInt() and 0xFF) * inv) / 255).toByte()
                            bgra[o + 3] = 255.toByte()
                        } else {
                            bgra[o] = sb.toByte()
                            bgra[o + 1] = sg.toByte()
                            bgra[o + 2] = sr.toByte()
                            bgra[o + 3] = 255.toByte()
                        }
                    }
                }
                idx++
            }
        }
    }

    private fun edge(ax: Float, ay: Float, bx: Float, by: Float, px: Float, py: Float): Float =
        (px - ax) * (by - ay) - (py - ay) * (bx - ax)

    private fun clearBuffer() {
        // Screen-black background (#0B1418).
        for (i in bgra.indices step 4) {
            bgra[i] = 0x18
            bgra[i + 1] = 0x14
            bgra[i + 2] = 0x0B
            bgra[i + 3] = 0xFF.toByte()
        }
        zbuf.fill(Float.POSITIVE_INFINITY)
    }

    // ------------------------------------------------------------------ scene

    private fun buildScene(tris: MutableList<Tri>, t: Float) {
        // Ocean plate.
        disc(tris, 0f, -0.005f, 0f, 1.7f, 64, 0xFF12303F)

        // Island: low cone + plateau, lambert-shaded.
        cone(tris, 0f, 0f, 0f, 0.62f, 0.42f, 0.22f, 48, 0xFF2E4A33, shade = true)
        disc(tris, 0f, 0.22f, 0f, 0.42f, 48, 0xFF3A5C3F)

        // Storm: a stack of rotating translucent ring sectors around the island.
        val stormAngle = t * 0.7f
        val stormX = sin(stormAngle) * 0.55f
        val stormZ = cos(stormAngle) * 0.55f
        val spin = t * 2.4f
        val rings = 9
        for (i in 0 until rings) {
            val f = i / (rings - 1f)
            val y = 0.06f + f * 0.85f
            val radius = 0.42f - f * 0.24f
            val alpha = ((0.34f - f * 0.18f) * 255).toInt() shl 24
            val base = 0.78f + 0.07f * sin(i * 1.7f)
            val rotation = spin + i * 0.35f
            ringSectors(tris, stormX, y, stormZ, radius, 48, rotation, alpha, base)
        }

        // Storm eye / projected-position marker on the ground.
        disc(tris, stormX, 0.03f, stormZ, 0.09f, 24, 0xE5E55454)
    }

    private fun disc(
        tris: MutableList<Tri>,
        cx: Float, cy: Float, cz: Float,
        radius: Float, segments: Int, argb: Long,
    ) {
        val center = V3(cx, cy, cz)
        var prev = V3(cx + radius, cy, cz)
        for (i in 1..segments) {
            val a = (i * Math.PI * 2.0 / segments).toFloat()
            val next = V3(cx + cos(a) * radius, cy, cz + sin(a) * radius)
            tris.add(Tri(center, prev, next, argb, translucent = (argb ushr 24) < 255))
            prev = next
        }
    }

    /** Ring of alternating-brightness sectors so the storm's rotation is visible. */
    private fun ringSectors(
        tris: MutableList<Tri>,
        cx: Float, cy: Float, cz: Float,
        radius: Float, segments: Int, rotation: Float,
        alphaBits: Int, baseBrightness: Float,
    ) {
        val center = V3(cx, cy, cz)
        for (i in 0 until segments) {
            val a0 = rotation + (i * Math.PI * 2.0 / segments).toFloat()
            val a1 = rotation + ((i + 1) * Math.PI * 2.0 / segments).toFloat()
            val bright = if (i % 2 == 0) baseBrightness else baseBrightness - 0.09f
            val r = (bright * 255).toInt().coerceIn(0, 255)
            val g = (bright * 262).toInt().coerceIn(0, 255)
            val b = (bright * 268).toInt().coerceIn(0, 255)
            val argb = (alphaBits.toLong() and 0xFF000000) or
                ((r.toLong() and 0xFF) shl 16) or ((g.toLong() and 0xFF) shl 8) or (b.toLong() and 0xFF)
            val v0 = V3(cx + cos(a0) * radius, cy, cz + sin(a0) * radius)
            val v1 = V3(cx + cos(a1) * radius, cy, cz + sin(a1) * radius)
            tris.add(Tri(center, v0, v1, argb, translucent = true))
        }
    }

    private val lightDir = run {
        val l = sqrt(0.5f * 0.5f + 1f + 0.35f * 0.35f)
        V3(-0.5f / l, 1f / l, 0.35f / l)
    }

    private fun cone(
        tris: MutableList<Tri>,
        cx: Float, cyBase: Float, cz: Float,
        rBottom: Float, rTop: Float, height: Float,
        segments: Int, argb: Long, shade: Boolean,
    ) {
        val yTop = cyBase + height
        for (i in 0 until segments) {
            val a0 = (i * Math.PI * 2.0 / segments).toFloat()
            val a1 = ((i + 1) * Math.PI * 2.0 / segments).toFloat()
            val b0 = V3(cx + cos(a0) * rBottom, cyBase, cz + sin(a0) * rBottom)
            val b1 = V3(cx + cos(a1) * rBottom, cyBase, cz + sin(a1) * rBottom)
            val t0 = V3(cx + cos(a0) * rTop, yTop, cz + sin(a0) * rTop)
            val t1 = V3(cx + cos(a1) * rTop, yTop, cz + sin(a1) * rTop)
            val color = if (shade) shadeColor(argb, b0, b1, t1) else argb
            tris.add(Tri(b0, b1, t1, color, translucent = false))
            tris.add(Tri(b0, t1, t0, color, translucent = false))
        }
    }

    private fun shadeColor(argb: Long, a: V3, b: V3, c: V3): Long {
        // Face normal × light, floor the brightness so dark faces stay readable.
        val ux = b.x - a.x; val uy = b.y - a.y; val uz = b.z - a.z
        val vx = c.x - a.x; val vy = c.y - a.y; val vz = c.z - a.z
        var nx = uy * vz - uz * vy
        var ny = uz * vx - ux * vz
        var nz = ux * vy - uy * vx
        val nl = sqrt(nx * nx + ny * ny + nz * nz)
        if (nl > 0f) { nx /= nl; ny /= nl; nz /= nl }
        val ndotl = max(0.22f, nx * lightDir.x + ny * lightDir.y + nz * lightDir.z)
        val r = (((argb shr 16) and 0xFF) * ndotl).toInt().coerceIn(0, 255)
        val g = (((argb shr 8) and 0xFF) * ndotl).toInt().coerceIn(0, 255)
        val bl = ((argb and 0xFF) * ndotl).toInt().coerceIn(0, 255)
        return (argb and 0xFF000000.toLong()) or
            ((r.toLong() and 0xFF) shl 16) or ((g.toLong() and 0xFF) shl 8) or (bl.toLong() and 0xFF)
    }
}
