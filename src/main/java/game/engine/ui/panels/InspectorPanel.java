package game.engine.ui.panels;

import game.engine.ECS.Entity;
import game.engine.EntityRegistry;
import game.engine.ui.EditContext;
import game.engine.ui.RenameService;
import game.engine.ui.SelectionContext;
import game.engine.ui.UIComponent;
import game.engine.ui.components.EditableLabel;
import imgui.ImGui;

/**
 * Inspector panel — shows the currently selected entity's name and basic details.
 * Uses an EditableLabel for inline renames and delegates actual rename commits to
 * the centralized RenameService.
 */
public class InspectorPanel extends UIComponent {
    private final SelectionContext selection;
    private final RenameService renameService;
    private final EditContext editContext;
    private final EditableLabel editable;
    private int prevSelectedId = -1; // track selection to avoid extra work

    /**
     * Create an InspectorPanel.
     * @param registry underlying entity registry
     * @param selection shared selection context
     * @param renameService centralized rename applier
     * @param editContext shared edit context
     */
    public InspectorPanel(EntityRegistry registry, SelectionContext selection, RenameService renameService, EditContext editContext) {
        super(registry);
        this.selection = selection;
        this.renameService = renameService;
        this.editContext = editContext;
        this.editable = new EditableLabel("inspector", "", editContext);
    }

    @Override
    public void render() {
        ImGui.begin("Inspector");

        int id = selection.getSelectedEntityId();

        // When selection changes, cancel any edit that might be active here.
        if (id != prevSelectedId) {
            if (editable.isEditing()) {
                editable.cancel();
            }
        }
        prevSelectedId = id;

        if (id < 0) {
            ImGui.text("No entity selected");
            ImGui.end();
            return;
        }

        Entity e = entityRegistry.getEntity(id);
        if (e == null) {
            ImGui.text("Selected entity not found");
            ImGui.end();
            // If the entity is gone, ensure we don't hold a stale selection
            selection.clear();
            return;
        }

        String currentName = e.getName() != null ? e.getName() : ("Entity " + id);

        // Keep text updated if not editing
        if (!editable.isEditing()) {
            editable.setText(currentName);
        }

        editable.render(currentName, id, new EditableLabel.Callback() {
            @Override
            public void onCommit(String newText) {
                renameService.commitRename(id, newText);
            }

            @Override
            public void onCancel() {
                // EditableLabel handles internal state; nothing extra to do
            }
        }, () -> {
            // onSelect: executed on left-click (for consistency, though redundant here)
            selection.setSelectedEntityId(id);
        });

        ImGui.separator();
        ImGui.text("Components:");
        ImGui.text("[component list not implemented]");

        ImGui.end();
    }
}
