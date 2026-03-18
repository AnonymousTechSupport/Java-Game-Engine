package game.engine.ui.components;

import game.engine.ui.core.UIContext;
import game.engine.ui.components.props.DropdownProps;
import imgui.ImGui;
import imgui.type.ImInt;

/**
 * Reusable dropdown widget that uses DropdownProps for configuration.
 */
public class UIDropdown implements Widget {
    private final DropdownProps props;
    private final ImInt current;

    public UIDropdown(DropdownProps props) {
        this.props = props;
        this.current = new ImInt(props.selectedIndex);
    }

    @Override
    public void render(UIContext context) {
        ImGui.pushID(props.label);
        try {
            if (ImGui.combo(props.label, current, props.options, props.options.length)) {
                props.selectedIndex = current.get();
                try {
                    if (props.onChange != null) props.onChange.accept(current.get());
                } catch (Exception e) {
                    game.engine.logging.Logger.error(game.engine.logging.Logger.UI, "UIDropdown handler failed: " + e.getMessage(), e);
                }
            }
        } finally {
            ImGui.popID();
        }
    }

    public int getSelectedIndex() { return current.get(); }
}
