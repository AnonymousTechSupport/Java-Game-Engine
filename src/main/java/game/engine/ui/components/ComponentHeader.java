package game.engine.ui.components;

import game.engine.ui.core.UIContext;
import java.util.function.Consumer;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;


public class ComponentHeader implements Widget {

    private final String title;
    private final boolean initiallyOpen;
    private final Widget content;
    private final InlineActionBar actionBar;
    private final Consumer<ContextMenu.MenuBuilder> contextMenuBuilder;

    public ComponentHeader(String title, boolean initiallyOpen, Widget content, InlineActionBar actionBar) {
        this(title, initiallyOpen, content, actionBar, null);
    }

    public ComponentHeader(String title, boolean initiallyOpen, Widget content, InlineActionBar actionBar, Consumer<ContextMenu.MenuBuilder> contextMenuBuilder) {
        this.title = title;
        this.initiallyOpen = initiallyOpen;
        this.content = content;
        this.actionBar = actionBar;
        this.contextMenuBuilder = contextMenuBuilder;
    }

    @Override
    public void render(UIContext context) {
        int flags = 0;
        if (initiallyOpen) {
            flags |= ImGuiTreeNodeFlags.DefaultOpen;
            ImGui.setNextItemOpen(true);
        }

        // Render the tree node with modified FramePadding to control height.
        // We push FramePadding.y to increase the height.
        float defaultFramePaddingY = ImGui.getStyle().getFramePaddingY();
        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.FramePadding, ImGui.getStyle().getFramePaddingX(), defaultFramePaddingY + 3f);
        
        // Add spaces to title for left padding/indentation from the arrow
        boolean opened = ImGui.treeNodeEx("   " + title, flags);

        // Attach a right-click context menu to the header label if provided.
        if (contextMenuBuilder != null) {
            String popupId = title + "-header-popup";
            if (ImGui.isItemClicked(1)) ImGui.openPopup(popupId);
            if (ImGui.beginPopup(popupId)) {
                ContextMenu.buildPopup(context, contextMenuBuilder);
                ImGui.endPopup();
            }
        }
        
        ImGui.popStyleVar();

        if (actionBar != null) {
            actionBar.render(context);
        }

        if (opened) {
            if (content != null) {
                content.render(context);
            }
            ImGui.treePop();
        }
    }
}
