package game.engine;

import game.engine.ECS.Entity;
import game.engine.ECS.EntityManager;
import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import game.engine.logging.Logger;

/**
 * High-level entity registry used by tools (editor) to create/destroy named entities.
 * Keeps the low-level allocation (EntityManager) and component cleanup (World) coordinated.
 */
public class EntityRegistry {
    private final EntityManager entityManager;
    private final World world;

    public EntityRegistry(EntityManager entityManager, World world) {
        this.entityManager = entityManager;
        this.world = world;
    }

    /** Create a named entity and return an Entity object, or null on failure. */
    public Entity createEntity(String name) {
        int id = entityManager.createEntity();
        if (id == -1) {
            Logger.warn(Logger.WORLD, "Failed to create entity: no IDs available.");
            return null;
        }
        entityManager.setName(id, name);
        Logger.info(Logger.WORLD, "Created entity " + id + " name=\"" + name + "\"");
        return new Entity(id, name != null ? name : ("Entity " + id));
    }

    /** Return a snapshot list of entities (id + name). */
    public List<Entity> listEntities() {
        List<Entity> out = new ArrayList<>();
        // Use the activeEntities bitset to iterate currently alive entities.
        BitSet bits = entityManager.activeEntities();
        Logger.trace(Logger.WORLD, () -> "Listing entities for UI refresh; activeCount=" + bits.cardinality());
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
            String name = entityManager.getName(i);
            out.add(new Entity(i, name != null ? name : ("Entity " + i)));
        }
        return out;
    }

    /** Get an Entity object, or null if id inactive. */
    public Entity getEntity(int id) {
        if (!entityManager.alive(id)) return null;
        String name = entityManager.getName(id);
        return new Entity(id, name != null ? name : ("Entity " + id));
    }

    /** Rename an entity; returns true if successful. */
    public boolean renameEntity(int id, String newName) {
        if (!entityManager.alive(id)) {
            Logger.warn(Logger.WORLD, "Rename failed: unknown entity id=" + id);
            return false;
        }
        entityManager.setName(id, newName);
        Logger.info(Logger.WORLD, "Renamed entity " + id + " -> \"" + newName + "\"");
        return true;
    }

    /** Fully destroy an entity: remove components then free the id. */
    public boolean destroyEntity(int id) {
        if (!entityManager.alive(id)) {
            Logger.warn(Logger.WORLD, "Attempted to destroy unknown entity id=" + id);
            return false;
        }
        Logger.debug(Logger.WORLD, () -> "Destroying entity " + id + " (cleanup components)");
        world.removeAllComponents(id);
        entityManager.removeEntity(id);
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
