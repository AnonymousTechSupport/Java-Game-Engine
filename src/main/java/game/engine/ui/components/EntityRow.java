package game.engine.ui.components;

import game.engine.ECS.components.ComponentType;
import game.engine.ui.core.UIContext;
import game.engine.ui.components.props.EditableLabelProps;
import game.engine.ui.services.Selection;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.ImVec2;

import java.util.List;

/**
 * Renders a single entity row in the hierarchy with expand/collapse and
 * inline renaming support via EditableLabel.
 */
public class EntityRow {
    private final int entityId;
    private final EditableLabel editable;
    private boolean expanded;
    private final ContextMenu contextMenu;
    private final UIContext uiContext;


    public interface Callback {
        void onSelect(Selection selection);
        void onRename(int entityId, String newName);
        void onDelete(int entityId);
        void onToggleExpand(int entityId, boolean newState);
        void onAddComponent(int entityId);
        void onComponentSelected(int entityId, ComponentType type);
    }

    public EntityRow(int entityId, String initialName, game.engine.ui.services.EditContext editContext, UIContext uiContext) {
        this.entityId = entityId;
        this.editable = new EditableLabel(new EditableLabelProps("entity-" + entityId, initialName, editContext));
        this.expanded = false;
        this.contextMenu = new ContextMenu("entity-context-menu-" + entityId);
        this.uiContext = uiContext;
    }

    public void setText(String name) { editable.setText(name); }
    public boolean isEditing() { return editable.isEditing(); }
    public void cancelEdit() { editable.cancel(); }
    public void setExpanded(boolean val) { this.expanded = val; }
    public boolean isExpanded() { return this.expanded; }
    public game.engine.ui.components.InlineEditor getEditable() { return this.editable; }

    public void render(String displayName, Selection selection, List<ComponentType> components, Callback cb) {
        ImGui.pushID(entityId);
        boolean isEntitySelected = selection != null && selection.isEntity() && selection.getEntityId() == entityId;

        int baseFlags = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.AllowItemOverlap;
        if (isEntitySelected) baseFlags |= ImGuiTreeNodeFlags.Selected;
        if (components.isEmpty()) baseFlags |= ImGuiTreeNodeFlags.Leaf;

        float fh = ImGui.getFrameHeight();
        float entityRowHeight = fh + 2f;
        boolean nodeExpanded;
        
        // Helper to draw the tree node arrow with transparent header (so ImGui doesn't draw its hover box).
        final java.util.function.BiFunction<String, Integer, Boolean> hiddenNode = (id, flags) -> {
            ImGui.pushStyleColor(ImGuiCol.Header, 0);
            ImGui.pushStyleColor(ImGuiCol.HeaderHovered, 0);
            ImGui.pushStyleColor(ImGuiCol.HeaderActive, 0);
            boolean res = ImGui.treeNodeEx(id, flags);
            ImGui.popStyleColor(3);
            return res;
        };

        

        if (editable.isEditing()) {
            if (expanded) ImGui.setNextItemOpen(true);
            nodeExpanded = hiddenNode.apply("##" + entityId, baseFlags | ImGuiTreeNodeFlags.OpenOnArrow);
            // Draw a hover outline around the arrow when mouse is over it (visual affordance)
            ImVec2 amin = ImGui.getItemRectMin();
            ImVec2 amax = ImGui.getItemRectMax();
            if (ImGui.isItemHovered()) {
                int col = ImGui.getColorU32(ImGuiCol.HeaderHovered);
                ImGui.getWindowDrawList().addRect(amin.x, amin.y, amax.x, amax.y, col, 0f, 0, 1f);
            }

            ImGui.sameLine();
            // Position label directly after arrow (3px spacing)
            ImGui.setCursorPosX(ImGui.getCursorPosX() + 3f);
            editable.render(displayName, entityId, isEntitySelected, new EditableLabel.Callback() {
                @Override public void onCommit(String newText) { if (cb != null) cb.onRename(entityId, newText); }
                @Override public void onCancel() { }
            }, null, fh);
        } else {
            if (expanded) ImGui.setNextItemOpen(true);
            nodeExpanded = hiddenNode.apply("##arrow-" + entityId, baseFlags | ImGuiTreeNodeFlags.OpenOnArrow);
            // Draw a hover outline around the arrow when mouse is over it
            ImVec2 amin = ImGui.getItemRectMin();
            ImVec2 amax = ImGui.getItemRectMax();
            if (ImGui.isItemHovered()) {
                int col = ImGui.getColorU32(ImGuiCol.HeaderHovered);
                ImGui.getWindowDrawList().addRect(amin.x, amin.y, amax.x, amax.y, col, 0f, 0, 1f);
            }

            ImGui.sameLine();
            float currentY = ImGui.getCursorPosY();
            float centerOffset = (entityRowHeight - fh) * 0.5f;
            if (centerOffset > 0) ImGui.setCursorPosY(currentY + centerOffset);

            ImGui.setCursorPosX(ImGui.getCursorPosX() + 3f);
            ImGui.selectable(displayName + "##sel-" + entityId, isEntitySelected, 0, 0, entityRowHeight);
            
            // Draw green dot if this is the main camera
            if (uiContext.getMainCameraEntityId() == entityId) {
                 ImVec2 itemMin = ImGui.getItemRectMin();
                 ImVec2 itemMax = ImGui.getItemRectMax();
                 float cx = itemMax.x - 10.0f;
                 float cy = (itemMin.y + itemMax.y) * 0.5f;
                 float radius = 4.0f;
                 int green = ImGui.getColorU32(0.0f, 1.0f, 0.0f, 1.0f);
                 ImGui.getWindowDrawList().addCircleFilled(cx, cy, radius, green, 12);
                 // Tooltip to explain the dot
                 if (ImGui.isItemHovered()) {
                     ImGui.beginTooltip();
                     ImGui.text("Main Camera");
                     ImGui.endTooltip();
                 }
            }

            if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(0)) editable.tryStart(entityId);
            if (ImGui.isItemClicked() && cb != null) cb.onSelect(new Selection(entityId));
        }

        if (nodeExpanded != this.expanded) {
            this.expanded = nodeExpanded;
            if (cb != null) cb.onToggleExpand(entityId, this.expanded);
        }

        contextMenu.render(uiContext, builder -> {
            // Use a submenu so the Add Component entry expands with the triangle and
            // can show grouped Engine / Custom components (populated by the helper).
            builder.addSubMenu("Add Component", sub -> ContextMenu.populateAddComponentMenu(uiContext, entityId, sub));
            builder.addItem("Rename Entity", () -> cb.onRename(entityId, null));
            builder.addSeparator();
            builder.addItem("Delete Entity", () -> cb.onDelete(entityId));
        });

        if (nodeExpanded) {
            for (ComponentType component : components) {
                boolean isComponentSelected = selection != null && selection.isComponent()
                        && selection.getEntityId() == entityId
                        && selection.getComponentType() == component;

                float childRowHeight = fh - 4f;
                float childY = ImGui.getCursorPosY();
                float childOffset = (childRowHeight - fh) * 0.5f;
                if (childOffset != 0) ImGui.setCursorPosY(childY + childOffset);
                ImGui.setCursorPosX(ImGui.getCursorPosX() + 12f);

                ImGui.selectable(component.name() + "##cmp-" + entityId + "-" + component.name(), isComponentSelected, 0, 0, childRowHeight);
                if (ImGui.isItemClicked()) cb.onComponentSelected(entityId, component);

                // Right-click context menu for component actions (Delete)
                // Use explicit openPopup on right-click for reliability.
                String cmpPopupId = "component-context-menu-" + entityId + "-" + component.name();
                if (ImGui.isItemClicked(1)) ImGui.openPopup(cmpPopupId);
                if (ImGui.beginPopup(cmpPopupId)) {
                    if (ImGui.menuItem("Delete Component")) {
                        uiContext.deferRemoveComponent(entityId, component);
                    }
                    ImGui.endPopup();
                }
            }
            ImGui.treePop();
        }

        ImGui.popID();
    }
}
