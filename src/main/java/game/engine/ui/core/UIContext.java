package game.engine.ui.core;

import game.engine.ECS.components.Component;
import game.engine.ECS.components.ComponentType;
import game.engine.ui.services.UIEventQueue;
import game.engine.ui.services.SelectionService;
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
    private final SelectionService selectionService;

    public UIContext(UIEventQueue eventQueue, EntityRegistry entityRegistry) {
        this.eventQueue = eventQueue;
        this.entityRegistry = entityRegistry;
        this.selectionService = null; // UIContext doesn't create selection
                                      // service; UIManager owns it
    }

    /**
     * Defers an arbitrary action to be executed on the main/UI thread. This is
     * useful for any operations that need to interact with the EntityRegistry
     * or ECS in a thread-safe manner. For specific entity/component mutations,
     * use the provided defer* methods for clarity and intent.
     * 
     * @param action
     */
    public void defer(Runnable action) {
        eventQueue.enqueue(action);
    }

    /**
     * Returns a map of component type to component instance for the specified
     * entity. This is a read-only operation and should be used for UI rendering
     * and display purposes. For mutation operations (adding/removing
     * components), use the provided defer* methods to ensure thread safety.
     * 
     * @param entityId
     * @return a Map of ComponentType to Component instance for the specified
     * entity, or an empty map if the entity has no components.
     */
    public Map<ComponentType, Component> getComponents(int entityId) {
        return entityRegistry.getComponents(entityId);
    }

    /**
     * Returns the component of the specified type for the given entity, or null
     * if the entity does not have that component. This is a read-only operation
     * and should be used for UI rendering and display purposes. For mutation
     * operations (adding/removing components), use the provided defer* methods
     * to ensure thread safety.
     * 
     * @param entityId
     * @param type
     * @return the Component instance of the specified type for the given
     * entity, or null if not present.
     */
    public Component getComponent(int entityId, ComponentType type) {
        return entityRegistry.getComponent(entityId, type);
    }

    /**
     * Returns the list of available component types that can be added to
     * entities. This is used to populate the "Add Component" menu in the UI.
     * INTERNAL component types (used for engine metadata and not user-facing)
     * are filtered out.
     * 
     * @return array of ComponentType that can be added to entities via the UI.
     */
    public ComponentType[] getAvailableComponentTypes() {
        // Editor UI should not show INTERNAL components (engine-only metadata).
        ComponentType[] all = entityRegistry.getAvailableComponentTypes();
        return java.util.Arrays.stream(all).filter(t -> t.getCategory() != game.engine.ECS.components.ComponentType.Category.INTERNAL)
                .toArray(ComponentType[]::new);
    }

    /**
     * Defers adding a component to an entity. The actual addComponent call will
     * be executed on the main thread to ensure thread safety with the
     * underlying EntityRegistry and ECS.
     * 
     * @param entityId
     * @param type
     */
    public void deferAddComponent(int entityId, ComponentType type) {
        defer(() -> entityRegistry.addComponent(entityId, type));
    }

    /**
     * Defers removing a component from an entity. The actual removeComponent
     * call will be executed on the main thread to ensure thread safety with the
     * underlying EntityRegistry and ECS.
     * 
     * @param entityId
     * @param type
     */
    public void deferRemoveComponent(int entityId, ComponentType type) {
        defer(() -> entityRegistry.removeComponent(entityId, type));
    }

    /**
     * Defers destroying an entity. The actual destroyEntity call will be
     * executed on the main thread to ensure thread safety with the underlying
     * EntityRegistry and ECS.
     * 
     * @param entityId
     */
    public void deferDestroyEntity(int entityId) {
        defer(() -> entityRegistry.destroyEntity(entityId));
    }

    public int getMainCameraEntityId() {
        return entityRegistry.getMainCameraEntityId();
    }

    public void setMainCameraEntityId(int id) {
        entityRegistry.setMainCameraEntityId(id);
    }

    /**
     * Provides direct access to the EntityRegistry for read-only operations.
     * Mutation operations should be performed via the provided defer* methods
     * to ensure thread safety.
     * 
     * @return the EntityRegistry instance used by the UIContext
     */
    EntityRegistry getEntityRegistry() {
        return entityRegistry;
    }

    public SelectionService getSelectionService() {
        return selectionService;
    }
}
