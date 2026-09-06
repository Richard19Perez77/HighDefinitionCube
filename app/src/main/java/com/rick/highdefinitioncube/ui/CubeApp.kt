package com.rick.highdefinitioncube.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rick.highdefinitioncube.graphics.CubeDemoState
import com.rick.highdefinitioncube.graphics.CubeLooks
import com.rick.highdefinitioncube.graphics.DeviceProfile
import com.rick.highdefinitioncube.graphics.QualityChoice
import com.rick.highdefinitioncube.graphics.RendererBackend

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CubeApp() {
    val context = LocalContext.current
    val device = remember { DeviceProfile.from(context) }
    val state = remember { CubeDemoState(device) }
    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = (now - lastNanos) / 1_000_000_000f
                    state.tick(dt)
                    state.noteFrame(dt)
                }
                lastNanos = now
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07070C)),
    ) {
        when (state.backend) {
            RendererBackend.Filament -> FilamentCubeScene(
                state = state,
                modifier = Modifier.fillMaxSize(),
            )
            RendererBackend.Canvas -> CanvasCubeScene(
                state = state,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            HudCard(state)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(onClick = { showControls = !showControls }) {
                    Text(
                        text = if (showControls) "Hide controls" else "Show controls",
                        color = Color(0xFFD7DEEA),
                    )
                }
                AnimatedVisibility(visible = showControls) {
                    ControlSheet(state)
                }
            }
        }
    }
}

@Composable
private fun HudCard(state: CubeDemoState) {
    Surface(
        color = Color(0xCC10131A),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "HIGH DEFINITION CUBE",
                color = Color(0xFFE8EEF8),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${state.look.name}  ·  ${state.backend.engineName}",
                color = Color(0xFFF4C15D),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = state.look.blurb,
                color = Color(0xFFB7C0CE),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = buildString {
                    append("%.0f fps".format(state.fps))
                    append("  ·  ")
                    append("%.1f ms".format(state.frameMs))
                    append("  ·  heap ")
                    append(state.javaHeapMb)
                    append(" MB")
                },
                color = Color(0xFFE8EEF8),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
            )
            Text(
                text = "${state.device.model}  ·  ${state.device.ramLabel}  ·  ${state.device.cpuCores} cores  ·  GLES ${state.device.glEsVersion}",
                color = Color(0xFF8E99AB),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
            )
            Text(
                text = if (state.qualityChoice == QualityChoice.Auto) {
                    state.device.autoReason
                } else {
                    "Manual ${state.resolvedQuality.label}: ${state.resolvedQuality.summary}"
                },
                color = Color(0xFF9BB0D0),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ControlSheet(state: CubeDemoState) {
    Surface(
        color = Color(0xE610131A),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Label("Renderer")
            ChipRow {
                RendererBackend.entries.forEach { backend ->
                    DemoChip(
                        label = backend.label,
                        selected = state.backend == backend,
                        onClick = { state.backend = backend },
                    )
                }
            }
            Label("Quality on this phone")
            ChipRow {
                QualityChoice.entries.forEach { quality ->
                    DemoChip(
                        label = quality.label,
                        selected = state.qualityChoice == quality,
                        onClick = { state.qualityChoice = quality },
                    )
                }
            }
            Label("Look")
            ChipRow {
                CubeLooks.curated.forEach { look ->
                    DemoChip(
                        label = look.name,
                        selected = state.look.name == look.name && state.look in CubeLooks.curated,
                        onClick = { state.look = look },
                    )
                }
                DemoChip(
                    label = "Remix",
                    selected = state.look !in CubeLooks.curated,
                    onClick = { state.remixLook() },
                )
            }
            Label(if (state.autoSpin) "Auto-spin  ${state.spinSpeed.toInt()}°/s" else "Flick spin  ·  tap cube to resume")
            Slider(
                value = state.spinSpeed,
                onValueChange = {
                    state.spinSpeed = it
                    state.autoSpin = true
                },
                valueRange = 0f..90f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFF4C15D),
                    activeTrackColor = Color(0xFFF4C15D),
                    inactiveTrackColor = Color(0xFF2A3140),
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = { state.toggleSpin() }) {
                    Text(
                        text = if (state.autoSpin) "Pause spin" else "Resume spin",
                        color = Color(0xFFE8EEF8),
                    )
                }
                Text(
                    text = if (state.backend == RendererBackend.Filament) {
                        "Drag to orbit · pinch zoom · tap pause"
                    } else {
                        "Drag to spin · tap pause"
                    },
                    color = Color(0xFF8E99AB),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text.uppercase(),
        color = Color(0xFF8E99AB),
        fontSize = 11.sp,
        letterSpacing = 1.1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        content()
    }
}

@Composable
private fun DemoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFF1A2030),
            labelColor = Color(0xFFD7DEEA),
            selectedContainerColor = Color(0xFFF4C15D),
            selectedLabelColor = Color(0xFF1A1408),
        ),
    )
}
