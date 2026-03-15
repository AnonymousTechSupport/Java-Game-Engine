package game.engine;

import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ComponentRegistry {

    // Small handler tying a typed ComponentManager together with a supplier
    // that can create default instances for that type.
    private static class Handler<T extends Component> {
        final game.engine.ECS.ComponentManager<T> manager;
        final Supplier<T> supplier;

        Handler(game.engine.ECS.ComponentManager<T> manager, Supplier<T> supplier) {
            this.manager = manager;
            this.supplier = supplier;
        }

        Component add(int entityId) {
            T c = supplier.get();
            manager.add(entityId, c);
            return c;
        }

        void remove(int entityId) {
            manager.remove(entityId);
        }

        Component get(int entityId) {
            return manager.get(entityId);
        }
    }

    private final Map<ComponentType, Handler<?>> handlers = new HashMap<>();

    public <T extends Component> void register(ComponentType type, game.engine.ECS.ComponentManager<T> manager, Supplier<T> supplier) {
        handlers.put(type, new Handler<>(manager, supplier));
    }

    /** Return the typed manager if the caller knows the type. */
    @SuppressWarnings("unchecked")
    public <T extends Component> game.engine.ECS.ComponentManager<T> getManager(ComponentType type) {
        Handler<?> h = handlers.get(type);
        return h == null ? null : (game.engine.ECS.ComponentManager<T>) h.manager;
    }

    public Component addComponent(int entityId, ComponentType type) {
        Handler<?> h = handlers.get(type);
        if (h != null) return h.add(entityId);
        return null;
    }

    public void removeComponent(int entityId, ComponentType type) {
        Handler<?> h = handlers.get(type);
        if (h != null) h.remove(entityId);
    }

    public Component getComponent(int entityId, ComponentType type) {
        Handler<?> h = handlers.get(type);
        return h == null ? null : h.get(entityId);
    }

    public Map<ComponentType, Component> getComponents(int entityId) {
        Map<ComponentType, Component> components = new HashMap<>();
        for (Map.Entry<ComponentType, Handler<?>> entry : handlers.entrySet()) {
            Component c = entry.getValue().get(entityId);
            if (c != null) components.put(entry.getKey(), c);
        }
        return components;
    }

    public void removeAllComponents(int entityId) {
        for (Handler<?> h : handlers.values()) h.remove(entityId);
    }

    public ComponentType[] getAvailableComponentTypes() {
        return handlers.keySet().toArray(new ComponentType[0]);
    }
}
