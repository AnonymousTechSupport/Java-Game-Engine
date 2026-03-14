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
        Logger.check(!availableEntityIds.isEmpty(), "No available entity IDs left.");
        int entityId = availableEntityIds.poll();
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
        return activeEntities.get(entityId);
    }

    public java.util.Deque<Integer> availableEntityIds() {
        return availableEntityIds;
    }

    public java.util.BitSet activeEntities() {
        return activeEntities;
    }
}
