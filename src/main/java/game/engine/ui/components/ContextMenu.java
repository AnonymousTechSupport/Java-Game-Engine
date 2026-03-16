package game.engine.ui.components;

import game.engine.ui.core.UIContext;
import imgui.ImGui;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;

/**
 * Small helper for rendering right-click context menus and generic
 * add-component popups. The class provides a fluent MenuBuilder for
 * building nested menu items and groups that execute actions or defer
 * UI operations via the provided UIContext.
 */
public class ContextMenu {

    // Identifier used when the popup needs a stable id (not used by the
    // beginPopupContextItem flow but retained for explicit openPopup/beginPopup use)
    private final String popupId;

    /**
     * Create a context menu helper instance.
     *
     * @param popupId stable id for popups built from this instance
     */
    public ContextMenu(String popupId) {
        this.popupId = popupId;
    }

    /**
     * Render a right-click context menu attached to the previously submitted
     * ImGui item. The provided builder consumer is invoked to populate the
     * menu contents. This method calls ImGui.beginPopupContextItem(), builds
     * the menu and ends the popup if opened.
     *
     * @param uiContext   context used by menu items to defer actions safely
     * @param menuBuilder lambda that populates the MenuBuilder
     */
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
     * Build the same menu structure into an explicitly created popup. This is
     * useful for non-context popups (for example the "Add Component" popup)
     * where the popup is opened with ImGui.openPopup()/ImGui.beginPopup.
     *
     * @param uiContext   context used by menu items to defer actions safely
     * @param menuBuilder lambda that populates the MenuBuilder
     */
    public static void buildPopup(UIContext uiContext, Consumer<MenuBuilder> menuBuilder) {
        MenuBuilder builder = new MenuBuilder();
        menuBuilder.accept(builder);
        builder.build(uiContext);
    }
    public static class MenuBuilder {
        private final List<MenuItem> items = new ArrayList<>();
        private Widget footer;

        /**
         * Add a single selectable menu item that executes the given action
         * when activated.
         */
        public MenuBuilder addItem(String label, Runnable action) {
            items.add(new MenuItem(label, action));
            return this;
        }

        /**
         * Add a nested submenu. The provided consumer receives a new MenuBuilder
         * instance which should be populated with submenu entries.
         */
        public MenuBuilder addSubMenu(String label, java.util.function.Consumer<MenuBuilder> submenuBuilder) {
            MenuBuilder sub = new MenuBuilder();
            submenuBuilder.accept(sub);
            items.add(new MenuItem(label, sub));
            return this;
        }

        /**
         * Insert a visual separator between menu groups.
         */
        public MenuBuilder addSeparator() {
            items.add(new MenuItem(null, null, true));
            return this;
        }

        /**
         * Add a titled group header followed by the group's items. The group
         * consumer receives a MenuBuilder for adding items that will be
         * appended after the header.
         */
        public MenuBuilder addGroup(String title, Consumer<MenuBuilder> group) {
            items.add(new MenuItem(title, null, false, true, null));
            MenuBuilder groupBuilder = new MenuBuilder();
            group.accept(groupBuilder);
            items.addAll(groupBuilder.items);
            return this;
        }

        /**
         * Set an optional footer widget rendered after the menu items. The
         * footer can be used for inline controls like an "Add Custom" button.
         */
        public void setFooter(Widget footer) {
            this.footer = footer;
        }

        /**
         * Render the built menu into the current ImGui popup/menu scope. The
         * uiContext is passed to footer widgets so they can safely defer
         * actions through the UI thread.
         */
        void build(UIContext uiContext) {
            for (MenuItem item : items) {
                if (item.isSeparator) {
                    ImGui.separator();
                } else if (item.isGroupHeader) {
                    ImGui.text(item.label);
                    ImGui.separator();
                } else if (item.submenu != null) {
                    if (ImGui.beginMenu(item.label)) {
                        item.submenu.build(uiContext);
                        ImGui.endMenu();
                    }
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
        final MenuBuilder submenu;

        MenuItem(String label, Runnable action) {
            this(label, action, false, false, null);
        }

        MenuItem(String label, Runnable action, boolean isSeparator) {
            this(label, action, isSeparator, false, null);
        }
        MenuItem(String label, MenuBuilder submenu) {
            this(label, null, false, false, submenu);
        }

        MenuItem(String label, Runnable action, boolean isSeparator, boolean isGroupHeader, MenuBuilder submenu) {
            this.label = label;
            this.action = action;
            this.isSeparator = isSeparator;
            this.isGroupHeader = isGroupHeader;
            this.submenu = submenu;
        }
    }

    /**
     * Populate the standard "Add Component" menu entries. This helper is
     * used by both the Inspector and the per-entity context menu to produce
     * grouped Engine / Custom component lists and wire them to deferred
     * add-component actions on the supplied UIContext.
     *
     * @param uiContext context used to defer add-component operations
     * @param entityId  target entity id to which components will be added
     * @param builder   menu builder to populate
     */
    public static void populateAddComponentMenu(UIContext uiContext, int entityId, MenuBuilder builder) {
        builder.addSubMenu("Engine Components", group -> {
            for (game.engine.ECS.components.ComponentType componentType : uiContext.getAvailableComponentTypes()) {
                if (componentType.getCategory() == game.engine.ECS.components.ComponentType.Category.ENGINE) {
                    group.addItem(componentType.name(), () -> uiContext.deferAddComponent(entityId, componentType));
                }
            }
        });

        builder.addSeparator();

        builder.addSubMenu("Custom Components", group -> {
            for (game.engine.ECS.components.ComponentType componentType : uiContext.getAvailableComponentTypes()) {
                if (componentType.getCategory() == game.engine.ECS.components.ComponentType.Category.CUSTOM) {
                    group.addItem(componentType.name(), () -> uiContext.deferAddComponent(entityId, componentType));
                }
            }
        });
    }
}
