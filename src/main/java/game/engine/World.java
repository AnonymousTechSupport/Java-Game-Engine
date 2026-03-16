package game.engine;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;
import game.engine.ECS.components.MetaDataComponent;
import game.engine.renderer.Renderer;
import game.engine.ECS.systems.RenderingSystem;
import game.engine.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * World holds ECS component managers and systems. Keeps GameLoop minimal by
 * owning entity/component setup and exposing simple lifecycle methods.
 */
public class World {
    private final Dominion dominion;
    private final ComponentRegistry componentRegistry;
    private final RenderingSystem renderingSystem;

    // Map to bridge legacy integer IDs to Dominion Entities
    private final Map<Integer, dev.dominion.ecs.api.Entity> entityMap = new HashMap<>();
    private int nextEntityId = 1;

    public World() {
        this.dominion = Dominion.create();
        this.componentRegistry = new ComponentRegistry();

        componentRegistry.register(ComponentType.TRANSFORM, () -> new game.engine.ECS.components.TransformComponent(0, 0));
        componentRegistry.register(ComponentType.RECTANGLE, () -> new game.engine.ECS.components.RectangleComponent(100, 100));
        componentRegistry.register(ComponentType.BALL, () -> new game.engine.ECS.components.BallComponent(50));
        componentRegistry.register(ComponentType.METADATA, () -> new MetaDataComponent("Entity"));

        this.renderingSystem = new RenderingSystem(dominion);

        Logger.info(Logger.WORLD, "World created and systems initialised (Dominion ECS).");
    }

    /** Create an entity and return its ID. */
    public int createEntity(String name) {
        dev.dominion.ecs.api.Entity entity = dominion.createEntity();

        // Allocate a stable integer id used by the rest of the codebase
        int id = nextEntityId++;

        // Add MetaDataComponent for name (use the chosen id for presentation)
        MetaDataComponent meta = new MetaDataComponent(name != null ? name : "Entity " + id);
        entity.add(meta);

        // Store mapping from our integer id to the Dominion entity
        entityMap.put(id, entity);
        
        Logger.trace(Logger.WORLD, () -> "Created entity " + id + " (" + meta.name + ")");
        return id;
    }

    /** Create a couple demo entities to exercise rendering. */
    public void initDemoEntities(int screenWidth, int screenHeight) {
        int e1 = createEntity("Demo Rectangle");
        addComponent(e1, ComponentType.TRANSFORM);
        addComponent(e1, ComponentType.RECTANGLE);
        
        int e2 = createEntity("Demo Ball");
        addComponent(e2, ComponentType.TRANSFORM);
        addComponent(e2, ComponentType.BALL);
    }

    /** Render all systems that produce draws. */
    public void render(Renderer renderer) {
        Logger.trace(Logger.WORLD, "World render step initiated.");
        renderingSystem.renderAll(renderer);
    }

    public void update(float dt) {
        // TODO: Update game logic, e.g., physics, AI
    }

    public void addComponent(int entityId, ComponentType componentType) {
        dev.dominion.ecs.api.Entity entity = entityMap.get(entityId);
        if (entity == null) return;

        // Prevent adding duplicate component types — Dominion ECS throws when
        // attempting to add the same component/class twice.
        if (entity.has(componentType.getComponentClass())) {
            Logger.warn(Logger.WORLD, "Entity " + entityId + " already has component " + componentType.name() + "; skipping add.");
            return;
        }

        Component component = componentRegistry.createComponent(componentType);
        if (component != null) {
            entity.add(component);
        }
    }

    public void removeComponent(int entityId, ComponentType componentType) {
        dev.dominion.ecs.api.Entity entity = entityMap.get(entityId);
        if (entity == null) return;

        // Remove by class type
        if (entity.has(componentType.getComponentClass())) {
            entity.removeType(componentType.getComponentClass());
        }
    }

    public Map<ComponentType, Component> getComponents(int entityId) {
        Map<ComponentType, Component> components = new HashMap<>();
        dev.dominion.ecs.api.Entity entity = entityMap.get(entityId);
        if (entity == null) return components;

        for (ComponentType type : ComponentType.values()) {
            if (type == ComponentType.METADATA) continue; // internal use usually
            
            if (entity.has(type.getComponentClass())) {
                // Dominion get returns the object cast to the class
                Component c = (Component) entity.get(type.getComponentClass());
                if (c != null) {
                    components.put(type, c);
                }
            }
        }
        return components;
    }

    public Component getComponent(int entityId, ComponentType componentType) {
        dev.dominion.ecs.api.Entity entity = entityMap.get(entityId);
        if (entity == null) return null;

        if (entity.has(componentType.getComponentClass())) {
            return (Component) entity.get(componentType.getComponentClass());
        }
        return null;
    }

    public ComponentType[] getAvailableComponentTypes() {
        return componentRegistry.getAvailableComponentTypes();
    }

    /**
     * Remove all components associated with the given entity id.
     * Centralizes component cleanup so callers can fully destroy an entity.
     */
    public void removeAllComponents(int entityId) {
      Entity entity = entityMap.get(entityId);
      if (entity != null) {
        dominion.deleteEntity(entity);
        entityMap.remove(entityId);
      }
        Logger.debug(Logger.WORLD, () -> "Completed cleanup for entity " + entityId);
    }
    
    public Dominion getDominion() {
        return dominion;
    }
    
    // Helper to get name from MetaData
    public String getName(int entityId) {
        Entity entity = entityMap.get(entityId);
        if (entity == null) return null;
        if (entity.has(MetaDataComponent.class)) {
            return entity.get(MetaDataComponent.class).name;
        }
        return "Entity " + entityId;
    }

    public void setName(int entityId, String name) {
        Entity entity = entityMap.get(entityId);
        if (entity == null) return;
        if (entity.has(MetaDataComponent.class)) {
            entity.get(MetaDataComponent.class).name = name;
        } else {
            entity.add(new MetaDataComponent(name));
        }
    }
    
    /** Return underlying Dominion entity map */
    public Map<Integer, dev.dominion.ecs.api.Entity> getEntityMap() {
        return entityMap;
    }
}
