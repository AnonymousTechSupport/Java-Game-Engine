package game.engine.ui;

import game.engine.EntityRegistry;

/**
 * A base class for UI components that provides access to shared resources
 * like the entity registry.
 */
public abstract class UIComponent {
    protected final EntityRegistry entityRegistry;

    public UIComponent(EntityRegistry entityRegistry) {
        this.entityRegistry = entityRegistry;
    }

    /**
     * Renders the UI component. This method is called every frame.
     */
    public abstract void render();
}
