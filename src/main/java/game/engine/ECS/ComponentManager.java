package game.engine.ECS;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple insertion-ordered component container keyed by entity id.
 */
public class ComponentManager<T> {
    private final Map<Integer, T> store = new LinkedHashMap<>();

    /** Add or replace a component for an entity. */
    public void add(int entityId, T component) { store.put(entityId, component); }

    /** Get a component for an entity, or null if missing. */
    public T get(int entityId) { return store.get(entityId); }

    /** Remove a component for an entity. */
    public void remove(int entityId) { store.remove(entityId); }

    /** Returns entity ids in insertion order. */
    public Set<Integer> getEntityIds() { return store.keySet(); }
}
