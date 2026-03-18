package game.engine.renderer;

/**
 * Holds per-frame rendering context like viewport size and delta time.
 */
public class RenderContext {
    public final int width;
    public final int height;
    public final float deltaTime;

    public RenderContext(int width, int height, float deltaTime) {
        this.width = width; this.height = height; this.deltaTime = deltaTime;
    }
}
