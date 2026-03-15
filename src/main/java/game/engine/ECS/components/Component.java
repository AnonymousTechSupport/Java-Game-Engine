package game.engine.ECS.components;

import imgui.ImGui;

public interface Component {
    default void renderInspector() {
        ImGui.text("No inspector properties for this component.");
    }
}
