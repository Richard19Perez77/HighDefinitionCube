package com.rick.highdefinitioncube.graphics

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class CubeLook(
    val name: String,
    val blurb: String,
    val color: Color,
    val metallic: Float,
    val roughness: Float,
    val reflectance: Float,
)

object CubeLooks {
    val chrome = CubeLook(
        name = "Liquid Chrome",
        blurb = "Almost-mirror metal. Cheap on geometry, expensive on lighting.",
        color = Color(0xFFD9DEE7),
        metallic = 1f,
        roughness = 0.06f,
        reflectance = 0.9f,
    )
    val gold = CubeLook(
        name = "Studio Gold",
        blurb = "Warm metal. Reads as 'product shot' under Filament IBL.",
        color = Color(0xFFE2B13C),
        metallic = 1f,
        roughness = 0.22f,
        reflectance = 0.7f,
    )
    val candy = CubeLook(
        name = "Candy Coat",
        blurb = "Dielectric paint. Color lives in the base, not the metal.",
        color = Color(0xFF6B5CFF),
        metallic = 0.05f,
        roughness = 0.28f,
        reflectance = 0.55f,
    )
    val glass = CubeLook(
        name = "Obsidian Glass",
        blurb = "Dark, tight highlights. Shows bloom cost on Max quality.",
        color = Color(0xFF141820),
        metallic = 0.15f,
        roughness = 0.04f,
        reflectance = 0.85f,
    )

    val curated: List<CubeLook> = listOf(chrome, gold, candy, glass)

    private val remixNames = listOf(
        "Nebula Foil",
        "Harbor Brass",
        "Afterimage",
        "Velvet Ion",
        "Midnight Lacquer",
        "Solar Drift",
        "Quiet Mercury",
        "Iris Alloy",
    )

    fun remix(random: Random): CubeLook {
        val hue = random.nextFloat() * 360f
        val metallic = if (random.nextFloat() > 0.32f) {
            0.72f + random.nextFloat() * 0.28f
        } else {
            random.nextFloat() * 0.18f
        }
        val roughness = 0.04f + random.nextFloat() * 0.32f
        val sat = 0.28f + random.nextFloat() * 0.55f
        val value = 0.55f + random.nextFloat() * 0.4f
        val color = Color(AndroidColor.HSVToColor(floatArrayOf(hue, sat, value)))
        return CubeLook(
            name = remixNames[random.nextInt(remixNames.size)],
            blurb = "On-device remix of hue, metal, and roughness. Phone-safe 'AI look' — no cloud GPU.",
            color = color,
            metallic = metallic,
            roughness = roughness,
            reflectance = lerpReflectance(metallic),
        )
    }

    private fun lerpReflectance(metallic: Float): Float {
        return 0.45f + metallic * 0.45f
    }
}

