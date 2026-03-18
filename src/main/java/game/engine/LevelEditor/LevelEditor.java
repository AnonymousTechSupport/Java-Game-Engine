package game.engine.LevelEditor;

import game.engine.EntityRegistry;
import game.engine.renderer.Renderer;
import game.engine.ui.core.ImGuiLayer;
import game.engine.ui.core.UIManager;
import game.engine.ui.core.UIController;
import game.engine.ui.panels.ViewportPanel;

/**
 * The main class for the Level Editor UI. This class is responsible for
 * initializing and managing the ImGui layer and the UIManager.
 */
public class LevelEditor {
    private final ImGuiLayer imGuiLayer;
    private final UIManager uiManager;
    private final ViewportPanel viewportPanel;

    public LevelEditor(long glfwWindow, EntityRegistry entityRegistry, game.engine.StateManager stateManager, Renderer renderer) {
        this.imGuiLayer = new ImGuiLayer();
        this.imGuiLayer.init(glfwWindow);
        this.uiManager = new UIManager(entityRegistry);

        // Initialize ViewportPanel
        this.viewportPanel = new ViewportPanel(uiManager.getUIContext(), entityRegistry.getWorld(), entityRegistry.getWorld().getRenderingSystem(),
                renderer, stateManager, uiManager.getSelectionService());
        this.uiManager.registerComponent(this.viewportPanel);

        // Register the UIController which centralizes UI reactions to engine
        // state
        // changes. The UIController will forward events to panels and run
        // cross-cutting policies such as cancelling edits on PLAYING.
        var uiController = new UIController(uiManager);
        stateManager.addListener(uiController);
    }

    public ViewportPanel getViewportPanel() {
        return viewportPanel;
    }

    public UIManager getUiManager() {
        return uiManager;
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
        viewportPanel.cleanup();
        imGuiLayer.cleanup();
    }
}
