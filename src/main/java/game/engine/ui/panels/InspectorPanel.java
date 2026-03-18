package game.engine.ui.panels;

import game.engine.ECS.components.CameraComponent;
import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;
import game.engine.ui.components.ComponentHeader;
import game.engine.ui.components.ContextMenu;
import game.engine.ui.components.UIButton;
import game.engine.ui.components.Widget;
import game.engine.ui.components.props.ButtonProps;
import game.engine.ui.components.props.ComponentHeaderProps;
import game.engine.ui.core.UIComponentWithContext;
import game.engine.ui.core.UIContext;
import game.engine.ui.services.EditContext;
import game.engine.ui.services.RenameService;
import game.engine.ui.services.Selection;
import game.engine.ui.services.SelectionService;
import imgui.ImGui;

public class InspectorPanel extends UIComponentWithContext implements SelectionService.SelectionListener {

    private final SelectionService selectionService;
    private final RenameService renameService;
    private final EditContext editContext;
    private Selection currentSelection;

    public InspectorPanel(UIContext uiContext, SelectionService selectionService, RenameService renameService, EditContext editContext) {
        super(uiContext);
        this.selectionService = selectionService;
        this.renameService = renameService;
        this.editContext = editContext;
    }

    @Override
    public void onSelectionChanged(Selection selection) {
        this.currentSelection = selection;
    }

    @Override
    public void render() {
        ImGui.begin("Inspector");

        if (currentSelection != null) {
            if (currentSelection.isEntity()) {
                renderEntityInspector(currentSelection.getEntityId());
            } else if (currentSelection.isComponent()) {
                renderComponentInspector();
            } else {
                ImGui.text("Unknown selection type.");
            }
        } else {
            ImGui.text("No entity selected.");
        }

        ImGui.end();
    }

    private void renderEntityInspector(int entityId) {
        // Since we are iterating and modifying components potentially, usually
        // safe iteration is needed.
        // But UI runs on main thread and modifications are deferred or
        // immediate.
        // We iterate over available component types to see if the entity has
        // them.

        for (ComponentType componentType : uiContext.getAvailableComponentTypes()) {
            Component component = entityRegistry.getComponent(entityId, componentType);

            if (component != null) {

                ComponentHeaderProps props = new ComponentHeaderProps(componentType.name(), true, (Widget) (ctx) -> {
                    // Push a stable ID so internal ImGui widgets (like "width")
                    // don't collide between components.
                    imgui.ImGui.pushID(componentType.name() + "#" + entityId);
                    // Render component inspector without an inline remove
                    // button — removal is in the header context menu.
                    if (component instanceof CameraComponent) {
                        ((CameraComponent) component).renderInspector(ctx, entityId);
                    } else {
                        component.renderInspector();
                    }
                    imgui.ImGui.popID();
                }, null, builder -> {
                    builder.addItem("Delete Component", () -> {
                        // Remove immediately on the UI thread so the inspector
                        // updates without waiting a frame.
                        entityRegistry.removeComponent(entityId, componentType);
                        // If the removed component was selected in the
                        // inspector view, clear selection.
                        if (currentSelection != null && currentSelection.isComponent() && currentSelection.getEntityId() == entityId
                                && currentSelection.getComponentType() == componentType) {
                            selectionService.clear();
                        }
                    });
                });

                new ComponentHeader(props).render(uiContext);
            }
        }

        ImGui.separator();

        // Add Component Button
        if (ImGui.button("Add Component")) {
            ImGui.openPopup("AddComponentPopup");
        }

        if (ImGui.beginPopup("AddComponentPopup")) {
            // We use a helper to build the menu items
            ContextMenu.buildPopup(uiContext, builder -> renderAddComponentMenuItemsBuilder(entityId, builder));
            ImGui.endPopup();
        }
    }

    private void renderComponentInspector() {
        if (currentSelection == null || !currentSelection.isComponent())
            return;

        Component component = entityRegistry.getComponent(currentSelection.getEntityId(), currentSelection.getComponentType());
        if (component != null) {
            ImGui.text(currentSelection.getComponentType().name());
            ImGui.separator();

            // Push ID for safety
            ImGui.pushID(currentSelection.getComponentType().name() + "#" + currentSelection.getEntityId());
            if (component instanceof CameraComponent) {
                ((CameraComponent) component).renderInspector(uiContext, currentSelection.getEntityId());
            } else {
                component.renderInspector();
            }
            ImGui.popID();
        } else {
            ImGui.text("Selected component not found.");
            // consider clearing selection
        }
    }

    // Builder used by ContextMenu.buildPopup to populate the add-component
    // popup
    private void renderAddComponentMenuItemsBuilder(int entityId, ContextMenu.MenuBuilder builder) {
        builder.addGroup("Engine Components", group -> {
            for (ComponentType componentType : uiContext.getAvailableComponentTypes()) {
                if (componentType.getCategory() == ComponentType.Category.ENGINE) {
                    // Filter out components the entity already has
                    if (entityRegistry.getComponent(entityId, componentType) == null) {
                        group.addItem(componentType.name(), () -> uiContext.deferAddComponent(entityId, componentType));
                    }
                }
            }
        });

        builder.addSeparator();

        builder.addGroup("Custom Components", group -> {
            for (ComponentType componentType : uiContext.getAvailableComponentTypes()) {
                if (componentType.getCategory() == ComponentType.Category.CUSTOM) {
                    if (entityRegistry.getComponent(entityId, componentType) == null) {
                        group.addItem(componentType.name(), () -> uiContext.deferAddComponent(entityId, componentType));
                    }
                }
            }
        });

        builder.setFooter(new UIButton(new ButtonProps("Add Custom Component", (ctx) -> {
            // TODO: Implement logic to add a new custom component
        }, 0f, null)));
    }
}
