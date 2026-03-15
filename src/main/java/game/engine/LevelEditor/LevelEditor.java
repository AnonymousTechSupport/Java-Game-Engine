package game.engine.LevelEditor;

import game.engine.EntityRegistry;
import game.engine.ui.ImGuiLayer;
import game.engine.ui.UIManager;

/**
 * The main class for the Level Editor UI.
 * This class is responsible for initializing and managing the ImGui layer and the UIManager.
 */
public class LevelEditor {
    private final ImGuiLayer imGuiLayer;
    private final UIManager uiManager;

    public LevelEditor(long glfwWindow, EntityRegistry entityRegistry, game.engine.StateManager stateManager) {
        this.imGuiLayer = new ImGuiLayer();
        this.imGuiLayer.init(glfwWindow);
        this.uiManager = new UIManager(entityRegistry);

        // When the engine transitions to PLAYING we should cancel any active inline edit
        // so that entering the game does not leave stale input widgets open. Use the
        // StateManager listener API to keep this coupling minimal.
        stateManager.addListener(new game.engine.StateManager.StateListener() {
            @Override
            public void onStateChanged(game.engine.EngineState from, game.engine.EngineState to) {
                if (to == game.engine.EngineState.PLAYING) {
                    uiManager.cancelEdits();
                }
            }
        });
    }

    /**
     * Renders the level editor UI for the current frame.
     */
    public void update() {
        imGuiLayer.beginFrame();
        uiManager.render();
        imGuiLayer.endFrame();
    }

    /**
     * Cleans up ImGui resources.
     */
    public void cleanup() {
        imGuiLayer.cleanup();
    }
}
