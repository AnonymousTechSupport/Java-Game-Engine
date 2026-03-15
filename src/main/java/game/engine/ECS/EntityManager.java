package game.engine.ECS;

import game.engine.logging.Logger;

public class EntityManager {
    private static final int MAX_ENTITIES = 150;

    /**
     * A queue that tracks the availability of entity IDs and ensures that IDs are reused efficiently
     * ensuring that we never exceed max entity count
     */
    private java.util.Deque<Integer> availableEntityIds;
    /** A bit set that tracks which entity IDs are currently in use */
    private java.util.BitSet activeEntities;
    /** Optional display names for entities. */
    private final java.util.Map<Integer, String> names = new java.util.HashMap<>();

    /**
     * Initializes the EntityManager with a fixed number of available entity IDs.
     */
    public EntityManager() {
        availableEntityIds = new java.util.ArrayDeque<>(MAX_ENTITIES);
        for (int i = 0; i < MAX_ENTITIES; i++) {
            availableEntityIds.offer(i);
        }
        activeEntities = new java.util.BitSet(MAX_ENTITIES);
        Logger.info(Logger.ECS, "EntityManager initialized with capacity for " + MAX_ENTITIES + " entities.");
    }

    /** Creates a new entity and returns its ID. If no IDs are available, returns -1. */
    public int createEntity() {
        if (availableEntityIds.isEmpty()) {
            Logger.trace(Logger.ECS, () -> "Failed to create entity: no IDs available.");
            return -1;
        }
        Integer entityId = availableEntityIds.poll();
        if (entityId == null) {
            Logger.trace(Logger.ECS, () -> "Failed to create entity: poll returned null.");
            return -1;
        }
        activeEntities.set(entityId);
        Logger.trace(Logger.ECS, () -> "Created entity with ID " + entityId);
        return entityId;
    }

    /**
     * Removes an entity and returns its ID to the pool of available IDs.
     * @param entityId the ID of the entity to remove
     */
    public void removeEntity(int entityId) {
        if (entityId >= 0 && entityId < MAX_ENTITIES) {
            activeEntities.clear(entityId);
            // clear metadata and return id to pool
            names.remove(entityId);
            if (!availableEntityIds.contains(entityId)) {
                availableEntityIds.offer(entityId);
                Logger.trace(Logger.ECS, () -> "Removed entity with ID " + entityId);
            }
        }
    }

    /** 
     * Checks if an entity ID is currently active.
     * @param entityId the ID of the entity to check
     * @return true if the entity is active, false otherwise
     */
    public boolean alive(int entityId) {
        if (entityId < 0 || entityId >= MAX_ENTITIES) return false;
        return activeEntities.get(entityId);
    }

    /** Assign or update a display name for an active entity. */
    public void setName(int entityId, String name) {
        if (entityId < 0 || entityId >= MAX_ENTITIES) return;
        if (!activeEntities.get(entityId)) return;
        if (name == null) names.remove(entityId);
        else names.put(entityId, name);
    }

    /** Get the display name for an entity, or null if none / inactive. */
    public String getName(int entityId) {
        if (entityId < 0 || entityId >= MAX_ENTITIES) return null;
        if (!activeEntities.get(entityId)) return null;
        return names.get(entityId);
    }

    /** Convenience overload: create an entity and set its name. */
    public int createEntity(String name) {
        int id = createEntity();
        if (id != -1 && name != null) setName(id, name);
        return id;
    }

    public java.util.Deque<Integer> availableEntityIds() {
        return availableEntityIds;
    }

    public java.util.BitSet activeEntities() {
        return activeEntities;
    }
}
