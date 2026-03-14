package game.engine.ECS.components;

/**
 * Rectangle renderable data.
 */
public class RectangleComponent {
    public int width = 32;
    public int height = 32;
    public float r = 1f, g = 1f, b = 1f, a = 1f;
    public int z = 0;

    public RectangleComponent() {}

    public RectangleComponent(int w, int h) { this.width = w; this.height = h; }
}
