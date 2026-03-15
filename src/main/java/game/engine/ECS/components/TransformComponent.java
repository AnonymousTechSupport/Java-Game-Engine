package game.engine.ECS.components;

import imgui.ImGui;
import imgui.type.ImFloat;

/**
 * Position and scale for entities.
 */
public class TransformComponent implements Component {
    public float x = 0f;
    public float y = 0f;
    public float rotation = 0f;
    public float scaleX = 1f;
    public float scaleY = 1f;

    public TransformComponent() {}

    public TransformComponent(float x, float y) {
        this.x = x; this.y = y;
    }

    @Override
    public void renderInspector() {
        ImFloat xValue = new ImFloat(x);
        if (ImGui.inputFloat("x", xValue)) {
            x = xValue.get();
        }

        ImFloat yValue = new ImFloat(y);
        if (ImGui.inputFloat("y", yValue)) {
            y = yValue.get();
        }

        ImFloat rotationValue = new ImFloat(rotation);
        if (ImGui.inputFloat("rotation", rotationValue)) {
            rotation = rotationValue.get();
        }

        ImFloat scaleXValue = new ImFloat(scaleX);
        if (ImGui.inputFloat("scaleX", scaleXValue)) {
            scaleX = scaleXValue.get();
        }

        ImFloat scaleYValue = new ImFloat(scaleY);
        if (ImGui.inputFloat("scaleY", scaleYValue)) {
            scaleY = scaleYValue.get();
        }
    }
}

