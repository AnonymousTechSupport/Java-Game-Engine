# Java Game Engine

A lightweight 2D game engine written in Java, designed for simplicity and modularity. It provides a foundational ECS (Entity-Component-System) architecture, a decoupled rendering pipeline using OpenGL, and basic window and input management via LWJGL.

## Project Specifications

*   **Language**: Java 11+
*   **Build Tool**: Maven
*   **Core Libraries**:
    *   **LWJGL 3**: A low-level Java library that provides access to native APIs like OpenGL.
        *   **OpenGL**: Used for hardware-accelerated 2D rendering.
        *   **GLFW**: Used for creating and managing windows, contexts, and user input.

## Folder Structure

*   `src/main/java/game/engine/`: The root package for all engine source code.
    *   `ECS/`: Contains the Entity-Component-System implementation, including components and systems.
    *   `input/`: Handles user input management, wrapping GLFW callbacks.
    *   `logging/`: Provides a simple, static utility for logging engine events.
    *   `renderer/`: Manages the rendering pipeline, with abstractions for different rendering APIs.
*   `pom.xml`: The Maven project file, defining dependencies and build settings.

## How to Build and Run

### Prerequisites

*   Java Development Kit (JDK) 11 or newer.
*   Apache Maven.

### Cloning the Project

To get a local copy of the project, clone the repository using Git:

```bash
git clone <repository-url>
cd <repository-directory>
```

### Building with Maven

The project is managed by Maven, which handles dependency resolution and compilation. To build the project, run the following command from the root directory:

```bash
mvn clean install
```

This will compile the source code and package it into a JAR file in the `target/` directory.

### Running the Engine

After a successful build, you can run the main application entry point:

```bash
mvn exec:java -Dexec.mainClass="game.engine.Main"
```
