package game.engine.ui.components;

import game.engine.ui.core.UIContext;
import imgui.ImGui;

import java.util.function.Consumer;

public class UIButton implements Widget {

    private final String label;
    private final Consumer<UIContext> onClick;

    public UIButton(String label, Consumer<UIContext> onClick) {
        this.label = label;
        this.onClick = onClick;
    }

    @Override
    public void render(UIContext context) {
        if (ImGui.button(label)) {
            if (onClick != null) {
                try {
                    onClick.accept(context);
                } catch (Exception e) {
                    // Safely swallow handler exceptions to avoid breaking the UI loop
                    game.engine.logging.Logger.error(game.engine.logging.Logger.UI, "Error in UIButton handler: " + e.getMessage(), e);
                }
            }
        }
    }
}
