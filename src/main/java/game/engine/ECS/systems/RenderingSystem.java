package game.engine.ECS.systems;

import dev.dominion.ecs.api.Dominion;
import game.engine.ECS.components.TransformComponent;
import game.engine.ECS.components.RenderComponent;
import game.engine.renderer.Renderer;
import game.engine.render.Framebuffer;
import game.engine.render.Camera;
import game.engine.World;
import game.engine.renderer.RenderContext;
import static org.lwjgl.opengl.GL11.*;

/**
 * Rendering system: iterates renderable components using Dominion ECS
 * and issues draw calls on the provided Renderer. Preserves submission order.
 */
public class RenderingSystem {
    private final Dominion dominion;

    public RenderingSystem(Dominion dominion) {
        this.dominion = dominion;
    }

    public void renderToFramebuffer(Framebuffer framebuffer, Camera camera, World world, Renderer renderer, float deltaTime) {
        framebuffer.bind();
        glViewport(0, 0, framebuffer.getWidth(), framebuffer.getHeight());

        // Prepare context with FBO size and delta time
        RenderContext context = new RenderContext(framebuffer.getWidth(), framebuffer.getHeight(), deltaTime);

        renderer.beginFrame(context, camera);
        world.render(renderer); // This calls renderAll, which is fine.
        renderer.endFrame();

        framebuffer.unbind();
    }

    /** Collects and immediately issues draw calls for all renderables. */
    public void renderAll(Renderer renderer) {
        // Generic render component (preferred)
        dominion.findEntitiesWith(TransformComponent.class, RenderComponent.class).stream()
            .forEach(result -> {
                TransformComponent tc = result.comp1();
                RenderComponent rc = result.comp2();

                renderer.pushMatrix();
                // Use JOML vectors for position/scale when calling renderer
                renderer.translate(tc.position);
                renderer.rotate(tc.rotation);
                renderer.scale(tc.scale);

                switch (rc.type) {
                    case RECTANGLE:
                        renderer.drawRect(rc.position, rc.size, rc.color);
                        break;
                    case BALL:
                        renderer.drawBall(rc.position, rc.radius, rc.color);
                        break;
                    default:
                        break;
                }

                renderer.popMatrix();
            });
    }
}
