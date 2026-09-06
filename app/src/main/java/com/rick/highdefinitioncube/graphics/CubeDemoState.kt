package com.rick.highdefinitioncube.graphics

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.random.Random

enum class RendererBackend(val label: String, val engineName: String) {
    Filament("Filament", "SceneView + Filament"),
    Canvas("Canvas", "Compose Canvas (CPU)"),
}

enum class QualityChoice(val label: String, val summary: String) {
    Auto("Auto", "Picked from this phone's RAM and CPU"),
    Performance("Phone-safe", "No SSAO/bloom, 1x MSAA — holds fps when warm"),
    Balanced("Balanced", "2x MSAA, SSAO, bloom — default product look"),
    Cinematic("Max", "4x MSAA, high shadows, DoF — flagship trailer preset"),
}

@Stable
class CubeDemoState(val device: DeviceProfile) {
    var backend by mutableStateOf(RendererBackend.Filament)
    var qualityChoice by mutableStateOf(QualityChoice.Auto)
    var look by mutableStateOf(CubeLooks.chrome)
    var autoSpin by mutableStateOf(true)
    var spinSpeed by mutableFloatStateOf(28f)
    var yaw by mutableFloatStateOf(38f)
    var pitch by mutableFloatStateOf(-22f)
    var fps by mutableFloatStateOf(0f)
    var frameMs by mutableFloatStateOf(0f)
    var javaHeapMb by mutableIntStateOf(0)

    private var yawVelocity = 0f
    private var pitchVelocity = 0f
    private var fpsAccum = 0f
    private var fpsFrames = 0
    private var remixSerial = 1

    val resolvedQuality: QualityChoice
        get() = if (qualityChoice == QualityChoice.Auto) device.recommendedQuality else qualityChoice

    fun tick(dtSeconds: Float) {
        if (dtSeconds <= 0f || dtSeconds > 0.1f) return
        if (autoSpin) {
            yaw = wrapDegrees(yaw + spinSpeed * dtSeconds)
            yawVelocity = 0f
            pitchVelocity = 0f
        } else {
            yaw = wrapDegrees(yaw + yawVelocity * dtSeconds)
            pitch = (pitch + pitchVelocity * dtSeconds).coerceIn(-80f, 80f)
            yawVelocity *= 0.985f
            pitchVelocity *= 0.985f
            if (abs(yawVelocity) < 1f) yawVelocity = 0f
            if (abs(pitchVelocity) < 1f) pitchVelocity = 0f
        }
    }

    fun drag(deltaYaw: Float, deltaPitch: Float) {
        autoSpin = false
        yaw = wrapDegrees(yaw + deltaYaw)
        pitch = (pitch + deltaPitch).coerceIn(-80f, 80f)
        yawVelocity = deltaYaw * 48f
        pitchVelocity = deltaPitch * 48f
    }

    fun toggleSpin() {
        autoSpin = !autoSpin
        if (autoSpin) {
            yawVelocity = 0f
            pitchVelocity = 0f
        }
    }

    fun noteFrame(dtSeconds: Float) {
        if (dtSeconds <= 0f || dtSeconds > 1f) return
        fpsAccum += 1f / dtSeconds
        fpsFrames += 1
        if (fpsFrames >= 20) {
            fps = fpsAccum / fpsFrames
            frameMs = 1000f / fps.coerceAtLeast(1f)
            fpsAccum = 0f
            fpsFrames = 0
        }
        val runtime = Runtime.getRuntime()
        javaHeapMb = ((runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L)).toInt()
    }

    fun remixLook() {
        look = CubeLooks.remix(Random(System.nanoTime() + remixSerial))
        remixSerial += 1
    }

    private fun wrapDegrees(value: Float): Float {
        var wrapped = value % 360f
        if (wrapped < 0f) wrapped += 360f
        return wrapped
    }
}
