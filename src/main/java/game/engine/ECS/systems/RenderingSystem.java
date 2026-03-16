package game.engine.ECS.systems;

import dev.dominion.ecs.api.Dominion;
import game.engine.ECS.components.TransformComponent;
import game.engine.ECS.components.RectangleComponent;
import game.engine.ECS.components.BallComponent;
import game.engine.renderer.Renderer;

/**
 * Rendering system: iterates renderable components using Dominion ECS
 * and issues draw calls on the provided Renderer. Preserves submission order.
 */
public class RenderingSystem {
    private final Dominion dominion;

    public RenderingSystem(Dominion dominion) {
        this.dominion = dominion;
    }

    /** Collects and immediately issues draw calls for all renderables. */
    public void renderAll(Renderer renderer) {
        // Rectangles
        dominion.findEntitiesWith(TransformComponent.class, RectangleComponent.class).stream()
            .forEach(result -> {
                TransformComponent tc = result.comp1();
                RectangleComponent rc = result.comp2();

                renderer.pushMatrix();
                renderer.translate(tc.x, tc.y);
                renderer.rotate(tc.rotation);
                renderer.scale(tc.scaleX, tc.scaleY);
                renderer.drawRect(0, 0, rc.width, rc.height, rc.r, rc.g, rc.b);
                renderer.popMatrix();
            });

        // Balls
        dominion.findEntitiesWith(TransformComponent.class, BallComponent.class).stream()
            .forEach(result -> {
                TransformComponent tc = result.comp1();
                BallComponent bc = result.comp2();

                renderer.pushMatrix();
                renderer.translate(tc.x, tc.y);
                renderer.rotate(tc.rotation);
                renderer.scale(tc.scaleX, tc.scaleY);
                renderer.drawBall(0, 0, bc.radius, bc.r, bc.g, bc.b);
                renderer.popMatrix();
            });
    }
}
