package game.engine;


import game.engine.ECS.EntityManager;
import game.engine.input.InputManager;
import game.engine.renderer.Renderer;
import game.engine.renderer.OpenGLRenderer;
import game.engine.renderer.RenderSurface;
import game.engine.LevelEditor.LevelEditor;


import game.engine.logging.Logger;

/**
 * Manages the main game loop, including initialization, updating, and cleanup.
 */
public class GameLoop {
    private WindowHandler window;
    private InputManager input;
    private EntityManager ecs;
    private Renderer renderer;
    private RenderSurface surface;
    private Time time;
    private World world;
    private LevelEditor editor;
    private StateManager stateManager;

    public GameLoop() {
        Logger.info(Logger.ENGINE, "GameLoop created.");
        window = new WindowHandler("My Game");
        ecs = new EntityManager();
    }

    private void start() {
        Logger.info(Logger.ENGINE, "Starting engine systems...");
        window.createWindow();
        time = new Time();
        this.stateManager = new StateManager(window, time);
        input = new InputManager(window.getHandle(), this.stateManager);

        surface = new RenderSurface() {
            @Override public int getWidth() { return window.getWidth(); }
            @Override public int getHeight() { return window.getHeight(); }
        };

        renderer = new OpenGLRenderer(surface);

        world = new World(ecs);
        world.initDemoEntities(window.getWidth(), window.getHeight());
        // create shared editor and registry once
        EntityRegistry registry = new EntityRegistry(ecs, world);
        editor = new LevelEditor(window.getHandle(), registry, this.stateManager);
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

            if (renderer != null) {
                game.engine.renderer.RenderContext ctx = new game.engine.renderer.RenderContext(
                        window.getWidth(), window.getHeight(), time.getDelta());
                
                renderer.beginFrame(ctx);
                if (world != null) world.render(renderer);
                renderer.endFrame();
            }

            // Editor UI should only render when in editor state
            if (editor != null && stateManager.isEditor()) {
                editor.update();
            }

            window.swapBuffers();
        }
    }

    /**
     * Stops the game loop and performs cleanup for resources such as the input manager and window handler.
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
