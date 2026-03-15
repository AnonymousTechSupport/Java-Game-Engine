package game.engine;

import game.engine.ECS.EntityManager;
import game.engine.ECS.ComponentManager;
import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;
import game.engine.renderer.Renderer;
import game.engine.ECS.components.BallComponent;
import game.engine.ECS.components.RectangleComponent;
import game.engine.ECS.components.TransformComponent;
import game.engine.ECS.systems.RenderingSystem;
import game.engine.logging.Logger;

import java.util.Map;

/**
 * World holds ECS component managers and systems. Keeps GameLoop minimal by
 * owning entity/component setup and exposing simple lifecycle methods.
 */
public class World {
    private final EntityManager ecs;
    private final ComponentRegistry componentRegistry;
    private final ComponentManager<TransformComponent> transforms;
    private final ComponentManager<RectangleComponent> rectangles;
    private final ComponentManager<BallComponent> balls;
    private final RenderingSystem renderingSystem;

    public World(EntityManager ecs) {
        this.ecs = ecs;
        this.componentRegistry = new ComponentRegistry();

        // Create typed managers locally so callers (like RenderingSystem) can use
        // them without unchecked casts. Also register the managers with the
        // ComponentRegistry so the rest of the codebase can access components
        // through the registry.
        this.transforms = new ComponentManager<>();
        this.rectangles = new ComponentManager<>();
        this.balls = new ComponentManager<>();

        componentRegistry.register(ComponentType.TRANSFORM, this.transforms, () -> new TransformComponent(0, 0));
        componentRegistry.register(ComponentType.RECTANGLE, this.rectangles, () -> new RectangleComponent(100, 100));
        componentRegistry.register(ComponentType.BALL, this.balls, () -> new BallComponent(50));

        this.renderingSystem = new RenderingSystem(this.transforms, this.rectangles, this.balls);

        Logger.info(Logger.WORLD, "World created and systems initialised.");
    }

    /** Create a couple demo entities to exercise rendering. */
    public void initDemoEntities(int screenWidth, int screenHeight) {
        int e1 = ecs.createEntity();
        addComponent(e1, ComponentType.TRANSFORM);
        addComponent(e1, ComponentType.RECTANGLE);
        Logger.debug(Logger.WORLD, () -> "Created demo rectangle entity with ID " + e1);

        int e2 = ecs.createEntity();
        addComponent(e2, ComponentType.TRANSFORM);
        addComponent(e2, ComponentType.BALL);
        Logger.debug(Logger.WORLD, () -> "Created demo ball entity with ID " + e2);
    }

    /** Render all systems that produce draws. */
    public void render(Renderer renderer) {
        Logger.trace(Logger.WORLD, "World render step initiated.");
        renderingSystem.renderAll(renderer);
    }

    public void update(float dt) {
        // TODO: Update game logic, e.g., physics, AI
        // no implementatin for now, but this is where it would go
    }

    public void addComponent(int entityId, ComponentType componentType) {
        componentRegistry.addComponent(entityId, componentType);
    }

    public void removeComponent(int entityId, ComponentType componentType) {
        componentRegistry.removeComponent(entityId, componentType);
    }

    public Map<ComponentType, Component> getComponents(int entityId) {
        return componentRegistry.getComponents(entityId);
    }

    public Component getComponent(int entityId, ComponentType componentType) {
        return componentRegistry.getComponent(entityId, componentType);
    }

    public ComponentType[] getAvailableComponentTypes() {
        return componentRegistry.getAvailableComponentTypes();
    }

    /**
     * Remove all components associated with the given entity id.
     * Centralizes component cleanup so callers can fully destroy an entity.
     */
    public void removeAllComponents(int entityId) {
        componentRegistry.removeAllComponents(entityId);
        Logger.debug(Logger.WORLD, () -> "Completed component cleanup for entity " + entityId);
    }
}
