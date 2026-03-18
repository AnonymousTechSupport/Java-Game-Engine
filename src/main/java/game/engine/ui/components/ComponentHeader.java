package game.engine.ui.components;

import game.engine.ui.core.UIContext;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

public class ComponentHeader implements Widget {

    private final game.engine.ui.components.props.ComponentHeaderProps props;

    public ComponentHeader(game.engine.ui.components.props.ComponentHeaderProps props) {
        this.props = props;
    }

    @Override
    public void render(UIContext context) {
        int flags = 0;
        if (props.initiallyOpen) {
            flags |= ImGuiTreeNodeFlags.DefaultOpen;
            ImGui.setNextItemOpen(true);
        }

        // Render the tree node with modified FramePadding to control height.
        // We push FramePadding.y to increase the height.
        float defaultFramePaddingY = ImGui.getStyle().getFramePaddingY();
        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.FramePadding, ImGui.getStyle().getFramePaddingX(), defaultFramePaddingY + 3f);

        // Add spaces to title for left padding/indentation from the arrow
        boolean opened = ImGui.treeNodeEx("   " + props.title, flags);

        // Attach a right-click context menu to the header label if provided.
        if (props.contextMenuBuilder != null) {
            String popupId = props.title + "-header-popup";
            if (ImGui.isItemClicked(1))
                ImGui.openPopup(popupId);
            if (ImGui.beginPopup(popupId)) {
                ContextMenu.buildPopup(context, props.contextMenuBuilder);
                ImGui.endPopup();
            }
        }

        ImGui.popStyleVar();

        if (props.actionBar != null) {
            props.actionBar.render(context);
        }

        if (opened) {
            if (props.content != null) {
                props.content.render(context);
            }
            ImGui.treePop();
        }
    }
}
