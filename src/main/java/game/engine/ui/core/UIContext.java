package game.engine.ui.core;

import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;
import game.engine.ui.services.UIEventQueue;
import game.engine.EntityRegistry;

import java.util.Map;

/**
 * UIContext provides lightweight services and controlled access to entity
 * operations used by UI components. Mutation helpers are provided as deferred
 * operations to ensure modifications happen on the UI/main thread.
 */
public class UIContext {

    private final UIEventQueue eventQueue;
    private final EntityRegistry entityRegistry;

    public UIContext(UIEventQueue eventQueue, EntityRegistry entityRegistry) {
        this.eventQueue = eventQueue;
        this.entityRegistry = entityRegistry;
    }

    public void defer(Runnable action) {
        eventQueue.enqueue(action);
    }

    // Read helpers (synchronous)
    public Map<ComponentType, Component> getComponents(int entityId) {
        return entityRegistry.getComponents(entityId);
    }
    public Component getComponent(int entityId, ComponentType type) {
        return entityRegistry.getComponent(entityId, type);
    }
    public ComponentType[] getAvailableComponentTypes() {
        return entityRegistry.getAvailableComponentTypes();
    }
    // Deferred mutation helpers (convenience wrappers)
    public void deferAddComponent(int entityId, ComponentType type) {
        defer(() -> entityRegistry.addComponent(entityId, type));
    }
    public void deferRemoveComponent(int entityId, ComponentType type) {
        defer(() -> entityRegistry.removeComponent(entityId, type));
    }
    public void deferDestroyEntity(int entityId) {
        defer(() -> entityRegistry.destroyEntity(entityId));
    }
    // Package-private getter retained only for internal wiring where unavoidable    EntityRegistry getEntityRegistry() {
        return entityRegistry;
    }
}
