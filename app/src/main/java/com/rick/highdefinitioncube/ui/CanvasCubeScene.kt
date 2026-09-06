package com.rick.highdefinitioncube.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.rick.highdefinitioncube.graphics.CubeDemoState
import com.rick.highdefinitioncube.graphics.QualityChoice
import androidx.compose.ui.graphics.lerp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CanvasCubeScene(
    state: CubeDemoState,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .background(Color(0xFF07070C))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { state.toggleSpin() })
            }
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    state.drag(
                        deltaYaw = dragAmount.x * 0.35f,
                        deltaPitch = dragAmount.y * 0.35f,
                    )
                }
            },
    ) {
        drawStudioBackdrop()
        drawProjectedCube(
            yawDegrees = state.yaw,
            pitchDegrees = state.pitch,
            lookColor = state.look.color,
            metallic = state.look.metallic,
            roughness = state.look.roughness,
            quality = state.resolvedQuality,
        )
    }
}

private fun DrawScope.drawStudioBackdrop() {
    drawRect(Color(0xFF07070C))
    drawCircle(
        color = Color(0xFF1A2240),
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.5f, size.height * 0.42f),
        alpha = 0.55f,
    )
}

private fun DrawScope.drawProjectedCube(
    yawDegrees: Float,
    pitchDegrees: Float,
    lookColor: Color,
    metallic: Float,
    roughness: Float,
    quality: QualityChoice,
) {
    val yaw = Math.toRadians(yawDegrees.toDouble())
    val pitch = Math.toRadians(pitchDegrees.toDouble())
    val cosY = cos(yaw)
    val sinY = sin(yaw)
    val cosP = cos(pitch)
    val sinP = sin(pitch)

    val half = 1f
    val verts = arrayOf(
        floatArrayOf(-half, -half, -half),
        floatArrayOf(half, -half, -half),
        floatArrayOf(half, half, -half),
        floatArrayOf(-half, half, -half),
        floatArrayOf(-half, -half, half),
        floatArrayOf(half, -half, half),
        floatArrayOf(half, half, half),
        floatArrayOf(-half, half, half),
    )

    val projected = Array(8) { Offset.Zero }
    val rotated = Array(8) { FloatArray(3) }
    val cameraZ = 4.2f
    val scale = size.minDimension * 0.28f
    val origin = Offset(size.width / 2f, size.height * 0.46f)

    for (i in verts.indices) {
        val x0 = verts[i][0]
        val y0 = verts[i][1]
        val z0 = verts[i][2]
        val x1 = (x0 * cosY + z0 * sinY).toFloat()
        val z1 = (-x0 * sinY + z0 * cosY).toFloat()
        val y1 = (y0 * cosP - z1 * sinP).toFloat()
        val z2 = (y0 * sinP + z1 * cosP).toFloat()
        rotated[i][0] = x1
        rotated[i][1] = y1
        rotated[i][2] = z2
        val perspective = cameraZ / (cameraZ + z2)
        projected[i] = Offset(
            origin.x + x1 * scale * perspective,
            origin.y - y1 * scale * perspective,
        )
    }

    data class Face(
        val indices: IntArray,
        val shade: Float,
        val depth: Float,
    )

    val faces = listOf(
        intArrayOf(0, 1, 2, 3),
        intArrayOf(5, 4, 7, 6),
        intArrayOf(4, 0, 3, 7),
        intArrayOf(1, 5, 6, 2),
        intArrayOf(3, 2, 6, 7),
        intArrayOf(4, 5, 1, 0),
    ).map { idx ->
        val a = rotated[idx[0]]
        val b = rotated[idx[1]]
        val c = rotated[idx[2]]
        val ux = b[0] - a[0]
        val uy = b[1] - a[1]
        val uz = b[2] - a[2]
        val vx = c[0] - a[0]
        val vy = c[1] - a[1]
        val vz = c[2] - a[2]
        val nx = uy * vz - uz * vy
        val ny = uz * vx - ux * vz
        val nz = ux * vy - uy * vx
        val length = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.0001f)
        val light = (nx * 0.35f + ny * 0.72f + nz * 0.55f) / length
        val specular = if (metallic > 0.5f) (light * (1f - roughness)).coerceAtLeast(0f) else 0f
        val shade = (0.22f + light * 0.62f + specular * 0.35f).coerceIn(0.08f, 1f)
        val depth = (rotated[idx[0]][2] + rotated[idx[1]][2] + rotated[idx[2]][2] + rotated[idx[3]][2]) / 4f
        Face(idx, shade, depth)
    }.sortedBy { it.depth }

    for (face in faces) {
        val path = Path().apply {
            moveTo(projected[face.indices[0]].x, projected[face.indices[0]].y)
            for (i in 1 until 4) {
                lineTo(projected[face.indices[i]].x, projected[face.indices[i]].y)
            }
            close()
        }
        val fill = lerp(Color.Black, lookColor, face.shade.coerceIn(0.12f, 1f))
        drawPath(path, fill)
        if (quality != QualityChoice.Performance) {
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.18f + metallic * 0.2f),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}
