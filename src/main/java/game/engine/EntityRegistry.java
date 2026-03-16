package game.engine;

import game.engine.ECS.Entity;
import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;
import game.engine.ECS.components.MetaDataComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import game.engine.logging.Logger;

/**
 * High-level entity registry used by tools (editor) to create/destroy named entities.
 * Delegates actual storage to World (Dominion ECS).
 */
public class EntityRegistry {
    private final World world;

    public EntityRegistry(World world) {
        this.world = world;
    }

    /** Create a named entity and return an Entity object, or null on failure. */
    public Entity createEntity(String name) {
        int id = world.createEntity(name);
        // Assuming creation always succeeds with Dominion unless OOM
        Logger.info(Logger.WORLD, "Created entity " + id + " name=\"" + name + "\"");
        return new Entity(id, name != null ? name : ("Entity " + id));
    }

    /** Return a snapshot list of entities (id + name). */
    public List<Entity> listEntities() {
        List<Entity> out = new ArrayList<>();
        Map<Integer, dev.dominion.ecs.api.Entity> map = world.getEntityMap();
        
        Logger.trace(Logger.WORLD, () -> "Listing entities for UI refresh; count=" + map.size());
        
        for (Map.Entry<Integer, dev.dominion.ecs.api.Entity> entry : map.entrySet()) {
            int id = entry.getKey();
            dev.dominion.ecs.api.Entity domEntity = entry.getValue();
            String name = "Entity " + id;
            if (domEntity.has(MetaDataComponent.class)) {
                name = domEntity.get(MetaDataComponent.class).name;
            }
            out.add(new Entity(id, name));
        }
        return out;
    }

    /** Get an Entity object, or null if id inactive. */
    public Entity getEntity(int id) {
        dev.dominion.ecs.api.Entity domEntity = world.getEntityMap().get(id);
        if (domEntity == null) return null;

        String name = "Entity " + id;
        if (domEntity.has(MetaDataComponent.class)) {
            name = domEntity.get(MetaDataComponent.class).name;
        }
        return new Entity(id, name);
    }

    /** Rename an entity; returns true if successful. */
    public boolean renameEntity(int id, String newName) {
        if (!world.getEntityMap().containsKey(id)) {
            Logger.warn(Logger.WORLD, "Rename failed: unknown entity id=" + id);
            return false;
        }
        
        dev.dominion.ecs.api.Entity domEntity = world.getEntityMap().get(id);
        if (domEntity.has(MetaDataComponent.class)) {
            domEntity.get(MetaDataComponent.class).name = newName;
        } else {
            domEntity.add(new MetaDataComponent(newName));
        }
        
        Logger.info(Logger.WORLD, "Renamed entity " + id + " -> \"" + newName + "\"");
        return true;
    }

    /** Fully destroy an entity. */
    public boolean destroyEntity(int id) {
        if (!world.getEntityMap().containsKey(id)) {
            Logger.warn(Logger.WORLD, "Attempted to destroy unknown entity id=" + id);
            return false;
        }
        Logger.debug(Logger.WORLD, () -> "Destroying entity " + id);
        world.removeAllComponents(id); // This actually deletes the entity in World implementation
        Logger.info(Logger.WORLD, "Destroyed entity " + id);
        return true;
    }

    public void addComponent(int entityId, ComponentType componentType) {
        world.addComponent(entityId, componentType);
    }

    public void removeComponent(int entityId, ComponentType componentType) {
        world.removeComponent(entityId, componentType);
    }

    public Map<ComponentType, Component> getComponents(int entityId) {
        return world.getComponents(entityId);
    }

    public Component getComponent(int entityId, ComponentType componentType) {
        return world.getComponent(entityId, componentType);
    }

    public ComponentType[] getAvailableComponentTypes() {
        return world.getAvailableComponentTypes();
    }
}
