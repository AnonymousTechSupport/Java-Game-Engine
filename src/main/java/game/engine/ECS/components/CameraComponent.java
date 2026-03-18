package game.engine.ECS.components;

import game.engine.render.Camera;
import game.engine.ui.core.UIContext;
import imgui.ImGui;
import imgui.type.ImFloat;
import org.joml.Vector2f;
import java.io.Serializable;

/**
 * Component that holds a Camera for rendering.
 */
public class CameraComponent implements Component, Serializable {
    public Vector2f position = new Vector2f();
    public float rotation = 0.0f;
    public float zoom = 1.0f;

    public CameraComponent(float x, float y, float zoom) {
        this.position.set(x, y);
        this.zoom = zoom;
    }

    public CameraComponent() {
        this(0, 0, 1.0f);
    }

    // Transient ImFloat buffers used by the inspector to retain widget state across frames
    // Use descriptive names so intent is clear when debugging inspector behavior.
    private transient ImFloat posXBuffer, posYBuffer, rotationBuffer, zoomBuffer;
    
    public Camera toCamera() {
        Camera c = new Camera(position.x, position.y);
        c.rotation = rotation;
        c.zoom = zoom;
        return c;
    }

    public void fromCamera(Camera c) {
        this.position.set(c.position.x, c.position.y);
        this.rotation = c.rotation;
        this.zoom = c.zoom;
    }

    public void renderInspector(UIContext ctx, int entityId) {
        ensureInspectorBuffers();
        // Position
        if (ImGui.inputFloat("X", posXBuffer)) {
            position.x = posXBuffer.get();
        }
        if (ImGui.inputFloat("Y", posYBuffer)) {
            position.y = posYBuffer.get();
        }

        // Rotation and Zoom
        if (ImGui.inputFloat("Rotation", rotationBuffer, 1.0f, 10.0f, "%.2f")) {
            rotation = rotationBuffer.get();
        }
        if (ImGui.inputFloat("Zoom", zoomBuffer, 0.1f, 1.0f, "%.2f")) {
            zoom = zoomBuffer.get();
            if (zoom < 0.1f) zoom = 0.1f;
        }
        
        ImGui.separator();
        
        int mainCamId = ctx.getMainCameraEntityId();
        if (mainCamId == entityId) {
            ImGui.textColored(0, 1, 0, 1, "Main Camera (Default)");
        } else {
            if (ImGui.button("Set As Default Camera")) {
                ctx.setMainCameraEntityId(entityId);
            }
        }
    }

    // Ensure transient buffers exist and are seeded from component fields.
    private void ensureInspectorBuffers() {
        if (posXBuffer == null) posXBuffer = new ImFloat(position.x);
        if (posYBuffer == null) posYBuffer = new ImFloat(position.y);
        if (rotationBuffer == null) rotationBuffer = new ImFloat(rotation);
        if (zoomBuffer == null) zoomBuffer = new ImFloat(zoom);
    }
    
    @Override
    public void renderInspector() {
        ImGui.text("Requires UI Context for full inspection.");
    }
}
