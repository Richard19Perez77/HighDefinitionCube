package com.rick.highdefinitioncube.graphics

import android.app.ActivityManager
import android.content.Context
import android.os.Build

data class DeviceProfile(
    val model: String,
    val totalRamMb: Long,
    val isLowRam: Boolean,
    val cpuCores: Int,
    val sdk: Int,
    val glEsVersion: String,
) {
    /**
     * Auto never picks Cinematic. That preset is the "make it look like a
     * trailer" switch so a mid-range phone can show what extra quality costs.
     */
    val recommendedQuality: QualityChoice
        get() = when {
            isLowRam || totalRamMb < 4_096L || cpuCores <= 4 -> QualityChoice.Performance
            else -> QualityChoice.Balanced
        }

    val ramLabel: String
        get() = when {
            totalRamMb >= 1024L -> "%.1f GB".format(totalRamMb / 1024.0)
            else -> "$totalRamMb MB"
        }

    val autoReason: String
        get() = when (recommendedQuality) {
            QualityChoice.Performance ->
                "Auto picked Phone-safe: ${ramLabel} RAM, $cpuCores cores."
            QualityChoice.Balanced ->
                "Auto picked Balanced: ${ramLabel} RAM, $cpuCores cores. Max is opt-in."
            else -> "Auto picked ${recommendedQuality.label}."
        }

    companion object {
        fun from(context: Context): DeviceProfile {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memory = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memory)
            val totalRamMb = memory.totalMem / (1024L * 1024L)
            val glEsVersion = runCatching {
                activityManager.deviceConfigurationInfo.glEsVersion
            }.getOrElse { "unknown" }

            return DeviceProfile(
                model = "${Build.MANUFACTURER} ${Build.MODEL}",
                totalRamMb = totalRamMb,
                isLowRam = activityManager.isLowRamDevice,
                cpuCores = Runtime.getRuntime().availableProcessors(),
                sdk = Build.VERSION.SDK_INT,
                glEsVersion = glEsVersion,
            )
        }
    }
}
