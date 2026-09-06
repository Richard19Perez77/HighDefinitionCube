package com.rick.highdefinitioncube.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.rick.highdefinitioncube.graphics.CubeDemoState
import com.rick.highdefinitioncube.graphics.QualityChoice
import io.github.sceneview.RenderQuality
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import io.github.sceneview.node.ContactShadowContext
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberFillLightNode
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberOnGestureListener

@Composable
fun FilamentCubeScene(
    state: CubeDemoState,
    modifier: Modifier = Modifier,
) {
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val look = state.look
    val cubeMaterial = remember(materialLoader, look) {
        materialLoader.createColorInstance(
            color = look.color,
            metallic = look.metallic,
            roughness = look.roughness,
            reflectance = look.reflectance,
        )
    }
    val groundMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(
            color = Color(0xFF12141A),
            metallic = 0.1f,
            roughness = 0.85f,
            reflectance = 0.3f,
        )
    }
    val renderQuality = when (state.resolvedQuality) {
        QualityChoice.Performance -> RenderQuality.Performance
        QualityChoice.Cinematic -> RenderQuality.Cinematic
        QualityChoice.Balanced, QualityChoice.Auto -> RenderQuality.Default
    }

    SceneView(
        modifier = modifier,
        engine = engine,
        materialLoader = materialLoader,
        environmentLoader = environmentLoader,
        renderQuality = renderQuality,
        isOpaque = true,
        cameraNode = rememberCameraNode(engine) {
            position = Position(x = 0f, y = 0.55f, z = 2.7f)
            lookAt(Position(0f, 0f, 0f))
        },
        cameraManipulator = rememberCameraManipulator(
            orbitHomePosition = Position(x = 0f, y = 0.55f, z = 2.7f),
            targetPosition = Position(x = 0f, y = 0f, z = 0f),
        ),
        mainLightNode = rememberMainLightNode(engine) {
            intensity = 95_000f
        },
        fillLightNode = rememberFillLightNode(engine) {
            intensity = 28_000f
        },
        onGestureListener = rememberOnGestureListener(
            onSingleTapConfirmed = { _, _ -> state.toggleSpin() },
            onMoveBegin = { _, _, _ -> state.autoSpin = false },
        ),
    ) {
        CubeNode(
            size = Size(0.92f),
            materialInstance = cubeMaterial,
            rotation = Rotation(x = state.pitch, y = state.yaw),
        )
        ContactShadow(
            size = Size(x = 2.2f, y = 0f, z = 2.2f),
            context = ContactShadowContext.Floor,
            position = Position(y = -0.48f),
        )
        PlaneNode(
            size = Size(3.2f, 0.02f, 3.2f),
            materialInstance = groundMaterial,
            position = Position(y = -0.62f),
        )
    }
}
