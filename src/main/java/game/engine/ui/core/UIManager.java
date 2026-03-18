package game.engine.ui.core;

import game.engine.EntityRegistry;
import game.engine.ui.panels.InspectorPanel;
import game.engine.ui.panels.SceneHierarchyPanel;
import game.engine.ui.services.SelectionService;
import game.engine.ui.services.EditContext;
import game.engine.ui.services.RenameService;
import game.engine.ui.services.UIEventQueue;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the overall UI layout and hosts various UI components (panels,
 * popups). It is responsible for constructing and wiring together the various
 * services and contexts that the UI panels will use, such as the
 * SelectionService, EditContext, and RenameService.
 */
public class UIManager {
    private final List<UIComponent> components = new ArrayList<>();
    private final EditContext editContext;
    private final SelectionService selectionService;
    private final RenameService renameService;
    private final UIEventQueue uiEventQueue;
    private final UIContext uiContext;

    public UIManager(EntityRegistry entityRegistry) {
        // Centralized services that manage UI state and behavior
        this.selectionService = new SelectionService();
        this.editContext = new EditContext();
        this.renameService = new RenameService(entityRegistry);
        this.uiEventQueue = new UIEventQueue();
        this.uiContext = new UIContext(uiEventQueue, entityRegistry);

        // Register all UI components here, injecting the required services
        var sceneHierarchyPanel = new SceneHierarchyPanel(uiContext, selectionService, editContext, renameService);
        var inspectorPanel = new InspectorPanel(uiContext, selectionService, renameService, editContext);

        // Register panels as listeners to selection changes
        selectionService.addListener(sceneHierarchyPanel);
        selectionService.addListener(inspectorPanel);

        components.add(sceneHierarchyPanel);
        components.add(inspectorPanel);
    }

    public UIEventQueue getUiEventQueue() {
        return uiEventQueue;
    }

    public SelectionService getSelectionService() {
        return selectionService;
    }

    public UIContext getUIContext() {
        return uiContext;
    }

    public void registerComponent(UIComponent component) {
        components.add(component);
    }

    /**
     * Cancels any active inline editor managed by the UI. This is a safe
     * operation to call from external contexts, like a StateListener, to ensure
     * UI state is cleaned up when, for example, exiting the editor.
     */
    public void cancelEdits() {
        if (this.editContext != null) {
            this.editContext.cancel();
        }
    }

    /** Forward state changes to registered components so they can react. */
    public void onStateChanged(game.engine.EngineState from, game.engine.EngineState to) {
        for (UIComponent c : components) {
            try {
                c.onStateChanged(from, to);
            } catch (Exception e) {
                game.engine.logging.Logger.error(game.engine.logging.Logger.UI, "Error in panel onStateChanged: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Renders the main UI layout and all registered components.
     */
    public void render() {
        // Create a host dockspace that fills the main viewport. Panels are
        // normal
        // top-level windows and can be docked by the user.
        setupDockspace();

        // If there are pending deferred UI actions, log a warning then flush
        // them.
        if (uiEventQueue.hasPending()) {
            game.engine.logging.Logger.warn(game.engine.logging.Logger.UI, "Pending UI actions detected; flushing before render.");
        }
        // Flush any pending UI-deferred actions so they execute before
        // rendering.
        uiEventQueue.flush();

        // Render all registered components. Each component should call
        // ImGui.begin()/end()
        // with a stable window title if it wants to be dockable.
        for (UIComponent component : components) {
            component.render();
        }
    }

    private void setupDockspace() {
        ImGuiViewport vp = ImGui.getMainViewport();
        ImGui.setNextWindowPos(vp.getWorkPosX(), vp.getWorkPosY());
        ImGui.setNextWindowSize(vp.getWorkSizeX(), vp.getWorkSizeY());
        ImGui.setNextWindowViewport(vp.getID());

        int hostFlags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoBackground;

        // Remove window padding so docked panels can touch the viewport edges.
        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.WindowPadding, 0f, 0f);
        ImGui.begin("EditorDockHost", hostFlags);
        int dockspaceId = ImGui.getID("EditorDockspace");
        ImGui.dockSpace(dockspaceId, 0f, 0f, ImGuiDockNodeFlags.PassthruCentralNode);
        ImGui.end();
        ImGui.popStyleVar();
    }
}
