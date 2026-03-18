package game.engine;

import game.engine.input.InputManager;
import game.engine.renderer.Renderer;
import game.engine.renderer.OpenGLRenderer;
import game.engine.LevelEditor.LevelEditor;
import game.engine.logging.Logger;

/**
 * Manages the main game loop, including initialization, updating, and cleanup.
 */
public class GameLoop {
    private WindowHandler window;
    private InputManager input;
    private Renderer renderer;
    private Time time;
    private World world;
    private LevelEditor editor;
    private StateManager stateManager;

    public GameLoop() {
        Logger.info(Logger.ENGINE, "GameLoop created.");
        window = new WindowHandler("My Game");
    }

    private void start() {
        Logger.info(Logger.ENGINE, "Starting engine systems...");
        window.createWindow();
        time = new Time();
        this.stateManager = new StateManager(window, time);
        input = new InputManager(window.getHandle(), this.stateManager);

        renderer = new OpenGLRenderer();

        world = new World();

        // create shared editor and registry once
        EntityRegistry registry = new EntityRegistry(world);
        editor = new LevelEditor(window.getHandle(), registry, this.stateManager, renderer);
    }

    public void run() {
        try {
            start();
            loop();
        } catch (Exception e) {
            Logger.fatal("An uncaught exception occurred in the game loop.", e);
        } finally {
            stop();
        }
    }

    private void loop() {
        Logger.info(Logger.ENGINE, "Main loop started.");
        while (window.isRunning() && !window.shouldClose()) {
            time.update();
            Logger.trace(Logger.ENGINE, () -> "Frame delta: " + time.getDelta());

            input.beginFrame();
            window.pollEvents();

            // Update world state only when playing
            if (world != null && stateManager.isPlaying()) {
                world.update(time.getDelta());
            }

            // Editor UI (which includes the game viewport) should always render
            // Clear the default backbuffer before ImGui renders to avoid visual trails
            // (we render the scene into an FBO; ImGui composes the UI onto the backbuffer).
            if (window != null) {
                window.render();
            }
            if (editor != null) {
                editor.update();
                editor.getUiManager().getUiEventQueue().flush();
            }

            window.swapBuffers();
        }
    }

    /**
     * Stops the game loop and performs cleanup for resources such as the input
     * manager and window handler.
     */
    private void stop() {
        Logger.info(Logger.ENGINE, "Stopping engine systems...");
        if (input != null) {
            input.free();
        }
        if (editor != null) {
            editor.cleanup();
        }
        if (renderer != null) {
            renderer.cleanup();
        }
        if (window != null) {
            window.cleanup();
        }
        Logger.info(Logger.ENGINE, "Shutdown complete.");
    }
}
