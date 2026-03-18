package game.engine.ui.core;

import game.engine.EntityRegistry;
import game.engine.ui.panels.InspectorPanel;
import game.engine.ui.panels.SceneHierarchyPanel;
import game.engine.ui.services.SelectionService;
import game.engine.ui.services.EditContext;
import game.engine.ui.services.RenameService;
import game.engine.ui.services.UIEventQueue;
import imgui.internal.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDir;
import imgui.type.ImInt;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the overall UI layout and hosts various UI components (panels,
 * popups). It is responsible for constructing and wiring together the various
 * services and contexts that the UI panels will use, such as the
 * SelectionService, EditContext, and RenameService.
 */
public class UIManager {
    private boolean layoutInitialized = false;
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
                game.engine.logging.Logger.error(game.engine.logging.Logger.UI,
                        "Error in panel onStateChanged: " + e.getMessage(), e);
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
            game.engine.logging.Logger.warn(game.engine.logging.Logger.UI,
                    "Pending UI actions detected; flushing before render.");
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
        ImGui.dockBuilderGetNode(ImGui.getID("EditorDockspace"));
        ImGuiViewport vp = ImGui.getMainViewport();
        ImGui.setNextWindowPos(vp.getWorkPosX(), vp.getWorkPosY());
        ImGui.setNextWindowSize(vp.getWorkSizeX(), vp.getWorkSizeY());
        ImGui.setNextWindowViewport(vp.getID());

        int hostFlags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoBackground;

        // Remove window padding so docked panels can touch the viewport edges.
        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.WindowPadding, 0f, 0f);
        ImGui.begin("EditorDockHost", hostFlags);
        int dockspaceId = ImGui.getID("EditorDockspace");
        ImGui.dockSpace(dockspaceId, 0f, 0f, ImGuiDockNodeFlags.PassthruCentralNode);

        // One-time default layout: prefer DockBuilder (internal) when
        // available; otherwise set per-window positions as a fallback. We
        // also skip if an ini already exists so we don't clobber user
        // settings.
        if (!layoutInitialized) {
            try {
                int cfg = ImGui.getIO().getConfigFlags();
                if ((cfg & ImGuiConfigFlags.DockingEnable) == 0) {
                    // Docking not enabled; nothing to do.
                    layoutInitialized = true;
                } else {
                    File ini = new File("imgui.ini");
                    if (ini.exists()) {
                        // User has an ini -> respect existing layout.
                        layoutInitialized = true;
                    } else {
                        try {
                            // Build a 3-panel layout using DockBuilder APIs.
                            int rootId = ImGui.getID("EditorDockspace");
                            ImGui.dockBuilderRemoveNode(rootId);
                            ImGui.dockBuilderAddNode(rootId, ImGuiDockNodeFlags.None);
                            ImGui.dockBuilderSetNodeSize(rootId, vp.getWorkSizeX(), vp.getWorkSizeY());

                            ImInt leftId = new ImInt();
                            ImInt restId = new ImInt();
                            ImInt centerId = new ImInt();
                            ImInt rightId = new ImInt();

                            // Split root: left (20%) | rest
                            ImGui.dockBuilderSplitNode(rootId, ImGuiDir.Left, 0.2f, leftId, restId);
                            // Split rest: center (80%) | right (20% of rest)
                            ImGui.dockBuilderSplitNode(restId.get(), ImGuiDir.Right, 0.2f, centerId, rightId);

                            // Dock the panels into the nodes by their stable
                            // window titles. These titles must match ImGui.begin()
                            // calls in each panel.
                            ImGui.dockBuilderDockWindow("Scene Hierarchy", leftId.get());
                            ImGui.dockBuilderDockWindow("Viewport", centerId.get());
                            ImGui.dockBuilderDockWindow("Inspector", rightId.get());

                            ImGui.dockBuilderFinish(rootId);

                            // Persist layout so subsequent runs use it.
                            try {
                                ImGui.saveIniSettingsToDisk("imgui.ini");
                            } catch (Exception e) {
                                game.engine.logging.Logger.warn(game.engine.logging.Logger.UI,
                                        "Could not save imgui.ini after building default layout: " + e.getMessage());
                            }

                            layoutInitialized = true;
                        } catch (Throwable t) {
                            // DockBuilder failed at runtime; fall back to
                            // per-window positioning so windows appear in the
                            // intended places even without docking APIs.
                            game.engine.logging.Logger.warn(game.engine.logging.Logger.UI,
                                    "DockBuilder invocation failed, falling back to window positioning: "
                                            + t.getMessage());

                            float totalW = vp.getWorkSizeX();
                            float totalH = vp.getWorkSizeY();
                            float leftW = totalW * 0.2f;
                            float rightW = totalW * 0.2f;
                            float centerW = Math.max(0f, totalW - leftW - rightW);
                            float x = vp.getWorkPosX();
                            float y = vp.getWorkPosY();

                            // Left: Scene Hierarchy
                            ImGui.setNextWindowPos(x, y);
                            ImGui.setNextWindowSize(leftW, totalH);
                            // Center: Viewport
                            ImGui.setNextWindowPos(x + leftW, y);
                            ImGui.setNextWindowSize(centerW, totalH);
                            // Right: Inspector
                            ImGui.setNextWindowPos(x + leftW + centerW, y);
                            ImGui.setNextWindowSize(rightW, totalH);

                            layoutInitialized = true;
                        }
                    }
                }
            } catch (Exception e) {
                game.engine.logging.Logger.warn(game.engine.logging.Logger.UI,
                        "Failed to create default layout: " + e.getMessage());
                // As a last resort mark initialized so we don't retry.
                layoutInitialized = true;
            }
        }

        ImGui.end();
        ImGui.popStyleVar();
    }
}
