package game.engine.ECS.components;

import imgui.ImGui;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Generic renderable that can represent different primitive types (rectangle, ball, texture).
 * Start with RECTANGLE and BALL. Inspector shows a type dropdown and only the fields for the chosen type.
 */
public class RenderComponent implements Component {
    public enum RenderType { RECTANGLE, BALL /*, TEXTURE */ }

    public RenderType type = RenderType.RECTANGLE;

    // Rectangle params
    public Vector2f size = new Vector2f(32, 32);
    public Vector2f position = new Vector2f(0, 0);

    // Ball params
    public float radius = 16f;

    // Common color / ordering
    public Vector4f color = new Vector4f(1f, 1f, 1f, 1f);
    public int zIndex = 0;

    public RenderComponent() {}

    // Persistent ImGui buffers so inspector widgets keep focus/state across frames
    private transient ImInt renderTypeIndexBuffer;
    private transient ImInt widthBuffer;
    private transient ImInt heightBuffer;
    private transient ImFloat radiusBuffer;
    private transient ImFloat rBuffer, gBuffer, bBuffer, aBuffer;

    @Override
    public void renderInspector() {
        ensureInspectorBuffers();
        String[] kinds = new String[] { "Rectangle", "Ball" };
        // ImGui combo: show a simple dropdown for render type
        if (ImGui.combo("Renderable", renderTypeIndexBuffer, kinds, kinds.length)) {
            type = RenderType.values()[renderTypeIndexBuffer.get()];
        }

        if (type == RenderType.RECTANGLE) {
            if (ImGui.inputInt("width", widthBuffer)) {
                size.x = widthBuffer.get();
            }
            if (ImGui.inputInt("height", heightBuffer)) {
                size.y = heightBuffer.get();
            }
        } else if (type == RenderType.BALL) {
            if (ImGui.inputFloat("radius", radiusBuffer)) {
                radius = radiusBuffer.get();
            }
        }
        if (ImGui.inputFloat("Red", rBuffer)) color.x = rBuffer.get();
        if (ImGui.inputFloat("Green", gBuffer)) color.y = gBuffer.get();
        if (ImGui.inputFloat("Blue", bBuffer)) color.z = bBuffer.get();
        if (ImGui.inputFloat("Alpha", aBuffer)) color.w = aBuffer.get();
    }

    private void ensureInspectorBuffers() {
        if (renderTypeIndexBuffer == null) renderTypeIndexBuffer = new ImInt(type.ordinal());
        if (widthBuffer == null) widthBuffer = new ImInt((int)size.x);
        if (heightBuffer == null) heightBuffer = new ImInt((int)size.y);
        if (radiusBuffer == null) radiusBuffer = new ImFloat(radius);
        if (rBuffer == null) rBuffer = new ImFloat(color.x);
        if (gBuffer == null) gBuffer = new ImFloat(color.y);
        if (bBuffer == null) bBuffer = new ImFloat(color.z);
        if (aBuffer == null) aBuffer = new ImFloat(color.w);
    }
}
