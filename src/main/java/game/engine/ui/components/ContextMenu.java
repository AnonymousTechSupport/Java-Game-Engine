package game.engine.ui.components;

import game.engine.ui.core.UIContext;
import imgui.ImGui;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;

public class ContextMenu {

    private final String popupId;

    public ContextMenu(String popupId) {
        this.popupId = popupId;
    }

    public void render(UIContext uiContext, Consumer<MenuBuilder> menuBuilder) {
        // Open context popup on right-click of the last item. beginPopupContextItem
        // handles the input modality; use the overload without an explicit id so
        // the popup is associated with the actual last item (more reliable).
        if (ImGui.beginPopupContextItem()) {
            MenuBuilder builder = new MenuBuilder();
            menuBuilder.accept(builder);
            builder.build(uiContext);
            ImGui.endPopup();
        }
    }

    /**
     * Helper to build the same menu structure into an existing popup (e.g. when
     * using ImGui.openPopup / ImGui.beginPopup). Useful for the "Add Component"
     * regular popup which is not a right-click context menu.
     */
    public static void buildPopup(UIContext uiContext, Consumer<MenuBuilder> menuBuilder) {
        MenuBuilder builder = new MenuBuilder();
        menuBuilder.accept(builder);
        builder.build(uiContext);
    }

    public static class MenuBuilder {
        private final List<MenuItem> items = new ArrayList<>();
        private Widget footer;

        public MenuBuilder addItem(String label, Runnable action) {
            items.add(new MenuItem(label, action));
            return this;
        }

        public MenuBuilder addSeparator() {
            items.add(new MenuItem(null, null, true));
            return this;
        }

        public MenuBuilder addGroup(String title, Consumer<MenuBuilder> group) {
            items.add(new MenuItem(title, null, false, true));
            MenuBuilder groupBuilder = new MenuBuilder();
            group.accept(groupBuilder);
            items.addAll(groupBuilder.items);
            return this;
        }

        public void setFooter(Widget footer) {
            this.footer = footer;
        }

        void build(UIContext uiContext) {
            for (MenuItem item : items) {
                if (item.isSeparator) {
                    ImGui.separator();
                } else if (item.isGroupHeader) {
                    ImGui.text(item.label);
                    ImGui.separator();
                } else if (ImGui.menuItem(item.label)) {
                    item.action.run();
                }
            }
            if (footer != null) {
                ImGui.separator();
                footer.render(uiContext); // pass UIContext so footer actions can defer safely
            }
        }
    }

    private static class MenuItem {
        final String label;
        final Runnable action;
        final boolean isSeparator;
        final boolean isGroupHeader;

        MenuItem(String label, Runnable action) {
            this(label, action, false, false);
        }

        MenuItem(String label, Runnable action, boolean isSeparator) {
            this(label, action, isSeparator, false);
        }

        MenuItem(String label, Runnable action, boolean isSeparator, boolean isGroupHeader) {
            this.label = label;
            this.action = action;
            this.isSeparator = isSeparator;
            this.isGroupHeader = isGroupHeader;
        }
    }
}
