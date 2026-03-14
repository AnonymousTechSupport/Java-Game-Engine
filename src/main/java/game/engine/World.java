package game.engine;

import game.engine.ECS.ComponentManager;
import game.engine.ECS.EntityManager;
import game.engine.renderer.Renderer;
import game.engine.ECS.components.BallComponent;
import game.engine.ECS.components.RectangleComponent;
import game.engine.ECS.components.TransformComponent;
import game.engine.ECS.systems.RenderingSystem;

import game.engine.logging.Logger;

/**
 * World holds ECS component managers and systems. Keeps GameLoop minimal by
 * owning entity/component setup and exposing simple lifecycle methods.
 */
public class World {
    private final EntityManager ecs;

    public final ComponentManager<TransformComponent> transforms;
    public final ComponentManager<RectangleComponent> rectangles;
    public final ComponentManager<BallComponent> balls;

    private final RenderingSystem renderingSystem;

    public World(EntityManager ecs) {
        this.ecs = ecs;
        this.transforms = new ComponentManager<>();
        this.rectangles = new ComponentManager<>();
        this.balls = new ComponentManager<>();

        this.renderingSystem = new RenderingSystem(transforms, rectangles, balls);
        Logger.info(Logger.WORLD, "World created and systems initialized.");
    }

    /** Create a couple demo entities to exercise rendering. */
    public void initDemoEntities(int screenWidth, int screenHeight) {
        int e1 = ecs.createEntity();
        transforms.add(e1, new TransformComponent(screenWidth/2f - 50, screenHeight/2f - 25));
        rectangles.add(e1, new RectangleComponent(100, 50));
        Logger.debug(Logger.WORLD, () -> "Created demo rectangle entity with ID " + e1);

        int e2 = ecs.createEntity();
        transforms.add(e2, new TransformComponent(screenWidth/2f + 30, screenHeight/2f + 10));
        balls.add(e2, new BallComponent(30f));
        Logger.debug(Logger.WORLD, () -> "Created demo ball entity with ID " + e2);
    }

    /** Render all systems that produce draws. */
    public void render(Renderer renderer) {
        Logger.trace(Logger.WORLD, "World render step initiated.");
        renderingSystem.renderAll(renderer);
    }
}
