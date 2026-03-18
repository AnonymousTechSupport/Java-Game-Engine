package game.engine.renderer;

import game.engine.render.Camera;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Renderer abstraction / interface used by the engine. Implementations should manage
 * graphics API initialization in their constructors, per-frame rendering, and cleanup.
 */
public interface Renderer {
    /** Prepare the renderer for a new frame. */
    void beginFrame(RenderContext context);
    
    /** Prepare the renderer for a new frame with a camera. */
    default void beginFrame(RenderContext context, Camera camera) {
        beginFrame(context); // Fallback if not implemented
    }

    /** Draw a filled rectangle at the given position with size and color. */
    void drawRect(Vector2f position, Vector2f size, Vector4f color);

    /** Draw a filled circle at the given position with a color. */
    void drawBall(Vector2f position, float radius, Vector4f color);

    void pushMatrix();
    void popMatrix();
    void translate(Vector2f offset);
    void rotate(float angle);
    void scale(Vector2f scale);

    /** Finish the frame and flush any pending GPU work. */
    void endFrame();

    /** Cleanup GPU resources and state. */
    void cleanup();
}
