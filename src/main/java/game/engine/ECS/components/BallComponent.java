package game.engine.ECS.components;

/**
 * Ball renderable data.
 */
public class BallComponent {
    public float radius = 16f;
    public float r = 1f, g = 1f, b = 1f, a = 1f;
    public int z = 0;

    public BallComponent() {}

    public BallComponent(float radius) { this.radius = radius; }
}
