package game.engine.ECS.components;

import imgui.ImGui;
import imgui.type.ImInt;

/**
 * Rectangle renderable data.
 */
public class RectangleComponent implements Component {
    public int width = 32;
    public int height = 32;
    public float r = 1f, g = 1f, b = 1f, a = 1f;
    public int z = 0;

    public RectangleComponent() {}

    public RectangleComponent(int w, int h) { this.width = w; this.height = h; }

    @Override
    public void renderInspector() {
        ImInt widthValue = new ImInt(width);
        if (ImGui.inputInt("width", widthValue)) {
            width = widthValue.get();
        }

        ImInt heightValue = new ImInt(height);
        if (ImGui.inputInt("height", heightValue)) {
            height = heightValue.get();
        }
    }
}

