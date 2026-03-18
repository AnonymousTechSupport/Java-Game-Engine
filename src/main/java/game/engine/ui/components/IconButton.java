package game.engine.ui.components;

import game.engine.ui.core.UIContext;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;

import game.engine.ui.components.props.IconButtonProps;

public class IconButton implements Widget {

    private final IconButtonProps props;

    public IconButton(IconButtonProps props) {
        this.props = props;
    }

    @Override
    public void render(UIContext context) {
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
        ImGui.pushStyleColor(ImGuiCol.Button, 0);
        if (ImGui.button(props.icon)) {
            if (props.onClick != null) {
                try {
                    props.onClick.accept(context);
                } catch (Exception e) {
                    game.engine.logging.Logger.error(game.engine.logging.Logger.UI, "IconButton handler failed: " + e.getMessage(), e);
                }
            }
        }
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }
}
