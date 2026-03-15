package game.engine.ui.panels;

import game.engine.ECS.Entity;
import game.engine.EntityRegistry;
import game.engine.ui.EditContext;
import game.engine.ui.RenameService;
import game.engine.ui.SelectionContext;
import game.engine.ui.UIComponent;
import game.engine.ui.components.EditableLabel;
import imgui.ImGui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A UI panel that displays the list of entities in the scene.
 */
public class SceneHierarchyPanel extends UIComponent {
    private final SelectionContext selection;
    private final RenameService renameService;
    private final EditContext editContext;
    // Per-entity inline editors so each row can enter edit mode independently
    private final Map<Integer, EditableLabel> editableMap = new HashMap<>();

    public SceneHierarchyPanel(EntityRegistry entityRegistry, SelectionContext selection, EditContext editContext, RenameService renameService) {
        super(entityRegistry);
        this.selection = selection;
        this.editContext = editContext;
        this.renameService = renameService;
    }

    @Override
    public void render() {
        ImGui.begin("Scene Hierarchy");

        // Use a copy to avoid concurrency issues if another thread modifies the list
        List<Entity> entities = List.copyOf(entityRegistry.listEntities());

        for (Entity e : entities) {
            int entityId = e.getId();
            String currentName = e.getName() != null ? e.getName() : ("Entity " + entityId);

            // Obtain or create per-entity EditableLabel
            EditableLabel editable = editableMap.computeIfAbsent(entityId,
                id -> new EditableLabel("entity-" + id, currentName, editContext)
            );

            // Keep visible text in sync when not editing
            if (!editable.isEditing()) {
                editable.setText(currentName);
            }

            // Render the editable label
            editable.render(currentName, entityId, new EditableLabel.Callback() {
                @Override public void onCommit(String newText) {
                    if (renameService != null) renameService.commitRename(entityId, newText);
                }
                @Override public void onCancel() { /* nothing */ }
            }, () -> {
                // onSelect: executed on left-click
                selection.setSelectedEntityId(entityId);
            });

            // Right-click context menu: delete or start inline rename
            if (ImGui.beginPopupContextItem()) {
                if (ImGui.menuItem("Delete Entity")) {
                    entityRegistry.destroyEntity(entityId);
                    if (selection.getSelectedEntityId() == entityId) {
                        selection.clear();
                    }
                    editableMap.remove(entityId);
                    // Also notify EditContext to cancel if this entity was being edited
                    if (editContext.isEditing(entityId)) {
                        editContext.cancel();
                    }
                }
                if (ImGui.menuItem("Rename Entity")) {
                    // Request edit start. If another edit is active, EditContext will
                    // block and focus the active one.
                    boolean started = editContext.requestStartEdit(entityId, editable, currentName);
                    if (started) {
                        selection.setSelectedEntityId(entityId);
                    }
                }
                ImGui.endPopup();
            }
        }
        ImGui.end();
    }
}
