package os.nublar.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

/** Selectable full-screen post-process shader effects. */
enum class ScreenShader(val label: String) {
    None("None"),
    Crt("CRT — Scanlines"),
}

/**
 * Basic CRT post-process: barrel curvature with a black bezel, scanlines, a
 * subtle aperture-grille RGB mask, a (halved) vignette, and a slight flicker.
 * `content` is the layer's rasterized pixels.
 */
private const val CRT_BASIC_SKSL = """
uniform shader content;
uniform float2 resolution;
uniform float time;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / resolution;

    float2 c = uv - 0.5;
    float r2 = dot(c, c);
    float2 duv = uv + c * r2 * 0.0675;

    half4 col = content.eval(clamp(duv, 0.0, 1.0) * resolution);
    float inside = step(0.0, duv.x) * step(duv.x, 1.0) * step(0.0, duv.y) * step(duv.y, 1.0);
    col.rgb *= inside;

    float scan = 0.868 + 0.132 * sin(duv.y * resolution.y * 3.14159);
    col.rgb *= scan;

    // Slight vertical roll: a faint dark band scrolling down the screen.
    float rollBand = fract(duv.y - time * 0.08);
    float roll = 1.0 - 0.05 * smoothstep(0.85, 1.0, rollBand);
    col.rgb *= roll;

    float m = mod(fragCoord.x, 3.0);
    half3 mask = m < 1.0 ? half3(1.06, 0.94, 0.94)
               : m < 2.0 ? half3(0.94, 1.06, 0.94)
               :           half3(0.94, 0.94, 1.06);
    col.rgb *= mask;

    float vig = smoothstep(0.95, 0.35, r2 * 2.2);
    vig = mix(1.0, vig, 0.5);
    col.rgb *= vig;

    float flick = 0.97 + 0.03 * fract(sin(time * 91.7) * 47453.1);
    col.rgb *= flick;

    col.a = 1.0;
    return col;
}
"""

/** Compiles the runtime effect for [shader]; null when the shader fails to build. */
private fun compileEffect(shader: ScreenShader): RuntimeEffect? = when (shader) {
    ScreenShader.None -> null
    ScreenShader.Crt -> runCatching { RuntimeEffect.makeForShader(CRT_BASIC_SKSL) }.getOrNull()
}

/**
 * Wraps [content] and renders it through the selected [shader]. The effect is
 * compiled once per shader; only per-frame uniforms (time, resolution) update.
 * [ScreenShader.None] — or a shader that fails to compile — is a plain
 * pass-through Box (no offscreen layer).
 */
@Composable
fun CrtScreen(
    modifier: Modifier = Modifier,
    shader: ScreenShader = ScreenShader.Crt,
    content: @Composable () -> Unit,
) {
    val effect = remember(shader) { compileEffect(shader) }
    if (effect == null) {
        Box(modifier) { content() }
        return
    }
    // Advance a time value each frame so the shaders animate.
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(shader) {
        while (true) {
            withFrameMillis { time = it / 1000f }
        }
    }
    Box(
        modifier = modifier.graphicsLayer {
            if (size.width > 0f && size.height > 0f) {
                val builder = RuntimeShaderBuilder(effect)
                val childName = when (shader) {
                    ScreenShader.Crt -> {
                        builder.uniform("resolution", size.width, size.height)
                        builder.uniform("time", time)
                        "content"
                    }
                    ScreenShader.None -> "content"
                }
                renderEffect = ImageFilter
                    .makeRuntimeShader(builder, childName, null)
                    .asComposeRenderEffect()
            }
            clip = true
        },
    ) {
        content()
    }
}
