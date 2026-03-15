package game.engine.ui.components;

import game.engine.ui.core.UIContext;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;

import java.util.function.Consumer;

public class IconButton implements Widget {

    private final String icon;
    private final Consumer<UIContext> onClick;

    public IconButton(String icon, Consumer<UIContext> onClick) {
        this.icon = icon;
        this.onClick = onClick;
    }

    @Override
    public void render(UIContext context) {
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
        ImGui.pushStyleColor(ImGuiCol.Button, 0);
        if (ImGui.button(icon)) {
            onClick.accept(context);
        }
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }
}
