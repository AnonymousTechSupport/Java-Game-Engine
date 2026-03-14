package game.engine.renderer;

/**
 * Abstraction that represents the rendering surface. This decouples renderers from any
 * specific window implementation so renderers can be hot-swapped or used in different hosts.
 */
public interface RenderSurface {
    int getWidth();
    int getHeight();
}
