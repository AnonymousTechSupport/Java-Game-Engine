package game.engine.ECS.components;

import imgui.ImGui;
import imgui.type.ImFloat;

/**
 * Ball renderable data.
 */
public class BallComponent implements Component {
    public float radius = 16f;
    public float r = 1f, g = 1f, b = 1f, a = 1f;
    public int z = 0;

    public BallComponent() {}

    public BallComponent(float radius) { this.radius = radius; }

    @Override
    public void renderInspector() {
        ImFloat radiusValue = new ImFloat(radius);
        if (ImGui.inputFloat("radius", radiusValue)) {
            radius = radiusValue.get();
        }
    }
}

