package game.engine.ui.panels;

import game.engine.ECS.Entity;
import game.engine.EntityRegistry;
import game.engine.ui.UIComponent;
import game.engine.ui.popups.RenamePopup;
import imgui.ImGui;

import java.util.List;

/**
 * A UI panel that displays the list of entities in the scene.
 */
public class SceneHierarchyPanel extends UIComponent {
    private int selectedEntityId = -1;

    public SceneHierarchyPanel(EntityRegistry entityRegistry) {
        super(entityRegistry);
    }

    @Override
    public void render() {
        ImGui.begin("Scene Hierarchy");

        List<Entity> entities = entityRegistry.listEntities();
        for (Entity e : entities) {
            String label = e.getName() + "##" + e.getId();
            boolean isSelected = (selectedEntityId == e.getId());
            if (ImGui.selectable(label, isSelected)) {
                selectedEntityId = e.getId();
            }

            // Right-click context menu for the entity
            if (ImGui.beginPopupContextItem()) {
                if (ImGui.menuItem("Delete Entity")) {
                    entityRegistry.destroyEntity(e.getId());
                    if (selectedEntityId == e.getId()) selectedEntityId = -1;
                }
                if (ImGui.menuItem("Rename Entity")) {
                    RenamePopup.requestRename(e.getId(), entityRegistry);
                }
                ImGui.endPopup();
            }
        }
        ImGui.end();
    }
}
