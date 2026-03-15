package game.engine.ECS.systems;

import game.engine.ECS.ComponentManager;
import game.engine.ECS.components.TransformComponent;
import game.engine.ECS.components.RectangleComponent;
import game.engine.ECS.components.BallComponent;
import game.engine.renderer.Renderer;

/**
 * Rendering system: iterates renderable components in insertion order
 * and issues draw calls on the provided Renderer. Preserves submission order.
 */
public class RenderingSystem {
    private final ComponentManager<TransformComponent> transforms;
    private final ComponentManager<RectangleComponent> rectangles;
    private final ComponentManager<BallComponent> balls;

    public RenderingSystem(ComponentManager<TransformComponent> transforms,
                           ComponentManager<RectangleComponent> rectangles,
                           ComponentManager<BallComponent> balls) {
        this.transforms = transforms;
        this.rectangles = rectangles;
        this.balls = balls;
    }

    /** Collects and immediately issues draw calls for all renderables. */
    public void renderAll(Renderer renderer) {
        // Rectangles
        for (Integer e : rectangles.getEntityIds()) {
            RectangleComponent rc = rectangles.get(e);
            TransformComponent tc = transforms.get(e);
            if (tc == null || rc == null) continue;
            renderer.pushMatrix();
            renderer.translate(tc.x, tc.y);
            renderer.rotate(tc.rotation);
            renderer.scale(tc.scaleX, tc.scaleY);
            renderer.drawRect(0, 0, rc.width, rc.height, rc.r, rc.g, rc.b);
            renderer.popMatrix();
        }

        // Balls
        for (Integer e : balls.getEntityIds()) {
            BallComponent bc = balls.get(e);
            TransformComponent tc = transforms.get(e);
            if (tc == null || bc == null) continue;
            renderer.pushMatrix();
            renderer.translate(tc.x, tc.y);
            renderer.rotate(tc.rotation);
            renderer.scale(tc.scaleX, tc.scaleY);
            renderer.drawBall(0, 0, bc.radius, bc.r, bc.g, bc.b);
            renderer.popMatrix();
        }
    }
}
