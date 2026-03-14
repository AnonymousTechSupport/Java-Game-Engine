package game.engine.renderer;

/**
 * Renderer abstraction / interface used by the engine. Implementations should manage
 * graphics API initialization in their constructors, per-frame rendering, and cleanup.
 */
public interface Renderer {
    /** Prepare the renderer for a new frame. */
    void beginFrame(RenderContext ctx);

    /** Draw a filled rectangle at the given position. */
    void drawRect(float x, float y, int width, int height, float r, float g, float b);

    /** Draw a filled ball (circle) at the given position. */
    void drawBall(float x, float y, float radius, float r, float g, float b);

    /** Finish the frame and flush any pending GPU work. */
    void endFrame();

    /** Cleanup GPU resources and state. */
    void cleanup();
}
