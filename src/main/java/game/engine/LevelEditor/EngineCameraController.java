package game.engine.LevelEditor;

import game.engine.render.Camera;
import imgui.ImGui;
import imgui.ImVec2;

/**
 * Controls the editor camera inside the viewport. Handles input forwarding for
 * pan/zoom.
 */
public class EngineCameraController {
    private final Camera camera;

    // Smooth panning: camera moves toward a target position for smoothness (no
    // gliding)
    private final org.joml.Vector2f targetPosition = new org.joml.Vector2f();

    // Sensitivity for converting mouse pixel drag into world units
    private float panSensitivity = 1.0f;

    // Smoothness factor (per-second). Higher -> faster/snappier. Typical: 12.0
    private float smoothness = 12.0f;

    public EngineCameraController() {
        this.camera = new Camera(0, 0);
        this.targetPosition.set(camera.position.x, camera.position.y);
    }

    /**
     * Configure pan sensitivity (default 1.0). Higher values increase response
     * to drags.
     */
    public void setPanSensitivity(float sensitivity) {
        this.panSensitivity = sensitivity;
    }

    /**
     * Configure smoothness (per-second). Higher -> faster follow (snappier).
     */
    public void setSmoothness(float smoothness) {
        this.smoothness = smoothness;
    }

    public Camera getCamera() {
        return camera;
    }

    /**
     * Updates camera based on ImGui input state. Should only be called when
     * viewport is hovered.
     */
    public void update(float deltaTime) {
        handlePanning();
        applySmoothing(deltaTime);
        handleZooming();
    }

    private void handlePanning() {
        // Handle Pan: Left Mouse Drag (button 0) when Left Alt is held.
        boolean isAltHeld = ImGui.getIO().getKeyAlt();
        if (isAltHeld && ImGui.isMouseDragging(0)) {
            ImVec2 dragDelta = ImGui.getMouseDragDelta(0);

            // Immediate move for responsive dragging
            float movementX = -dragDelta.x / camera.zoom * panSensitivity;
            float movementY = dragDelta.y / camera.zoom * panSensitivity;

            camera.position.x += movementX;
            camera.position.y += movementY;

            // Keep the smooth target in sync with immediate position
            targetPosition.set(camera.position.x, camera.position.y);

            // Reset drag delta so next frame only accounts for new movement
            ImGui.resetMouseDragDelta(0);
        }
    }

    private void applySmoothing(float deltaTime) {
        // Smoothly move camera toward target position using exponential
        // smoothing
        if (smoothness > 0f) {
            float smoothingFactor = 1.0f - (float) Math.exp(-smoothness * deltaTime);
            camera.position.x += (targetPosition.x - camera.position.x) * smoothingFactor;
            camera.position.y += (targetPosition.y - camera.position.y) * smoothingFactor;
        } else {
            camera.position.set(targetPosition);
        }
    }

    private void handleZooming() {
        // Handle Zoom: Mouse Wheel
        float scrollAmount = ImGui.getIO().getMouseWheel();
        if (scrollAmount != 0) {
            float zoomSpeed = 0.1f;
            camera.zoom += scrollAmount * zoomSpeed * camera.zoom;

            // Clamp zoom levels
            if (camera.zoom < 0.1f)
                camera.zoom = 0.1f;
            if (camera.zoom > 10.0f)
                camera.zoom = 10.0f;
        }
    }
}
