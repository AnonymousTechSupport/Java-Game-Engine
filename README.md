# Java Game Engine

Lightweight 2D game engine in Java with a simple ECS, OpenGL rendering (via LWJGL), basic window/input handling and an ImGui-based editor layer.

Key points

- Language: Java 17 (configured in `pom.xml`)
- Build: Maven (`pom.xml`) with platform profiles for LWJGL natives (`windows`, `macos`, `linux`)
- Core libs: LWJGL 3, ImGui Java bindings, JOML, Dominion ECS

Quick start

- Requirements: JDK 17+, Maven
- Clone the repo and build:

```bash
git clone <repo-url>
cd <repo-directory>
mvn clean install
```

- Run the engine (uses the main class `game.engine.Main`):

```bash
mvn exec:java -Dexec.mainClass="game.engine.Main"
```

Project layout

- `src/main/java/game/engine/` — root package
  - `ECS/` — entity, components, systems and templates (`Entity`, components, `RenderingSystem`)
  - `input/` — input and key state handling (`InputManager`, `KeyboardState`)
  - `renderer/` and `render/` — renderer abstractions and OpenGL implementation (`Renderer`, `OpenGLRenderer`, `RenderContext`, `Framebuffer`, `Camera`)
  - `ui/` — ImGui-based editor: panels, components and services (scene hierarchy, inspector, viewport)
  - `LevelEditor/` — editor-specific classes and camera controller
  - `logging/` — lightweight logger (`Logger`, `LogLevel`)
  - core classes: `Main`, `GameLoop`, `World`, `Time`, `StateManager`, `EntityRegistry`, `ComponentRegistry`

Dependencies and notes

- Native LWJGL libraries are selected by Maven profiles in `pom.xml` (look for `lwjgl.natives`).
- ImGui Java bindings are included and used for the in-engine editor layer (`ImGuiLayer` and UI components under `ui/components`).
- Dominion ECS dependencies are present (`dominion-ecs-api`, `dominion-ecs-engine`).

Development

- Use `mvn clean install` to compile and package.
- Run the main entry with `mvn exec:java -Dexec.mainClass="game.engine.Main"`.
- The codebase targets a modular, extensible engine core; add systems or components under `ECS/` and register them via `EntityRegistry`/`ComponentRegistry`.

Useful files

- `pom.xml` — build, dependencies and platform profiles
- `src/main/java/game/engine/Main.java` — application entry point
- `src/main/java/game/engine/LevelEditor/LevelEditor.java` — editor bootstrap

If you need anything added (contribution guide, examples, tests), open an issue or request specific changes.
