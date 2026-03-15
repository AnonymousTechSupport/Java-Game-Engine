package game.engine.ui;

import game.engine.EntityRegistry;
import game.engine.ui.panels.SceneHierarchyPanel;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiWindowFlags;
import imgui.flag.ImGuiDockNodeFlags;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the overall UI layout and hosts various UI components (panels, popups).
 */
public class UIManager {
    private final List<UIComponent> components = new ArrayList<>();
    

    public UIManager(EntityRegistry entityRegistry) {
        // Register all UI components here
        SelectionContext selection = new SelectionContext();
        
        // edit context enforces single active editor
        EditContext editContext = new EditContext();
        // central rename service
        RenameService renameService = new RenameService(entityRegistry);
        SceneHierarchyPanel scene = new SceneHierarchyPanel(entityRegistry, selection, editContext, renameService);
        components.add(scene);
        components.add(new game.engine.ui.panels.InspectorPanel(entityRegistry, selection, renameService, editContext));
    }

    /**
     * Renders the main UI layout and all registered components.
     */
    public void render() {
        // Create a host dockspace that fills the main viewport. Panels are normal
        // top-level windows and can be docked by the user. We try to provide
        // an initial layout using DockBuilder if available.
        ImGuiViewport vp = ImGui.getMainViewport();
        ImGui.setNextWindowPos(vp.getWorkPosX(), vp.getWorkPosY());
        ImGui.setNextWindowSize(vp.getWorkSizeX(), vp.getWorkSizeY());
        ImGui.setNextWindowViewport(vp.getID());

        int hostFlags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoCollapse
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoBringToFrontOnFocus
                | ImGuiWindowFlags.NoBackground;

        ImGui.begin("EditorDockHost", hostFlags);
        int dockspaceId = ImGui.getID("EditorDockspace");
        ImGui.dockSpace(dockspaceId, 0f, 0f, ImGuiDockNodeFlags.PassthruCentralNode);
        ImGui.end();

        // Render all registered components. Each component should call ImGui.begin()/end()
        // with a stable window title if it wants to be dockable.
        for (UIComponent component : components) {
            component.render();
        }
    }
}
