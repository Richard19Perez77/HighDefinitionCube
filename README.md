# High Definition Cube

An Android demo of a spinning 3D cube that looks expensive on a flagship and stays runnable on a mid-range phone.

The cube is the mascot. The product is the **constraint layer**: renderer, quality tier, and a live HUD so you can see what extra definition costs in fps, frame time, and memory.

This APK is also the live companion for a blog series on making graphics with AI, then scaling them to a real device.

<div align="center">

[![Watch the High Definition Cube demo](https://img.youtube.com/vi/PRyFFdTmKhk/hqdefault.jpg)](https://www.youtube.com/shorts/PRyFFdTmKhk)

[Watch the demo on YouTube](https://www.youtube.com/shorts/PRyFFdTmKhk)

</div>

## The 60-second version

Anyone can draw a cube. This app asks a harder question: **how much quality can this phone hold after it is warm?**

- **Filament** (via [SceneView](https://github.com/sceneview/sceneview)) is the spectacular path: PBR materials, dual lights, bloom, contact shadows.
- **Compose Canvas** is the teaching path: the same cube, CPU-projected faces, no GPU scene graph.
- **Auto quality never picks Max.** Max (4x MSAA, high shadows, DoF) is a switch you turn on to show slowdown, not a default.
- **Remix** invents a new metal/paint look on-device. That is the phone-safe version of AI art direction — no cloud GPU, no 4K texture dump.

## Run it

Open the project in Android Studio and run `app` on a phone. The interesting test is your everyday device, not an emulator.

| Setting | Value |
|---|---|
| `minSdk` / `targetSdk` | 24 / 37 |
| SceneView / Filament | 4.32.0 |
| Kotlin | 2.4.10 |
| AGP | 9.4.0 |

```powershell
.\gradlew.bat :app:installDebug
```

On the phone:

1. Leave **Auto** on. Note fps after 30 seconds, then after 3 minutes.
2. Switch to **Max**. Same two measurements.
3. Flip **Filament → Canvas**. Same gestures.

Those three screenshots are the blog.

## Controls

| Gesture / control | What it does |
|---|---|
| Drag (Filament) | Orbit the camera |
| Pinch (Filament) | Zoom |
| Drag (Canvas) | Spin the cube; release for a flick |
| Tap the cube | Pause / resume auto-spin |
| Speed slider | Degrees per second when auto-spin is on |
| Renderer chips | Filament vs Canvas |
| Quality chips | Auto, Phone-safe, Balanced, Max |
| Look chips | Chrome, gold, candy, glass, or Remix |

The HUD shows renderer, look, fps, frame time, Java heap, RAM, CPU cores, GLES version, and why Auto chose that tier.

## Quality is a budget

| Chip | Filament preset | What you pay for |
|---|---|---|
| Phone-safe | `RenderQuality.Performance` | 1x MSAA, no SSAO/bloom — holds fps when the SoC is warm |
| Balanced | `RenderQuality.Default` | 2x MSAA, SSAO, bloom — default product look |
| Max | `RenderQuality.Cinematic` | 4x MSAA, high shadows, DoF — trailer preset, opt-in |
| Auto | Phone-safe or Balanced | From RAM and CPU cores. Never Max. |

A cube does not need more triangles. Engines spend GPU on lighting. That is why Canvas and Filament can draw the same six faces and look like different decades.

## Project map

```
app/src/main/java/com/rick/highdefinitioncube/
  MainActivity.kt
  graphics/
    DeviceProfile.kt      RAM, cores, Auto recommendation
    CubeDemoState.kt      spin, fps, renderer, quality
    CubeLooks.kt          PBR looks + on-device Remix
  ui/
    CubeApp.kt            HUD + controls
    FilamentCubeScene.kt  SceneView / Filament cube
    CanvasCubeScene.kt    Compose Canvas cube
```

## Blog notes

Worth writing:

1. Spectacular cube in a weekend (SceneView).
2. Same cube, two engines (Filament vs Canvas).
3. Auto must be conservative — 10-second fps vs 3-minute fps.
4. AI overproduces; phones underconsume. Remix first, then 512 / 1K / 2K textures with the HUD showing the bill.

Not in this build: AR, Unity/Godot-as-a-library, on-device diffusion, Vulkan from scratch.

https://github.com/user-attachments/assets/266b06a5-2216-41fc-86c0-1253cc5c1ece

## License

Personal demo and blog companion. SceneView is Apache 2.0.
