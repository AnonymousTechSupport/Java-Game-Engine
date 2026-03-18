package game.engine.ui.panels;

import game.engine.ECS.Entity;
import game.engine.ECS.Templates.BaseEntityTemplate;
import game.engine.ECS.Templates.EntityTemplates;
import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;
import game.engine.ui.core.UIComponentWithContext;
import game.engine.ui.core.UIContext;
import game.engine.ui.services.EditContext;
import game.engine.ui.services.RenameService;
import game.engine.ui.services.Selection;
import game.engine.ui.services.SelectionService;
import game.engine.ui.components.ContextMenu;
import game.engine.ui.components.UIButton;
import game.engine.ui.components.*;
import game.engine.ui.components.props.ButtonProps;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A UI panel that displays the list of entities in the scene hierarchy.
 * It allows selecting entities, renaming them, and deleting them. This panel
 * listens to selection changes to highlight the selected entity.
 */
public class SceneHierarchyPanel extends UIComponentWithContext implements SelectionService.SelectionListener {
    private final SelectionService selectionService;
    private final RenameService renameService;
    private final EditContext editContext;
    private final Map<Integer, EntityRow> rowMap = new HashMap<>();
    private final Map<Integer, Boolean> expanded = new HashMap<>();
    private Selection currentSelection = null;

    public SceneHierarchyPanel(UIContext context,
                               SelectionService selectionService,
                               EditContext editContext,
                               RenameService renameService) {
        super(context);
        this.selectionService = selectionService;
        this.editContext = editContext;
        this.renameService = renameService;
    }

    @Override
    public void onSelectionChanged(Selection newSelection) {
        this.currentSelection = newSelection;
        if (newSelection != null && newSelection.isEntity()) {
            expanded.put(newSelection.getEntityId(), true);
        }
    }

    @Override
    public void render() {
        ImGui.begin("Scene Hierarchy");
        // Slightly larger frame/item spacing for taller rows.
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 2.0f, 3.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 1.0f, 10.0f);

        List<Entity> entities = List.copyOf(entityRegistry.listEntities());

        // Render each entity as a row in the hierarchy. We maintain a map of entityId -> EntityRow to preserve
        // per-row UI state (like rename text and expanded/collapsed) across frames.
        for (Entity e : entities) {
            int entityId = e.getId();
            String currentName = e.getName() != null ? e.getName() : ("Entity " + entityId);

            EntityRow row = rowMap.computeIfAbsent(entityId,
                id -> new EntityRow(id, currentName, editContext, uiContext)
            );

            if (!row.isEditing()) row.setText(currentName);

            row.setExpanded(expanded.getOrDefault(entityId, false));

            Map<ComponentType, Component> componentsWithInstances = uiContext.getComponents(entityId);
            List<ComponentType> components = new ArrayList<>(componentsWithInstances.keySet());

            row.render(currentName, currentSelection, components, new EntityRow.Callback() {
                @Override public void onSelect(Selection selection) {
                    selectionService.setSelected(selection);
                }

                @Override public void onRename(int id, String newName) {
                    // If newName==null it's a request to start editing from the menu
                    if (newName == null) {
                        // request start edit via EditContext using the row's editable
                        EntityRow r = rowMap.get(id);
                        if (r != null) {
                            boolean started = editContext.requestStartEdit(id, r.getEditable(), currentName);
                            if (started) selectionService.setSelected(new Selection(id));
                        }
                    } else {
                        renameService.commitRename(id, newName);
                    }
                }
                @Override public void onDelete(int id) { handleDeleteEntity(id); }
                @Override public void onToggleExpand(int id, boolean newState) { expanded.put(id, newState); }

                @Override
                public void onAddComponent(int entityId) {
                    ImGui.openPopup("add_component_popup_hierarchy_" + entityId);
                }

                @Override
                public void onComponentSelected(int entityId, ComponentType type) {
                    selectionService.setSelected(new Selection(entityId, type));
                }
            });
            if (ImGui.beginPopup("add_component_popup_hierarchy_" + entityId)) {
                renderAddComponentMenuItems(entityId);
                ImGui.endPopup();
            }            
        }
        
        // Create an invisible full-width/height button at the end of the
        // list to capture right-clicks on the empty area.
        float availX = ImGui.getContentRegionAvailX();
        float availY = ImGui.getContentRegionAvailY();
        // Only create the invisible button if there is visible space remaining.
        if (availX > 1f && availY > 1f) {
            ImGui.invisibleButton("hierarchy_bg", availX, availY);
            if (ImGui.beginPopupContextItem()) {
                ContextMenu.buildPopup(uiContext, builder -> {
                    builder.addSubMenu("Add Entity", submenu -> {
                        for (BaseEntityTemplate template : EntityTemplates.getInstance()) {
                            String label = template.getName();
                            submenu.addItem(label, () -> uiContext.defer(() -> {
                                int newId = template.instantiate(entityRegistry);
                                selectionService.setSelected(new Selection(newId));
                            }));
                        }

                    });
                });
                ImGui.endPopup();
            }
        }
        
        // restore style vars for the panel
        ImGui.popStyleVar(2);
        ImGui.end();
    }

    // Render the "Add Component" context menu for a given entity, grouping engine and custom components separately.
    private void renderAddComponentMenuItems(int entityId) {
        ContextMenu.buildPopup(uiContext, builder -> {
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
            builder.setFooter(new UIButton(new ButtonProps("Add Custom Component", (ctx) -> {
                // TODO: Implement logic to add a new custom component
            }, 0f, null)));
        });
    }

    // Handle deletion of an entity: cancel any active edits, enqueue the destroy action, and clean up UI state.
    private void handleDeleteEntity(int entityId) {
        // Cancel any active edit immediately (UI thread) and enqueue destroy on engine side.
        if (editContext.isEditing(entityId)) {
            editContext.cancel();
        }
        uiContext.deferDestroyEntity(entityId);
        // remove any per-entity UI state we maintain
        rowMap.remove(entityId);
        expanded.remove(entityId);
        // If the deleted entity was selected, clear the selection.
        if (currentSelection != null && currentSelection.isEntity() && currentSelection.getEntityId() == entityId) {
            selectionService.clear();
        }
    }
}
