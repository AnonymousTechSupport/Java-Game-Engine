package game.engine.ui.panels;

import game.engine.ECS.Entity;
import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;
import game.engine.ui.components.EditableLabel;
import game.engine.ui.core.UIContext;
import game.engine.ui.core.UIComponentWithContext;
import game.engine.ui.services.EditContext;
import game.engine.ui.services.RenameService;
import game.engine.ui.services.Selection;
import game.engine.ui.services.SelectionService;
import game.engine.ui.components.ComponentHeader;
import game.engine.ui.components.ContextMenu;
import imgui.ImGui;

import java.util.Map;

/**
 * Inspector panel that displays the name and properties of the currently
 * selected entity. It uses an EditableLabel for inline renaming and listens
 * to the SelectionService to know which entity to display.
 */
public class InspectorPanel extends UIComponentWithContext implements SelectionService.SelectionListener {
    private final SelectionService selectionService;
    private final RenameService renameService;
    private final EditContext editContext;
    private final EditableLabel editable;
    private Selection currentSelection = null;

    public InspectorPanel(UIContext context, SelectionService selectionService, RenameService renameService, EditContext editContext) {
        super(context);
        this.selectionService = selectionService;
        this.renameService = renameService;
        this.editContext = editContext;
        // Reuse one EditableLabel for inspector renames.
        this.editable = new EditableLabel("inspector", "", editContext);
    }

    @Override
    public void onSelectionChanged(Selection newSelection) {
        // When selection changes, cancel any edit active in this panel.
        if (editable.isEditing()) {
            editable.cancel();
        }
        this.currentSelection = newSelection;
    }

    @Override
    public void render() {
        ImGui.begin("Inspector");

        if (currentSelection == null) {
            ImGui.text("No entity selected");
            ImGui.end();
            return;
        }

        int entityId = currentSelection.getEntityId();
        Entity e = entityRegistry.getEntity(entityId);

        if (e == null) {
            ImGui.text("Selected entity not found");
            ImGui.end();
            // Edge case: If the entity was deleted, it might still be selected for
            // a frame. Clear the selection to prevent holding a stale ID.
            selectionService.clear();
            return;
        }

        String currentName = e.getName() != null ? e.getName() : ("Entity " + entityId);

        // Keep the label's text synchronized with the entity's name, but only
        // when the user is not actively editing it.
        if (!editable.isEditing()) {
            editable.setText(currentName);
        }

        float rowHeight = ImGui.getFontSize() * 1.25f + 6.0f;
        editable.render(currentName, entityId, true, new EditableLabel.Callback() {
            @Override
            public void onCommit(String newText) {
                renameService.commitRename(entityId, newText);
            }
            @Override
            public void onCancel() {
                // No special action needed; EditableLabel handles its own state.
            }
        }, () -> selectionService.setSelected(new Selection(entityId)), rowHeight);

        ImGui.separator();

        if (currentSelection.isComponent()) {
            renderComponentInspector();
        } else {
            renderEntityInspector(entityId);
        }


        ImGui.end();
    }

    private void renderEntityInspector(int entityId) {
        ImGui.text("Components");

        if (ImGui.button("Add Component")) {
            ImGui.openPopup("add_component_popup");
        }

        if (ImGui.beginPopup("add_component_popup")) {
            ContextMenu.buildPopup(uiContext, builder -> renderAddComponentMenuItemsBuilder(entityId, builder));
            ImGui.endPopup();
        }

        Map<ComponentType, Component> components = uiContext.getComponents(entityId);
        for (Map.Entry<ComponentType, Component> entry : components.entrySet()) {
            ComponentType componentType = entry.getKey();
            Component component = entry.getValue();

            // Attach the entity id into the ImGui id so each component header's
            // open/closed state is stable per-entity and doesn't affect others.
                new ComponentHeader(componentType.name() + "##" + entityId, false, (ctx) -> {
                    // Push a stable ID so internal ImGui widgets (like "width") don't collide between components.
                    imgui.ImGui.pushID(componentType.name() + "#" + entityId);
                    // Render component inspector without an inline remove button — removal is in the header context menu.
                    component.renderInspector();
                    imgui.ImGui.popID();
                }, null, builder -> {
                    builder.addItem("Delete Component", () -> {
                        // Remove immediately on the UI thread so the inspector updates without waiting a frame.
                        entityRegistry.removeComponent(entityId, componentType);
                        // If the removed component was selected in the inspector view, clear selection.
                        if (currentSelection != null && currentSelection.isComponent()
                                && currentSelection.getEntityId() == entityId
                                && currentSelection.getComponentType() == componentType) {
                            selectionService.clear();
                        }
                    });
                }).render(uiContext);
        }
    }


    private void renderComponentInspector() {
        Component component = entityRegistry.getComponent(currentSelection.getEntityId(), currentSelection.getComponentType());
        if (component != null) {
            ImGui.text(currentSelection.getComponentType().name());
            ImGui.separator();
            component.renderInspector();
        } else {
            ImGui.text("Selected component not found.");
            // consider clearing selection
        }
    }

    // Builder used by ContextMenu.buildPopup to populate the add-component popup
    private void renderAddComponentMenuItemsBuilder(int entityId, ContextMenu.MenuBuilder builder) {
        builder.addGroup("Engine Components", group -> {
            for (ComponentType componentType : uiContext.getAvailableComponentTypes()) {
                if (componentType.getCategory() == ComponentType.Category.ENGINE) {
                    group.addItem(componentType.name(), () -> uiContext.deferAddComponent(entityId, componentType));
                }
            }
        });

        builder.addSeparator();

        builder.addGroup("Custom Components", group -> {
            for (ComponentType componentType : uiContext.getAvailableComponentTypes()) {
                if (componentType.getCategory() == ComponentType.Category.CUSTOM) {
                    group.addItem(componentType.name(), () -> uiContext.deferAddComponent(entityId, componentType));
                }
            }
        });

        builder.setFooter(new game.engine.ui.components.UIButton("Add Custom Component", (ctx) -> {
            // TODO: Implement logic to add a new custom component
        }));
    }
}
