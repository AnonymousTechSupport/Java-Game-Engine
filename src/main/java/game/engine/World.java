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
    private int mainCameraEntityId = -1;

    public World() {
        this.dominion = Dominion.create();
        this.componentRegistry = new ComponentRegistry();

        componentRegistry.register(ComponentType.TRANSFORM,
                () -> new game.engine.ECS.components.TransformComponent(0, 0));
        componentRegistry.register(ComponentType.RENDER, () -> new game.engine.ECS.components.RenderComponent());
        componentRegistry.register(ComponentType.CAMERA, () -> new game.engine.ECS.components.CameraComponent());
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

    /** Render all systems that produce draws. */
    public void render(Renderer renderer) {
        Logger.trace(Logger.WORLD, "World render step initiated.");
        renderingSystem.renderAll(renderer);
    }

    public RenderingSystem getRenderingSystem() {
        return renderingSystem;
    }

    /**
     * Update all systems that produce game logic changes. Currently empty since
     * we have no dynamic behavior, but this is where physics, AI, etc. would be
     * updated.
     */
    public void update(float deltaTime) {
        // TODO: Update game logic, e.g., physics, AI
    }

    /**
     * Add a component of the specified type to the entity with the given ID.
     * 
     * @param entityId      The integer ID of the entity to modify.
     * @param componentType The type of component to add
     */
    public void addComponent(int entityId, ComponentType componentType) {
        dev.dominion.ecs.api.Entity entity = entityMap.get(entityId);
        if (entity == null)
            return;

        // Prevent adding duplicate component types — Dominion ECS throws when
        // attempting to add the same component/class twice.
        if (entity.has(componentType.getComponentClass())) {
            Logger.warn(Logger.WORLD,
                    "Entity " + entityId + " already has component " + componentType.name() + "; skipping add.");
            return;
        }

        Component component = componentRegistry.createComponent(componentType);
        if (component != null) {
            entity.add(component);
            // If the added component is a Camera and there is no main camera yet,
            // set this entity as the main camera by default.
            if (componentType == ComponentType.CAMERA && this.mainCameraEntityId == -1) {
                Logger.info(Logger.WORLD, "First camera added on entity " + entityId + ", setting as main camera.");
                this.mainCameraEntityId = entityId;
            }
        }
    }

    /**
     * Remove a component of the specified type from the entity with the given ID.
     * 
     * @param entityId      The integer ID of the entity to modify.
     * @param componentType The type of component to remove.
     */
    public void removeComponent(int entityId, ComponentType componentType) {
        dev.dominion.ecs.api.Entity entity = entityMap.get(entityId);
        if (entity == null)
            return;

        // Remove by class type
        if (entity.has(componentType.getComponentClass())) {
            entity.removeType(componentType.getComponentClass());
        }
    }

    /**
     * Retrieve all components associated with the given entity id.
     * 
     * @param entityId The integer ID of the entity to query.
     * @return A map of component types to their instances.
     */
    public Map<ComponentType, Component> getComponents(int entityId) {
        Map<ComponentType, Component> components = new HashMap<>();
        dev.dominion.ecs.api.Entity entity = entityMap.get(entityId);
        if (entity == null)
            return components;

        for (ComponentType type : ComponentType.values()) {
            if (type == ComponentType.METADATA)
                continue; // internal use usually

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

    /**
     * Retrieve a specific component associated with the given entity id.
     * 
     * @param entityId      The integer ID of the entity to query.
     * @param componentType The type of component to retrieve.
     * @return The component instance, or null if not found.
     */
    public Component getComponent(int entityId, ComponentType componentType) {
        dev.dominion.ecs.api.Entity entity = entityMap.get(entityId);
        if (entity == null)
            return null;

        if (entity.has(componentType.getComponentClass())) {
            return (Component) entity.get(componentType.getComponentClass());
        }
        return null;
    }

    /**
     * Retrieve an array of all available component types.
     * 
     * @return An array of component types.
     */
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

    /**
     * Retrieve the name of the entity with the given ID.
     * 
     * @param entityId The integer ID of the entity to query.
     * @return The name of the entity, or a default name if not found.
     */
    public String getName(int entityId) {
        Entity entity = entityMap.get(entityId);
        if (entity == null)
            return null;
        if (entity.has(MetaDataComponent.class)) {
            return entity.get(MetaDataComponent.class).name;
        }
        return "Entity " + entityId;
    }

    /**
     * Set the name of the entity with the given ID.
     * 
     * @param entityId The integer ID of the entity to modify.
     * @param name     The new name for the entity.
     */
    public void setName(int entityId, String name) {
        Entity entity = entityMap.get(entityId);
        if (entity == null)
            return;
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

    public int getMainCameraEntityId() {
        return mainCameraEntityId;
    }

    public void setMainCameraEntityId(int id) {
        this.mainCameraEntityId = id;
    }

    public void clearMainCameraEntityId() {
        this.mainCameraEntityId = -1;
    }
}
