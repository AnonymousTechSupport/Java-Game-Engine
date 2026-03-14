package game.engine.ECS.components;

/**
 * Position and scale for entities.
 */
public class TransformComponent {
    public float x = 0f;
    public float y = 0f;
    public float rotation = 0f;
    public float scaleX = 1f;
    public float scaleY = 1f;

    public TransformComponent() {}

    public TransformComponent(float x, float y) {
        this.x = x; this.y = y;
    }
}
