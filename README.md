# TSA Scanner

**"That's a weapon?"** — A game jam entry.

TSA checkpoint simulator inspired by *Papers Please*. Bags roll on a conveyor belt; use the x-ray scanner to inspect contents. Flag items as weapons or let them pass. Some weapons are disguised as harmless items, some harmless items look suspicious. Time pressure, quota system, escalating difficulty.

## Tech

- **Engine:** libGDX 1.13.1
- **Language:** Java 11
- **ECS:** Ashley 1.7.4
- **Post-processing:** gdx-vfx (CRT, bloom, vignette, dither)
- **Style:** Pixel art
- **Platforms:** Desktop (LWJGL3), Web (GWT/WebGL)

## Running

```bash
# Desktop
WAYLAND_DISPLAY=wayland-1 XDG_RUNTIME_DIR=/run/user/1000 ./gradlew lwjgl3:run

# Build JAR
./gradlew lwjgl3:jar

# Web build
./gradlew html:dist
```

## Project Structure

```
core/           Game logic, screens, ECS, shaders, assets, UI
lwjgl3/         Desktop launcher
html/           Web/GWT launcher
assets/         Art, audio, UI skins, shaders
```

## License

Game jam entry — all rights reserved unless otherwise noted.
