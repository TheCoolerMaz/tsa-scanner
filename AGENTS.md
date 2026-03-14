# AGENTS.md — TSA Scanner

## Project

**TSA Scanner** — "That's a weapon?" game jam entry.
TSA checkpoint simulator inspired by Papers Please. libGDX 1.13.1, Java 11, pixel art.

## Architecture

```
core/src/main/java/com/tsascanner/
├── TsaGame.java              — Main game class (singleton via INSTANCE)
├── assets/
│   └── Assets.java           — Central asset loader (AssetManager wrapper)
├── ecs/
│   ├── components/            — Ashley ECS components (Position, Velocity, Sprite, Collider, Tag)
│   └── systems/               — Ashley ECS systems (Movement, Render)
├── graphics/
│   ├── ShaderPipeline.java    — Post-processing pipeline (gdx-vfx)
│   ├── IndexedSprite.java     — Palette-indexed sprite support
│   └── effects/               — CRT, Dither, Effect base class
├── screens/
│   ├── GameScreen.java        — Base screen class
│   ├── ScreenManager.java     — Stack-based screen management with transitions
│   ├── SplashScreen.java      — Loading/splash
│   ├── MenuScreen.java        — Main menu
│   ├── PlayScreen.java        — Main gameplay (TSA scanner)
│   ├── SettingsScreen.java    — Shader/audio settings
│   └── transitions/           — Fade, Slide transitions
├── ui/
│   ├── UiKit.java             — UI helper utilities
│   └── KeyboardNav.java       — Keyboard navigation for menus
└── util/
    └── Prefs.java             — Persistent preferences
```

## Key Constraints

- **Java only** (not Kotlin)
- **WebGL/GLES 1.00 compatible shaders** — no GLSL 3.00, no `#version` directives
- **libGDX 1.13.1** — don't upgrade without reason
- **Ashley ECS 1.7.4** — entity component system for game objects

## Running

```bash
# Compile check (no display needed)
./gradlew lwjgl3:classes

# Run on Wayland
WAYLAND_DISPLAY=wayland-1 XDG_RUNTIME_DIR=/run/user/1000 ./gradlew lwjgl3:run

# Build JAR
./gradlew lwjgl3:jar
```

## Design

See `DESIGN.md` for the full game design document.

## Code Patterns

- **Screens** extend `GameScreen`, use `manager.push()` / `manager.pop()` for navigation
- **Assets** loaded through `TsaGame.INSTANCE.assets` (singleton pattern)
- **Shaders** managed by `ShaderPipeline`, configured via `Prefs`
- **ECS** via Ashley — create entities with components, add systems to engine
- **UI** uses Scene2D with `KeyboardNav` for gamepad/keyboard accessibility

## Working Conventions

- Assets go in `assets/` directory (root level, symlinked into lwjgl3 resources)
- Pixel art at native resolution, scaled up via viewport
- Keep shader pipeline intact — it handles CRT/bloom/vignette/dither post-processing
- Test with `./gradlew lwjgl3:classes` before committing (headless compile check)
