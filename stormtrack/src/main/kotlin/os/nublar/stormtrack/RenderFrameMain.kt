package os.nublar.stormtrack

import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import java.io.File

/**
 * Headless dev harness: renders one storm frame at a fixed time and writes it
 * to stormtrack/build/storm-frame.png, so the renderer can be verified without
 * opening a window. Run with: ./gradlew :stormtrack:renderStormFrame
 */
fun main() {
    val width = 640
    val height = 480
    val renderer = SoftwareRenderer3D(width, height)
    val pixels = renderer.render(timeSeconds = 3.0f)
    val image = Image.makeRaster(
        ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.OPAQUE),
        pixels,
        width * 4,
    )
    val png = image.encodeToData(EncodedImageFormat.PNG)!!.bytes
    val out = File("build/storm-frame.png")
    out.parentFile.mkdirs()
    out.writeBytes(png)
    println("Wrote ${out.absolutePath}")
}
