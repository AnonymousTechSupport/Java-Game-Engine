package game.engine.ECS.components;

import imgui.ImGui;
import org.joml.Vector2f;

/**
 * Position and scale for entities
 */
public class TransformComponent implements Component {
    public Vector2f position = new Vector2f(0f, 0f);
    public float rotation = 0f;
    public Vector2f scale = new Vector2f(1f, 1f);

    // Transient persistent ImGui buffers
    private transient imgui.type.ImFloat posXBuf, posYBuf, rotationBuf, scaleXBuf, scaleYBuf;

    public TransformComponent() {
    }

    public TransformComponent(float x, float y) {
        this.position.set(x, y);
    }

    @Override
    public void renderInspector() {
        ensureInspectorBuffers();
        if (ImGui.inputFloat("x", posXBuf))
            position.x = posXBuf.get();
        if (ImGui.inputFloat("y", posYBuf))
            position.y = posYBuf.get();
        if (ImGui.inputFloat("rotation", rotationBuf))
            rotation = rotationBuf.get();
        if (ImGui.inputFloat("scaleX", scaleXBuf))
            scale.x = scaleXBuf.get();
        if (ImGui.inputFloat("scaleY", scaleYBuf))
            scale.y = scaleYBuf.get();
    }

    private void ensureInspectorBuffers() {
        if (posXBuf == null)
            posXBuf = new imgui.type.ImFloat(position.x);
        if (posYBuf == null)
            posYBuf = new imgui.type.ImFloat(position.y);
        if (rotationBuf == null)
            rotationBuf = new imgui.type.ImFloat(rotation);
        if (scaleXBuf == null)
            scaleXBuf = new imgui.type.ImFloat(scale.x);
        if (scaleYBuf == null)
            scaleYBuf = new imgui.type.ImFloat(scale.y);
    }
}
