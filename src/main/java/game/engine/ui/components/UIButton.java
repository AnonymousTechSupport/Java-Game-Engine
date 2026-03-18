package game.engine.ui.components;

import game.engine.ui.core.UIContext;
import imgui.ImGui;

import game.engine.ui.components.props.ButtonProps;

public class UIButton implements Widget {

    private final ButtonProps props;

    public UIButton(ButtonProps props) {
        this.props = props;
    }

    @Override
    public void render(UIContext context) {
        if (ImGui.button(props.label)) {
            if (props.onClick != null) {
                try {
                    props.onClick.accept(context);
                } catch (Exception e) {
                    // Safely swallow handler exceptions to avoid breaking the UI loop
                    game.engine.logging.Logger.error(game.engine.logging.Logger.UI, "Error in UIButton handler: " + e.getMessage(), e);
                }
            }
        }
    }
}
